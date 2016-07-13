package org.blocks4j.reconf.client.config.update;

import org.blocks4j.reconf.client.config.ConfigurationRepository;
import org.blocks4j.reconf.client.elements.ConfigurationItemElement;
import org.blocks4j.reconf.client.elements.ConfigurationRepositoryElement;
import org.blocks4j.reconf.client.setup.Environment;
import org.blocks4j.reconf.client.setup.config.ConnectionSettings;
import org.blocks4j.reconf.infra.log.LoggerHolder;
import org.blocks4j.reconf.infra.shutdown.ShutdownBean;
import org.blocks4j.reconf.throwables.UpdateConfigurationRepositoryException;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public class ConfigurationRepositoryUpdater implements Runnable, ShutdownBean {

    private final Environment environment;

    private ConfigurationRepositoryElement configurationRepositoryElement;
    private ConfigurationRepository repository;

    private ExecutorService executorService;

    public ConfigurationRepositoryUpdater(Environment environment, ConfigurationRepository repository, ConfigurationRepositoryElement configurationRepositoryElement) {
        this.repository = repository;
        this.environment = environment;
        this.configurationRepositoryElement = configurationRepositoryElement;

        this.loadExecutorService();

        this.syncNow(true);

        environment.manageShutdownObject(this);
    }

    private void loadExecutorService() {
        this.executorService = Executors.newCachedThreadPool(new ThreadFactory() {
            private int threadCount = 0;

            @Override
            public Thread newThread(Runnable runnable) {
                Thread thread = new Thread(runnable);
                thread.setDaemon(true);
                thread.setName("reconf-requisitor-" + ++threadCount);
                return thread;
            }
        });
    }

    @Override
    public void run() {
        try {
            syncNow(false);
        } catch (Throwable throwable) {
            LoggerHolder.getLog().warn("Error", throwable);
        }
    }

    public void syncNow(boolean useLocalCache) {
        this.syncNow(UpdateConfigurationRepositoryException.class, useLocalCache);
    }

    public void syncNow(Class<? extends RuntimeException> exceptionClass) {
        this.syncNow(exceptionClass, false);
    }

    public void syncNow(Class<? extends RuntimeException> exceptionClass, boolean useLocalCache) {
        List<ConfigurationItemUpdateResult> fullSyncResult = Collections.synchronizedList(new ArrayList<>());
        CompletableFuture<List<ConfigurationItemUpdateResult>> fullSyncResultFuture = CompletableFuture.completedFuture(fullSyncResult);

        try {

            for (ConfigurationItemElement configurationItemElement : this.configurationRepositoryElement.getConfigurationItems()) {
                ConfigurationItemRequisitor remoteRequisitor = new ConfigurationItemRequisitor(configurationItemElement, environment.getRemoteSource());

                if (useLocalCache) {
                    ConfigurationItemRequisitor localRequisitor = new ConfigurationItemRequisitor(configurationItemElement, environment.getLocalCacheSource());
                    fullSyncResultFuture = this.appendAsyncConfigurationSyncJob(fullSyncResultFuture, remoteRequisitor, localRequisitor);
                } else {
                    fullSyncResultFuture = this.appendAsyncConfigurationSyncJob(fullSyncResultFuture, remoteRequisitor);
                }

            }

            ConnectionSettings connectionSettings = this.environment.getConnectionSettings();
            fullSyncResult = fullSyncResultFuture.get(connectionSettings.getTimeout(), connectionSettings.getTimeUnit());
        } catch (Throwable throwable) {
            this.throwException(exceptionClass, throwable);
        }

        if (hasError(fullSyncResult)) {
            this.throwException(exceptionClass);
        }
    }

    private boolean hasError(Collection<ConfigurationItemUpdateResult> updateResults) {
        Optional<ConfigurationItemUpdateResult> updateErrorSample = updateResults.stream()
                                                                                 .filter(ConfigurationItemUpdateResult::isFailure).findFirst();
        return updateErrorSample.isPresent();
    }

    private void throwException(Class<? extends RuntimeException> exceptionClass) {
        try {
            Constructor<? extends RuntimeException> constructor = exceptionClass.getConstructor(String.class);
            constructor.setAccessible(true);
            throw constructor.newInstance("Error");
        } catch (Exception ignored) {
            throw new UpdateConfigurationRepositoryException("Error");
        }
    }

    private void throwException(Class<? extends RuntimeException> exceptionClass, Throwable cause) {
        try {
            Constructor<? extends RuntimeException> constructor = exceptionClass.getConstructor(String.class, Throwable.class);
            constructor.setAccessible(true);
            throw constructor.newInstance("Error", cause);
        } catch (Exception ignored) {
            throw new UpdateConfigurationRepositoryException("Error", cause);
        }
    }

    private CompletableFuture<List<ConfigurationItemUpdateResult>> appendAsyncConfigurationSyncJob(CompletableFuture<List<ConfigurationItemUpdateResult>> fullSyncResultFuture, ConfigurationItemRequisitor requisitor) {
        return appendAsyncConfigurationSyncJob(fullSyncResultFuture, requisitor, null);
    }

    private CompletableFuture<List<ConfigurationItemUpdateResult>> appendAsyncConfigurationSyncJob(CompletableFuture<List<ConfigurationItemUpdateResult>> fullSyncResultFuture, ConfigurationItemRequisitor remoteRequisitor, ConfigurationItemRequisitor localRequisitor) {
        CompletableFuture<ConfigurationItemUpdateResult> requestFuture = CompletableFuture.supplyAsync(remoteRequisitor::doRequest, this.executorService);

        if (localRequisitor != null) {
            requestFuture = this.enableLocalRequest(requestFuture, localRequisitor);
        }

        return fullSyncResultFuture.thenCombine(requestFuture,
                                                (fullSyncResult, currentResult) -> {
                                                    if (currentResult.isSuccess()) {
                                                        currentResult = this.repository.update(currentResult);
                                                    }
                                                    fullSyncResult.add(currentResult);
                                                    return fullSyncResult;
                                                });
    }

    private CompletableFuture<ConfigurationItemUpdateResult> enableLocalRequest(CompletableFuture<ConfigurationItemUpdateResult> requestFuture, ConfigurationItemRequisitor localRequisitor) {
        requestFuture = requestFuture.thenApply(configurationItemUpdateResult -> {
            ConfigurationItemUpdateResult finalResult = configurationItemUpdateResult;
            if (configurationItemUpdateResult.isFailure()) {
                finalResult = localRequisitor.doRequest();
            }

            return finalResult;
        });
        return requestFuture;
    }

    public Class<?> getRespositoryClass() {
        return this.configurationRepositoryElement.getInterfaceClass();
    }

    @Override
    public void shutdown() {
        this.executorService.shutdown();
    }
}
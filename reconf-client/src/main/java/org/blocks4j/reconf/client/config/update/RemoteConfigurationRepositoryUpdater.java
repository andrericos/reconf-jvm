package org.blocks4j.reconf.client.config.update;

import com.google.common.collect.Sets;
import org.blocks4j.reconf.client.config.ConfigurationItemId;
import org.blocks4j.reconf.client.config.ConfigurationRepository;
import org.blocks4j.reconf.client.config.listener.ModificationEvent;
import org.blocks4j.reconf.client.config.listener.ModificationListener;
import org.blocks4j.reconf.client.config.update.requisitor.ConfigurationItemRequisitor;
import org.blocks4j.reconf.client.elements.ConfigurationItemElement;
import org.blocks4j.reconf.client.elements.ConfigurationRepositoryElement;
import org.blocks4j.reconf.client.setup.Environment;
import org.blocks4j.reconf.client.setup.config.ConnectionSettings;
import org.blocks4j.reconf.infra.concurrent.ReconfExecutors;
import org.blocks4j.reconf.infra.log.LoggerHolder;
import org.blocks4j.reconf.infra.shutdown.ShutdownBean;
import org.blocks4j.reconf.throwables.UpdateConfigurationRepositoryException;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

public class RemoteConfigurationRepositoryUpdater implements Runnable, ShutdownBean {

    private final Environment environment;

    private final ConfigurationRepositoryElement configurationRepositoryElement;
    private final ConfigurationRepository repository;

    private final ExecutorService executorService;
    private final Set<ModificationListener> modificationListeners;

    public RemoteConfigurationRepositoryUpdater(Environment environment, ConfigurationRepository repository, ConfigurationRepositoryElement configurationRepositoryElement) {
        this.repository = repository;
        this.environment = environment;
        this.configurationRepositoryElement = configurationRepositoryElement;
        this.executorService = ReconfExecutors.newReconfThreadExecutor("requisitors");
        this.modificationListeners = Sets.newConcurrentHashSet();


        environment.manageShutdownObject(this);
    }

    public void addModificationListener(ModificationListener modificationListener) {
        this.modificationListeners.add(modificationListener);
    }

    @Override
    public void run() {
        try {
            syncNow();
        } catch (Throwable throwable) {
            LoggerHolder.getLog().warn("Error", throwable);
        }
    }

    public void syncNow() {
        this.syncNow(UpdateConfigurationRepositoryException.class);
    }

    public void syncNow(Class<? extends RuntimeException> exceptionClass) {
        List<ConfigurationItemUpdateResult> fullSyncResult = Collections.synchronizedList(new ArrayList<>());
        CompletableFuture<List<ConfigurationItemUpdateResult>> fullSyncResultFuture = CompletableFuture.completedFuture(fullSyncResult);

        try {

            for (ConfigurationItemElement configurationItemElement : this.configurationRepositoryElement.getConfigurationItems()) {
                fullSyncResultFuture = this.appendAsyncConfigurationSyncJob(fullSyncResultFuture, configurationItemElement);
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
                                                                                 .filter(ConfigurationItemUpdateResult::isFailure)
                                                                                 .findFirst();
        return updateErrorSample.isPresent();
    }

    private CompletableFuture<List<ConfigurationItemUpdateResult>> appendAsyncConfigurationSyncJob(CompletableFuture<List<ConfigurationItemUpdateResult>> fullSyncResultFuture, ConfigurationItemElement configurationItemElement) {
        CompletableFuture<ConfigurationItemUpdateResult> requestFuture = this.doAsyncRequest(this.environment, configurationItemElement);

        return registerResults(fullSyncResultFuture, requestFuture);
    }


    protected CompletableFuture<ConfigurationItemUpdateResult> doAsyncRequest(Environment environment, ConfigurationItemElement configurationItemElement) {
        ConfigurationItemRequisitor remoteRequisitor = new ConfigurationItemRequisitor(configurationItemElement, environment.getRemoteSource());

        return CompletableFuture.supplyAsync(remoteRequisitor::doRequest, this.executorService);
    }

    private CompletableFuture<List<ConfigurationItemUpdateResult>> registerResults(CompletableFuture<List<ConfigurationItemUpdateResult>> fullSyncResultFuture, CompletableFuture<ConfigurationItemUpdateResult> requestFuture) {
        return fullSyncResultFuture.thenCombine(requestFuture,
                                                (fullSyncResult, currentResult) -> {
                                                    if (currentResult.isSuccess()) {
                                                        ConfigurationItemId configurationItemId = currentResult.getConfigurationItemId();

                                                        Object convertedValue = currentResult.getObject();

                                                        this.repository.update(configurationItemId, convertedValue);
                                                        this.fireModificationEvents(new ModificationEvent(configurationItemId,
                                                                                                          currentResult.getRawValue(),
                                                                                                          convertedValue));
                                                    }
                                                    fullSyncResult.add(currentResult);
                                                    return fullSyncResult;
                                                });
    }

    private void fireModificationEvents(ModificationEvent modificationEvent) {
        ConfigurationItemId configurationItemId = modificationEvent.getConfigurationItemId();
        this.modificationListeners.stream()
                                  .filter(modificationListener -> modificationListener.isEnabled(configurationItemId))
                                  .forEach(modificationListener -> {
                                      try {
                                          modificationListener.onChange(modificationEvent);
                                      } catch (RuntimeException exception) {
                                          exception.printStackTrace();
                                      }
                                  });
    }

    public Class<?> getRespositoryClass() {
        return this.configurationRepositoryElement.getInterfaceClass();
    }

    private void throwException(Class<? extends RuntimeException> exceptionClass) {
        try {
            Constructor<? extends RuntimeException> constructor = exceptionClass.getConstructor(String.class);
            constructor.setAccessible(true);
            throw constructor.newInstance("Error");
        } catch (Exception e) {
            throw new UpdateConfigurationRepositoryException("Error", e);
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

    @Override
    public void shutdown() {
        this.executorService.shutdown();
    }
}
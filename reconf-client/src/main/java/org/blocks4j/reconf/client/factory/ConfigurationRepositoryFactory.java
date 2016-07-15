/*
 *   Copyright 2013-2015 Blocks4J Team (www.blocks4j.org)
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package org.blocks4j.reconf.client.factory;

import org.blocks4j.reconf.client.config.ConfigurationRepository;
import org.blocks4j.reconf.client.config.update.ConfigurationRepositoryUpdater;
import org.blocks4j.reconf.client.customization.Customization;
import org.blocks4j.reconf.client.elements.ConfigurationRepositoryElement;
import org.blocks4j.reconf.client.proxy.ConfigurationRepositoryProxyHandler;
import org.blocks4j.reconf.client.setup.DefaultEnvironment;
import org.blocks4j.reconf.client.setup.Environment;
import org.blocks4j.reconf.client.setup.config.ReconfConfiguration;
import org.blocks4j.reconf.infra.i18n.MessagesBundle;
import org.blocks4j.reconf.infra.log.LoggerHolder;
import org.blocks4j.reconf.infra.shutdown.ShutdownBean;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;

public class ConfigurationRepositoryFactory implements ShutdownBean {

    private static final MessagesBundle msg = MessagesBundle.getBundle(ConfigurationRepositoryFactory.class);

    private Environment environment;

    private ConfigurationRepository repository;
    private ConfigurationRepositoryElementFactory factory;
    private final ConcurrentMap<String, Object> proxyCache;
    private final Set<ConfigurationRepositoryUpdater> updatersCreated;
    private final ScheduledExecutorService scheduledExecutorService;

    public ConfigurationRepositoryFactory() {
        this(new DefaultEnvironment());
    }

    public ConfigurationRepositoryFactory(ReconfConfiguration reconfConfiguration) {
        this(new DefaultEnvironment(reconfConfiguration));
    }

    public ConfigurationRepositoryFactory(Environment environment) {
        this.environment = environment;

        this.updatersCreated = new HashSet<>();
        this.proxyCache = new ConcurrentHashMap<>();
        this.factory = new ConfigurationRepositoryElementFactory(environment.getReconfConfiguration());
        this.repository = environment.getRepository();

        this.scheduledExecutorService = this.createSchedulerService();
        this.environment.manageShutdownObject(this);
    }

    private ScheduledExecutorService createSchedulerService() {
        return Executors.newScheduledThreadPool(5, new ThreadFactory() {
            private int threadCount = 0;

            @Override
            public Thread newThread(Runnable runnable) {
                Thread thread = new Thread(runnable);
                thread.setDaemon(true);
                thread.setName("reconf-scheduler-" + ++threadCount);
                return thread;
            }
        });
    }

    public synchronized <T> T get(Class<T> arg) {
        return get(arg, null);
    }

    @SuppressWarnings("unchecked")
    public synchronized <T> T get(Class<T> arg, Customization customization) {
        if (customization == null) {
            customization = new Customization();
        }

        String key = arg.getName() + customization;
        if (proxyCache.containsKey(key)) {
            LoggerHolder.getLog().info(msg.format("cached.instance", arg.getName()));
            return (T) proxyCache.get(key);
        }

        ConfigurationRepositoryElement repo = this.factory.create(arg);
        repo.applyCustomization(customization);

        //LoggerHolder.getLog().info(msg.format("new.instance", LineSeparator.value(), repo.toString()));

        Object result = newInstance(arg, repo);
        proxyCache.put(key, result);
        return (T) result;
    }

    @SuppressWarnings("unchecked")
    private synchronized <T> T newInstance(Class<T> arg, ConfigurationRepositoryElement configurationRepositoryElement) {
        ConfigurationRepositoryUpdater repositoryUpdater = initUpdater(configurationRepositoryElement);

        Object proxyInstance = Proxy.newProxyInstance(arg.getClassLoader(), new Class<?>[]{arg}, new ConfigurationRepositoryProxyHandler(configurationRepositoryElement, this.repository, repositoryUpdater));

        this.validateProxyLoad(proxyInstance, arg);

        return (T) proxyInstance;
    }

    @NotNull
    private ConfigurationRepositoryUpdater initUpdater(ConfigurationRepositoryElement configurationRepositoryElement) {
        ConfigurationRepositoryUpdater repositoryUpdater = new ConfigurationRepositoryUpdater(this.environment, this.repository, configurationRepositoryElement);

        this.updatersCreated.add(repositoryUpdater);

        repositoryUpdater.addModificationListener(environment.getLocalCacheSource());

        repositoryUpdater.syncNow(true);

        this.scheduleUpdater(configurationRepositoryElement, repositoryUpdater);
        return repositoryUpdater;
    }

    private void validateProxyLoad(Object proxyInstance, Class<?> proxyInterface) {
        for (Method method : proxyInterface.getMethods()) {
            Object methodReturn;
            try {
                methodReturn = method.invoke(proxyInstance);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new IllegalStateException(e);
            }

            if (methodReturn == null) {
                throw new IllegalStateException();
            }
        }
    }

    private void scheduleUpdater(ConfigurationRepositoryElement configurationRepositoryElement, ConfigurationRepositoryUpdater repositoryUpdater) {
        this.scheduledExecutorService.scheduleWithFixedDelay(repositoryUpdater, configurationRepositoryElement.getRate(), configurationRepositoryElement.getRate(), configurationRepositoryElement.getTimeUnit());
    }

    public Environment getEnvironment() {
        return this.environment;
    }

    public Set<ConfigurationRepositoryUpdater> getUpdatersCreated() {
        return Collections.unmodifiableSet(this.updatersCreated);
    }

    @Override
    public void shutdown() {
        this.scheduledExecutorService.shutdown();
    }
}

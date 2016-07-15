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
package org.blocks4j.reconf.client.proxy;

import org.blocks4j.reconf.annotations.ConfigurationItem;
import org.blocks4j.reconf.annotations.UpdateConfigurationRepository;
import org.blocks4j.reconf.client.config.ConfigurationItemId;
import org.blocks4j.reconf.client.config.ConfigurationRepository;
import org.blocks4j.reconf.client.config.update.ConfigurationRepositoryUpdater;
import org.blocks4j.reconf.client.elements.ConfigurationItemElement;
import org.blocks4j.reconf.client.elements.ConfigurationRepositoryElement;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.stream.Collectors;

public class ConfigurationRepositoryProxyHandler implements InvocationHandler {

    private final ConfigurationRepository repository;

    private final ConfigurationRepositoryUpdater repositoryUpdater;
    private final Map<Method, ConfigurationItemId> configurationIdsByMethods;

    public ConfigurationRepositoryProxyHandler(ConfigurationRepositoryElement configurationRepositoryElement, ConfigurationRepository repository, ConfigurationRepositoryUpdater repositoryUpdater) {
        this.configurationIdsByMethods = this.extractConfigurationIdsByMethods(configurationRepositoryElement);
        this.repository = repository;
        this.repositoryUpdater = repositoryUpdater;
    }

    private Map<Method, ConfigurationItemId> extractConfigurationIdsByMethods(ConfigurationRepositoryElement configurationRepositoryElement) {
        return configurationRepositoryElement.getConfigurationItems()
                                             .stream()
                                             .collect(Collectors.toMap(ConfigurationItemElement::getMethod,
                                                                       ConfigurationItemElement::getConfigurationItemId));
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        boolean updateAnnotationPresent = method.isAnnotationPresent(UpdateConfigurationRepository.class);
        boolean configurationAnnotationPresent = method.isAnnotationPresent(ConfigurationItem.class);

        if (!configurationAnnotationPresent && !updateAnnotationPresent) {
            return method.invoke(this, args);
        }

        if (updateAnnotationPresent) {
            repositoryUpdater.syncNow(method.getAnnotation(UpdateConfigurationRepository.class).onErrorThrow());
        }

        Object configValue = null;

        if (configurationAnnotationPresent) {
            configValue = repository.getValueOf(this.configurationIdsByMethods.get(method));
        }

        return configValue;
    }
}

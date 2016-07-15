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

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.blocks4j.reconf.annotations.ConfigurationItem;
import org.blocks4j.reconf.annotations.UpdateConfigurationRepository;
import org.blocks4j.reconf.client.elements.ConfigurationItemElement;
import org.blocks4j.reconf.client.elements.ConfigurationRepositoryElement;
import org.blocks4j.reconf.client.setup.config.ReconfConfiguration;
import org.blocks4j.reconf.client.validation.ConfigurationRepositoryElementValidator;
import org.blocks4j.reconf.infra.i18n.MessagesBundle;
import org.blocks4j.reconf.infra.log.LoggerHolder;
import org.blocks4j.reconf.infra.system.LineSeparator;
import org.blocks4j.reconf.infra.throwables.ReConfInitializationError;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;


public class ConfigurationRepositoryElementFactory {

    private static final MessagesBundle msg = MessagesBundle.getBundle(ConfigurationRepositoryElementFactory.class);
    private ReconfConfiguration configuration;

    public ConfigurationRepositoryElementFactory(ReconfConfiguration configuration) {
        this.configuration = configuration;
    }

    public ConfigurationRepositoryElement create(Class<?> arg) {
        ConfigurationRepositoryElement result = createNewRepositoryFor(arg);
        validate(result);
        return result;
    }

    private ConfigurationRepositoryElement createNewRepositoryFor(Class<?> repository) {
        ConfigurationRepositoryElement configurationRepositoryElement = ConfigurationRepositoryElement.forRepository(repository);

        if (configurationRepositoryElement == null) {
            return null;
        }

        ConfigurationItemElement.Builder configurationItemBuilder = ConfigurationItemElement.forConfigurationRepositoryElement(configurationRepositoryElement);

        Arrays.stream(repository.getMethods())
              .map(method -> {
                  checkAnnotations(method);
                  return method;
              })
              .filter(method -> Modifier.isAbstract(method.getModifiers()))
              .filter(method -> method.isAnnotationPresent(ConfigurationItem.class))
              .map(method -> configurationItemBuilder.forMethod(method).build())
              .forEach(configurationItemElement -> {
                  override(configurationRepositoryElement, configurationItemElement);
                  configurationRepositoryElement.getConfigurationItems().add(configurationItemElement);
              });


        return configurationRepositoryElement;
    }

    private void override(ConfigurationRepositoryElement configurationRepositoryElement, ConfigurationItemElement configurationItemElement) {
        configurationItemElement.override(configurationRepositoryElement);
    }

    private void checkAnnotations(Method method) {
        if (!(method.isAnnotationPresent(ConfigurationItem.class) || method.isAnnotationPresent(UpdateConfigurationRepository.class))) {
            throw new ReConfInitializationError(msg.format("error.not.configured.method", method.toString()));
        }
    }


    private void validate(ConfigurationRepositoryElement arg) {
        if (arg == null) {
            throw new ReConfInitializationError(msg.get("error.internal"));
        }

        Map<String, String> violations = ConfigurationRepositoryElementValidator.validate(arg);
        if (MapUtils.isEmpty(violations)) {
            return;
        }

        List<String> errors = new ArrayList<>();
        int i = 1;
        for (Entry<String, String> violation : violations.entrySet()) {
            errors.add(i++ + " - " + violation.getValue() + " @ " + StringUtils.replace(arg.getInterfaceClass().toString(), "interface ", "") + "." + violation.getKey());
        }

        if (configuration.isDebug()) {
            LoggerHolder.getLog().error(msg.format("error.factory", LineSeparator.value(), StringUtils.join(errors, LineSeparator.value())) + LineSeparator.value());
        } else {
            throw new ReConfInitializationError(msg.format("error.factory", LineSeparator.value(), StringUtils.join(errors, LineSeparator.value())) + LineSeparator.value());
        }
    }
}

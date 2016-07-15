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
package org.blocks4j.reconf.client.elements;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.blocks4j.reconf.annotations.ConfigurationRepository;
import org.blocks4j.reconf.client.config.ConfigurationItemId;
import org.blocks4j.reconf.client.config.ConfigurationRepositoryId;
import org.blocks4j.reconf.client.customization.Customization;
import org.blocks4j.reconf.infra.system.LineSeparator;
import org.blocks4j.reconf.infra.throwables.ReConfInitializationError;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;


public class ConfigurationRepositoryElement {

    private final ConfigurationRepositoryId configurationRepositoryId;
    private final Integer rate;
    private final TimeUnit timeUnit;

    private final Class<?> interfaceClass;
    private final List<ConfigurationItemElement> configurationItems;
    private Customization customization;

    private ConfigurationRepositoryElement(ConfigurationRepositoryId configurationRepositoryId,
                                           Integer rate,
                                           TimeUnit timeUnit,
                                           Class<?> interfaceClass) {
        this.configurationRepositoryId = configurationRepositoryId;
        this.rate = rate;
        this.timeUnit = timeUnit;
        this.interfaceClass = interfaceClass;
        this.configurationItems = new ArrayList<>();
    }

    public static ConfigurationRepositoryElement forRepository(Class<?> repository) {
        if (!repository.isInterface()) {
            throw new ReConfInitializationError("is not a interface");
        }

        if (!repository.isAnnotationPresent(ConfigurationRepository.class)) {
            return null;
        }

        ConfigurationRepository ann = repository.getAnnotation(ConfigurationRepository.class);

        return new ConfigurationRepositoryElement(new ConfigurationRepositoryId(ann.product(), ann.component()),
                                                  ann.pollingRate(),
                                                  ann.pollingTimeUnit(),
                                                  repository);
    }

    public ConfigurationRepositoryId getConfigurationRepositoryId() {
        return configurationRepositoryId;
    }

    public Class<?> getInterfaceClass() {
        return interfaceClass;
    }

    public List<ConfigurationItemElement> getConfigurationItems() {
        return configurationItems;
    }

    public Customization getCustomization() {
        return customization;
    }

    public Integer getRate() {
        return rate;
    }

    public TimeUnit getTimeUnit() {
        return timeUnit;
    }

    public void applyCustomization(Customization customization) {
        this.customization = customization;

        this.getConfigurationRepositoryId().applyCustomization(customization);

        this.getConfigurationItems().forEach(item -> item.getConfigurationItemId().applyCustomization(customization));
    }

    public Collection<String> getFullProperties() {
        Set<String> result = new LinkedHashSet<>();
        for (ConfigurationItemElement elem : configurationItems) {
            String productName;
            if (StringUtils.isEmpty(elem.getConfigurationItemId().getProduct())) {
                productName = getConfigurationRepositoryId().getProduct();
            } else {
                productName = elem.getConfigurationItemId().getProduct();
            }

            String componentName;
            if (StringUtils.isEmpty(elem.getConfigurationItemId().getComponent())) {
                componentName = getConfigurationRepositoryId().getComponent();
            } else {
                componentName = elem.getConfigurationItemId().getComponent();
            }
            result.add(FullPropertyElement.from(productName, componentName, elem.getConfigurationItemId().getName()));
        }
        return result;
    }


    @Override
    public String toString() {
        ToStringBuilder result = new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("class", getInterfaceClass())
                .append("product", getConfigurationRepositoryId().getProduct())
                .append("component", getConfigurationRepositoryId().getComponent())
                .append("pollingRate", getRate())
                .append("pollingTimeUnit", getTimeUnit());

        result.append("@ConfigurationItems", LineSeparator.value() + getConfigurationItems());
        return result.toString();
    }

}

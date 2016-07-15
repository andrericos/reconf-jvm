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
import org.blocks4j.reconf.adapter.ConfigurationAdapter;
import org.blocks4j.reconf.annotations.ConfigurationItem;
import org.blocks4j.reconf.client.adapter.DefaultAntlr4ConfigurationAdapter;
import org.blocks4j.reconf.client.config.ConfigurationItemId;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;


public class ConfigurationItemElement {

    private final Method method;
    private final Class<? extends ConfigurationAdapter> adapter;
    private ConfigurationItemId configurationItemId;

    private ConfigurationItemElement(ConfigurationItemId configurationItemId, Method method, Class<? extends ConfigurationAdapter> adapter) {
        this.method = method;
        this.configurationItemId = configurationItemId;
        this.adapter = adapter;
    }

    public Method getMethod() {
        return method;
    }

    public ConfigurationItemId getConfigurationItemId() {
        return configurationItemId;
    }

    public Class<? extends ConfigurationAdapter> getAdapter() {
        return adapter;
    }

    public static Builder forConfigurationRepositoryElement(ConfigurationRepositoryElement repositoryElement) {
        return new Builder(repositoryElement);
    }

    @Override
    public String toString() {
        ToStringBuilder result = new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE).append("method", StringUtils.replace(getMethod().toString(), "public abstract ", ""));
        addToString(result, "product", this.getConfigurationItemId().getProduct());
        addToString(result, "component", this.getConfigurationItemId().getComponent());
        result.append("name", this.getConfigurationItemId().getName());
        return result.toString();
    }

    private void addToString(ToStringBuilder arg, String key, String value) {
        if (StringUtils.isNotBlank(value)) {
            arg.append(key, value);
        }
    }

    public void override(ConfigurationRepositoryElement configurationRepositoryElement) {
        this.configurationItemId = this.configurationItemId.override(configurationRepositoryElement.getConfigurationRepositoryId());
    }

    public static class Builder {

        private ConfigurationRepositoryElement repositoryElement;

        private Method method;

        private Builder(ConfigurationRepositoryElement repositoryElement) {
            this.repositoryElement = repositoryElement;
        }

        public Builder forMethod(Method method) {
            this.method = method;
            return this;
        }

        public ConfigurationItemElement build() {
            ConfigurationItem configurationItemAnnotation = method.getAnnotation(ConfigurationItem.class);

            ConfigurationItemId configurationItemId = this.extractConfigurationItemInformation(configurationItemAnnotation);
            configurationItemId.override(this.repositoryElement.getConfigurationRepositoryId());

            return new ConfigurationItemElement(configurationItemId, this.method, this.getAdapter(configurationItemAnnotation));
        }

        @NotNull
        private Class<? extends ConfigurationAdapter> getAdapter(ConfigurationItem configurationItemAnnotation) {
            Class<? extends ConfigurationAdapter> adapter = configurationItemAnnotation.adapter();

            if (adapter == ConfigurationAdapter.class) {
                adapter = DefaultAntlr4ConfigurationAdapter.class;
            }

            return adapter;
        }

        private ConfigurationItemId extractConfigurationItemInformation(ConfigurationItem configurationItemAnnotation) {
            return new ConfigurationItemId(configurationItemAnnotation.product(),
                                           configurationItemAnnotation.component(),
                                           configurationItemAnnotation.value());
        }
    }
}

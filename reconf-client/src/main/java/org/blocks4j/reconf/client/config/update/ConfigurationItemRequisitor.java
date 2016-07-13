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
package org.blocks4j.reconf.client.config.update;

import org.apache.commons.lang3.ArrayUtils;
import org.blocks4j.reconf.adapter.ConfigurationAdapter;
import org.blocks4j.reconf.client.config.ConfigurationItemId;
import org.blocks4j.reconf.client.elements.ConfigurationItemElement;
import org.blocks4j.reconf.data.MethodReturnData;
import org.blocks4j.reconf.infra.i18n.MessagesBundle;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;


public class ConfigurationItemRequisitor {

    private final static MessagesBundle msg = MessagesBundle.getBundle(ConfigurationItemRequisitor.class);

    private ConfigurationSource configurationSource;
    private ConfigurationItemElement configurationItemElement;
    private ConfigurationAdapter configurationAdapter;
    private Type returnType;

    public ConfigurationItemRequisitor(ConfigurationItemElement configurationItemElement, ConfigurationSource configurationSource) {
        this.configurationSource = configurationSource;
        this.configurationItemElement = configurationItemElement;
        this.configurationAdapter = this.getRemoteAdapter(configurationItemElement);
        this.returnType = this.getReturnType(configurationItemElement);
    }

    private Type getReturnType(ConfigurationItemElement configurationItemElement) {
        Type type;
        Method method = configurationItemElement.getMethod();
        Class<?> clazz = method.getReturnType();

        if (clazz.isArray()) {
            type = clazz.getComponentType();

        } else if (Collection.class.isAssignableFrom(clazz) || Map.class.isAssignableFrom(clazz)) {
            type = method.getGenericReturnType();

        } else {
            type = clazz;
        }

        return type;
    }

    public ConfigurationItemUpdateResult doRequest() {
        ConfigurationItemUpdateResult.Builder itemUpdateBuilder;
        try {
            ConfigurationResponse configurationResponse = this.configurationSource.get(new ConfigurationItemId(this.configurationItemElement.getProduct(),
                                                                                                               this.configurationItemElement.getComponent(),
                                                                                                               this.configurationItemElement.getValue()));

            String rawValue = configurationResponse.getRawConfiguration();

            MethodReturnData methodData = new MethodReturnData(this.returnType, rawValue);
            itemUpdateBuilder = ConfigurationItemUpdateResult.Builder.update(this.configurationAdapter.adapt(methodData))
                                                                     .valueRead(rawValue)
                                                                     .product(this.configurationItemElement.getProduct())
                                                                     .component(this.configurationItemElement.getComponent())
                                                                     .item(this.configurationItemElement.getValue())
                                                                     .method(this.configurationItemElement.getMethod())
                                                                     .cast(this.configurationItemElement.getMethod().getReturnType())
                                                                     .from(ConfigurationItemUpdateResult.Source.server);

        } catch (Throwable throwable) {
            itemUpdateBuilder = ConfigurationItemUpdateResult.Builder.error(throwable)
                                                                     .product(this.configurationItemElement.getProduct())
                                                                     .component(this.configurationItemElement.getComponent())
                                                                     .item(this.configurationItemElement.getValue())
                                                                     .method(this.configurationItemElement.getMethod())
                                                                     .cast(this.configurationItemElement.getMethod().getReturnType())
                                                                     .from(ConfigurationItemUpdateResult.Source.server);
        }

        return itemUpdateBuilder.build();
    }

    private ConfigurationAdapter getRemoteAdapter(ConfigurationItemElement configurationItemElement) {
        try {
            Constructor<? extends ConfigurationAdapter> constructor = configurationItemElement.getAdapter().getConstructor(ArrayUtils.EMPTY_CLASS_ARRAY);
            return constructor.newInstance(ArrayUtils.EMPTY_OBJECT_ARRAY);
        } catch (Throwable t) {
            throw new IllegalArgumentException(msg.get("error.adapter"), t);
        }
    }
}

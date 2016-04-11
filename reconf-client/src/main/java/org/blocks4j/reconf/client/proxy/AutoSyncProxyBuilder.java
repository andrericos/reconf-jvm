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

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import org.blocks4j.reconf.client.config.update.ConfigurationRepositoryUpdater;
import org.blocks4j.reconf.infra.i18n.MessagesBundle;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class AutoSyncProxyBuilder {

    private static final MessagesBundle msg = MessagesBundle.getBundle(AutoSyncProxyBuilder.class);
    private Method method;
    private ConfigurationRepositoryUpdater updater;

    public AutoSyncProxyBuilder(Method method) {
        this.method = method;
    }

    public static AutoSyncProxyBuilder forMethod(Method method) {
        return new AutoSyncProxyBuilder(method);
    }

    public AutoSyncProxyBuilder updater(ConfigurationRepositoryUpdater updater) {
        this.updater = updater;
        return this;
    }

    public Object build() {
        Object configValue;
        Class<?> configurationItemType = this.method.getReturnType();
        if (!Modifier.isFinal(configurationItemType.getModifiers())) {
            Enhancer enhancer = new Enhancer();
            enhancer.setSuperclass(configurationItemType);
            enhancer.setCallback(new AutoSyncProxyInterceptor(this.method, this.updater));
            configValue = enhancer.create();
        } else {
            throw new IllegalArgumentException(msg.format("error.autosync.final.class", configurationItemType));
        }
        return configValue;
    }

    private static class AutoSyncProxyInterceptor implements MethodInterceptor {
        private final Method method;
        private final ConfigurationRepositoryUpdater updater;

        private AutoSyncProxyInterceptor(Method method, ConfigurationRepositoryUpdater updater) {
            this.method = method;
            this.updater = updater;
        }

        @Override
        public Object intercept(Object obj, Method objMethod, Object[] args, MethodProxy proxy) throws Throwable {
            Object configurationItem = this.updater.getValueOf(this.method);
            return objMethod.invoke(configurationItem, args);
        }
    }

}

package org.blocks4j.reconf.client.config.update;

import org.blocks4j.reconf.client.config.ConfigurationItemId;

@FunctionalInterface
public interface ConfigurationSource {

    ConfigurationResponse get(ConfigurationItemId configurationItemId);

}

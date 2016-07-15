package org.blocks4j.reconf.client.config;

public interface ConfigurationRepository {

    Object getValueOf(ConfigurationItemId configurationItemId);

    void update(ConfigurationItemId configurationItemId, Object value);

}

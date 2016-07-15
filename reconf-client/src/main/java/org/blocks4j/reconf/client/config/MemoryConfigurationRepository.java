package org.blocks4j.reconf.client.config;

import com.google.common.collect.Maps;

import java.util.Map;

public class MemoryConfigurationRepository implements ConfigurationRepository {

    private final Map<ConfigurationItemId, Object> repository;

    public MemoryConfigurationRepository() {
        this.repository = Maps.newConcurrentMap();
    }

    @Override
    public Object getValueOf(ConfigurationItemId configurationItemId) {
        return this.repository.get(configurationItemId);
    }

    @Override
    public void update(ConfigurationItemId configurationItemId, Object value) {
        this.repository.put(configurationItemId, value);
    }
}

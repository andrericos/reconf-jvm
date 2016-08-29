package org.blocks4j.reconf.client.config.listener;

import org.blocks4j.reconf.client.config.ConfigurationItemId;

public class ModificationEvent {

    private final ConfigurationItemId configurationItemId;
    private final String newValue;
    private final Object convertedValue;

    public ModificationEvent(ConfigurationItemId configurationItemId, String newValue, Object convertedValue) {
        this.configurationItemId = configurationItemId;
        this.newValue = newValue;
        this.convertedValue = convertedValue;
    }

    public ConfigurationItemId getConfigurationItemId() {
        return configurationItemId;
    }

    public String getNewValue() {
        return newValue;
    }

    public Object getConvertedValue() {
        return this.convertedValue;
    }
}

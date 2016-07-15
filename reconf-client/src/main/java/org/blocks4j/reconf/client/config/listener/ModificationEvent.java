package org.blocks4j.reconf.client.config.listener;

import org.blocks4j.reconf.client.config.ConfigurationItemId;

public class ModificationEvent {

    private final ConfigurationItemId configurationItemId;
    private final String newValue;

    public ModificationEvent(ConfigurationItemId configurationItemId, String newValue) {
        this.configurationItemId = configurationItemId;
        this.newValue = newValue;
    }

    public ConfigurationItemId getConfigurationItemId() {
        return configurationItemId;
    }

    public String getNewValue() {
        return newValue;
    }
}

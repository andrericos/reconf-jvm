package org.blocks4j.reconf.client.config.cache;

import org.blocks4j.reconf.client.config.ConfigurationItemId;

import java.io.Serializable;
import java.util.Date;

public class ChangeHistoryItem implements Serializable {

    private final Date date;

    private final ConfigurationItemId configurationItemId;

    private final String newValue;

    public ChangeHistoryItem(ConfigurationItemId configurationItemId, String newValue) {
        this.date = new Date();
        this.configurationItemId = configurationItemId;
        this.newValue = newValue;
    }

    public Date getDate() {
        return this.date;
    }

    public ConfigurationItemId getConfigurationItemId() {
        return this.configurationItemId;
    }

    public String getNewValue() {
        return this.newValue;
    }
}

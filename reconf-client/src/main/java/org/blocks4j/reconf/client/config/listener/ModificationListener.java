package org.blocks4j.reconf.client.config.listener;

import org.blocks4j.reconf.client.config.ConfigurationItemId;

public interface ModificationListener {

    void onChange(ModificationEvent modificationEvent);

    boolean isEnabled(ConfigurationItemId configurationItemId);

}

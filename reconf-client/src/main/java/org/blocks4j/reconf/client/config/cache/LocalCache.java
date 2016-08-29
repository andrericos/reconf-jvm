package org.blocks4j.reconf.client.config.cache;

import org.blocks4j.reconf.client.config.listener.ModificationListener;
import org.blocks4j.reconf.client.config.update.source.ConfigurationSource;
import org.blocks4j.reconf.infra.shutdown.ShutdownBean;

public interface LocalCache extends ConfigurationSource, ModificationListener, ShutdownBean {

}

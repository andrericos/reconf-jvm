package org.blocks4j.reconf.spring.boot.actuator;

import org.blocks4j.reconf.client.config.MemoryConfigurationRepository;
import org.blocks4j.reconf.client.setup.AbstractEnvironment;
import org.blocks4j.reconf.client.setup.config.ReconfConfiguration;

public class SpringBootEnvironment extends AbstractEnvironment {

    public SpringBootEnvironment(ReconfConfiguration reconfConfiguration) {
        super(reconfConfiguration, getMapDBConfigurationRepository());
    }

    private static MemoryConfigurationRepository getMapDBConfigurationRepository() {
        return new MemoryConfigurationRepository();
    }
}
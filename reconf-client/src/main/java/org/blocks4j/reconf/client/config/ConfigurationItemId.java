package org.blocks4j.reconf.client.config;

import org.blocks4j.reconf.client.customization.Customization;

import java.io.Serializable;
import java.util.Objects;

public class ConfigurationItemId implements Serializable{

    private final ConfigurationRepositoryId configurationRepositoryId;

    private final String name;

    public ConfigurationItemId(String product, String component, String name) {
        this(new ConfigurationRepositoryId(product, component), name);
    }

    public ConfigurationItemId(ConfigurationRepositoryId configurationRepositoryId, String name) {
        this.configurationRepositoryId = configurationRepositoryId;
        this.name = name;
    }

    public String getProduct() {
        return this.configurationRepositoryId.getProduct();
    }

    public String getComponent() {
        return this.configurationRepositoryId.getComponent();
    }

    public String getName() {
        return this.name;
    }

    public ConfigurationItemId applyCustomization(Customization customization) {
        return new ConfigurationItemId(this.configurationRepositoryId.applyCustomization(customization),
                                       customization.getCustomItem(this.name));
    }

    public ConfigurationItemId override(ConfigurationRepositoryId override) {
        return new ConfigurationItemId(this.configurationRepositoryId.override(override),
                                       this.name);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ConfigurationItemId that = (ConfigurationItemId) o;
        return Objects.equals(getProduct(), that.getProduct()) &&
               Objects.equals(getComponent(), that.getComponent()) &&
               Objects.equals(getName(), that.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getProduct(), getComponent(), getName());
    }

}

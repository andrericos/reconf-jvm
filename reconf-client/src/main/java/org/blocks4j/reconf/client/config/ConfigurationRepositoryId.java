package org.blocks4j.reconf.client.config;

import org.apache.commons.lang3.StringUtils;
import org.blocks4j.reconf.client.customization.Customization;

import java.io.Serializable;
import java.util.Objects;

public class ConfigurationRepositoryId implements Serializable {

    private final String product;

    private final String component;

    public ConfigurationRepositoryId(String product, String component) {
        this.product = product;
        this.component = component;
    }

    public String getProduct() {
        return this.product;
    }

    public String getComponent() {
        return this.component;
    }

    public ConfigurationRepositoryId applyCustomization(Customization customization) {
        return new ConfigurationRepositoryId(customization.getCustomProduct(this.product),
                                             customization.getCustomComponent(this.component));
    }

    public ConfigurationRepositoryId override(ConfigurationRepositoryId override) {
        return new ConfigurationRepositoryId(StringUtils.defaultIfBlank(this.product, override.getProduct()),
                                             StringUtils.defaultIfBlank(this.component, override.getComponent()));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ConfigurationRepositoryId that = (ConfigurationRepositoryId) o;
        return Objects.equals(getProduct(), that.getProduct()) &&
               Objects.equals(getComponent(), that.getComponent());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getProduct(), getComponent());
    }

}

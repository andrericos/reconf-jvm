package org.blocks4j.reconf.client.config;

import java.util.Objects;

public class ConfigurationItemId {

    private final String product;

    private final String component;

    private final String name;

    public ConfigurationItemId(String product, String component, String name) {
        this.product = product;
        this.component = component;
        this.name = name;
    }

    public String getProduct() {
        return this.product;
    }

    public String getComponent() {
        return this.component;
    }

    public String getName() {
        return this.name;
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

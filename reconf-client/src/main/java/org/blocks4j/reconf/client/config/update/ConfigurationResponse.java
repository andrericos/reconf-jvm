package org.blocks4j.reconf.client.config.update;

import org.blocks4j.reconf.client.config.ConfigurationItemId;

public class ConfigurationResponse {

    private final ConfigurationItemId configurationItemId;

    private final String rawConfiguration;

    private final SourceType source;

    private final boolean success;

    private ConfigurationResponse(boolean success, ConfigurationItemId configurationItemId, String rawConfiguration, SourceType source) {
        this.success = success;
        this.configurationItemId = configurationItemId;
        this.rawConfiguration = rawConfiguration;
        this.source = source;
    }

    public ConfigurationItemId getConfigurationItemId() {
        return this.configurationItemId;
    }

    public String getRawConfiguration() {
        return this.rawConfiguration;
    }

    public SourceType getSource() {
        return this.source;
    }

    public boolean isSuccess() {
        return this.success;
    }

    public boolean isFail() {
        return !this.isSuccess();
    }


    public static ConfigurationResponse createSuccessfulResponse(ConfigurationItemId configurationItemId, String rawConfiguration, SourceType sourteType) {
        return new ConfigurationResponse(true, configurationItemId, rawConfiguration, sourteType);
    }

    public static ConfigurationResponse createErrorResponse(ConfigurationItemId configurationItemId, SourceType sourteType, Exception exception) {
        String exceptionMessage = exception.getClass().getCanonicalName() + " " + exception.getMessage();

        return new ConfigurationResponse(false, configurationItemId, exceptionMessage, sourteType);
    }
}

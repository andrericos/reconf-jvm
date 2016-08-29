package org.blocks4j.reconf.client.config.update.source;

import org.blocks4j.reconf.client.config.ConfigurationItemId;
import org.blocks4j.reconf.infra.http.ReconfServer;

public class HttpConfigurationSource implements ConfigurationSource {

    private ReconfServer reconfServer;

    public HttpConfigurationSource(ReconfServer reconfServer) {
        this.reconfServer = reconfServer;
    }

    @Override
    public ConfigurationResponse get(ConfigurationItemId configurationItemId) {
        try {
            String rawConfiguration = this.reconfServer.get(configurationItemId.getProduct(),
                                                            configurationItemId.getComponent(),
                                                            configurationItemId.getName());

            return ConfigurationResponse.createSuccessfulResponse(configurationItemId,
                                                                  rawConfiguration,
                                                                  SourceType.REMOTE);
        } catch (Exception exception) {
            return ConfigurationResponse.createErrorResponse(configurationItemId,
                                                             SourceType.REMOTE,
                                                             exception);
        }
    }
}

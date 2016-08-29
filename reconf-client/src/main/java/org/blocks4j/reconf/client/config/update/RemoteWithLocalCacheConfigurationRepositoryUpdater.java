package org.blocks4j.reconf.client.config.update;

import org.blocks4j.reconf.client.config.ConfigurationRepository;
import org.blocks4j.reconf.client.config.update.requisitor.ConfigurationItemRequisitor;
import org.blocks4j.reconf.client.elements.ConfigurationItemElement;
import org.blocks4j.reconf.client.elements.ConfigurationRepositoryElement;
import org.blocks4j.reconf.client.setup.Environment;

import java.util.concurrent.CompletableFuture;

public class RemoteWithLocalCacheConfigurationRepositoryUpdater extends RemoteConfigurationRepositoryUpdater {

    public RemoteWithLocalCacheConfigurationRepositoryUpdater(Environment environment,
                                                              ConfigurationRepository repository,
                                                              ConfigurationRepositoryElement configurationRepositoryElement) {
        super(environment, repository, configurationRepositoryElement);
    }

    @Override
    protected CompletableFuture<ConfigurationItemUpdateResult> doAsyncRequest(Environment environment, ConfigurationItemElement configurationItemElement) {
        ConfigurationItemRequisitor localRequisitor = new ConfigurationItemRequisitor(configurationItemElement, environment.getLocalCacheSource());

        return super.doAsyncRequest(environment, configurationItemElement).thenApply(configurationItemUpdateResult -> {
            ConfigurationItemUpdateResult finalResult = configurationItemUpdateResult;
            if (configurationItemUpdateResult.isFailure()) {
                finalResult = localRequisitor.doRequest();
            }

            return finalResult;
        });
    }
}
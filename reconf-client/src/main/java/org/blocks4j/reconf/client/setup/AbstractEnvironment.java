package org.blocks4j.reconf.client.setup;

import org.blocks4j.reconf.client.config.ConfigurationRepository;
import org.blocks4j.reconf.client.config.update.ConfigurationResponse;
import org.blocks4j.reconf.client.config.update.ConfigurationSource;
import org.blocks4j.reconf.client.config.update.HttpConfigurationSource;
import org.blocks4j.reconf.client.config.update.SourceType;
import org.blocks4j.reconf.client.setup.config.ConnectionSettings;
import org.blocks4j.reconf.client.setup.config.ReconfConfiguration;
import org.blocks4j.reconf.infra.http.ReconfServerStub;
import org.blocks4j.reconf.infra.shutdown.ShutdownBean;
import org.blocks4j.reconf.infra.shutdown.ShutdownInterceptor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class AbstractEnvironment implements Environment {

    private List<ShutdownBean> managedShutdownObjects;
    private ConfigurationSource remoteSource;
    private ReconfConfiguration reconfConfiguration;
    private ConfigurationRepository repository;

    public AbstractEnvironment(ReconfConfiguration reconfConfiguration, ConfigurationRepository repository) {
        this.reconfConfiguration = reconfConfiguration;
        this.repository = repository;

        this.reset();
        new ShutdownInterceptor(this).register();
    }

    private void reset() {
        this.managedShutdownObjects = Collections.synchronizedList(new ArrayList<>());
    }

    @Override
    public ReconfConfiguration getReconfConfiguration() {
        return this.reconfConfiguration;
    }

    @Override
    public ConfigurationRepository getRepository() {
        return this.repository;
    }

    @Override
    public void manageShutdownObject(ShutdownBean shutdownBean) {
        if (shutdownBean != null) {
            this.managedShutdownObjects.add(shutdownBean);
        }
    }

    @Override
    public void shutdown() {
        this.managedShutdownObjects.forEach(ShutdownBean::shutdown);

        this.reset();
    }

    @Override
    public ConfigurationSource getLocalCacheSource() {
        return configurationItemId -> ConfigurationResponse.createErrorResponse(configurationItemId, SourceType.LOCAL, new Exception());
    }

    @Override
    public ConfigurationSource getRemoteSource() {
        ConnectionSettings connectionSettings = this.getReconfConfiguration().getConnectionSettings();

        if (this.remoteSource == null) {
            ReconfServerStub reconfServerStub = new ReconfServerStub(connectionSettings.getUrl(),
                                                                     connectionSettings.getTimeout(),
                                                                     connectionSettings.getTimeUnit(),
                                                                     connectionSettings.getMaxRetry());

            this.remoteSource = new HttpConfigurationSource(reconfServerStub);
            this.manageShutdownObject(reconfServerStub);
        }

        return this.remoteSource;
    }

    @Override
    public ConnectionSettings getConnectionSettings() {
        return this.getReconfConfiguration().getConnectionSettings();
    }
}
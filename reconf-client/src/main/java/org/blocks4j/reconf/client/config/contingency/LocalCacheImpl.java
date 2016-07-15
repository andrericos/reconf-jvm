package org.blocks4j.reconf.client.config.contingency;

import org.blocks4j.reconf.client.config.ConfigurationItemId;
import org.blocks4j.reconf.client.config.listener.ModificationEvent;
import org.blocks4j.reconf.client.config.update.ConfigurationResponse;
import org.blocks4j.reconf.client.config.update.SourceType;
import org.blocks4j.reconf.client.setup.config.LocalCacheSettings;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Serializer;

import java.io.File;
import java.util.Map;
import java.util.Objects;

public class LocalCacheImpl implements LocalCache {

    private static final String CONFIGURATION_ITEM_ID_KEY_FORMAT = "%s#%s##%s###";

    private final Map<String, String> repository;

    private final DB db;

    public LocalCacheImpl(LocalCacheSettings localCacheSettings) {
        this.db = this.createMapDB(localCacheSettings);
        this.repository = this.db.hashMap("repository", Serializer.STRING, Serializer.STRING)
                                 .createOrOpen();
    }

    private DB createMapDB(LocalCacheSettings localCacheSettings) {
        DBMaker.Maker dbMaker = DBMaker.fileDB(new File(localCacheSettings.getBackupLocation(), "reconfV4"))
                                       .fileMmapEnableIfSupported()
                                       .transactionEnable();

        if (localCacheSettings.isCompressed()) {
            //dbMaker.compressionEnable();
        }

        return dbMaker.make();
    }

    @Override
    public ConfigurationResponse get(ConfigurationItemId configurationItemId) {
        ConfigurationResponse configurationResponse;

        String configurationItemIdKey = this.createConfigurationItemIdKey(configurationItemId);
        String rawValue = this.repository.get(configurationItemIdKey);

        if (Objects.nonNull(rawValue)) {
            configurationResponse = ConfigurationResponse.createSuccessfulResponse(configurationItemId, rawValue, SourceType.LOCAL);
        } else {
            configurationResponse = ConfigurationResponse.createErrorResponse(configurationItemId, SourceType.LOCAL, new NullPointerException("Configuration not found on local cache"));
        }

        return configurationResponse;
    }

    @Override
    public void onChange(ModificationEvent modificationEvent) {
        String configurationItemIdKey = this.createConfigurationItemIdKey(modificationEvent.getConfigurationItemId());

        String newValue = modificationEvent.getNewValue();
        String oldValue = this.repository.get(configurationItemIdKey);

        if (!Objects.equals(oldValue, newValue)) {
            this.repository.put(configurationItemIdKey, newValue);
        }
    }

    @Override
    public boolean isEnabled(ConfigurationItemId configurationItemId) {
        return true;
    }

    private String createConfigurationItemIdKey(ConfigurationItemId configurationItemId) {
        return String.format(CONFIGURATION_ITEM_ID_KEY_FORMAT,
                             configurationItemId.getProduct(),
                             configurationItemId.getComponent(),
                             configurationItemId.getName());
    }

    @Override
    public void shutdown() {
        this.db.commit();
        this.db.close();
    }
}

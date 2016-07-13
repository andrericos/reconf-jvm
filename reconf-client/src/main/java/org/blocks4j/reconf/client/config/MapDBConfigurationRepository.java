package org.blocks4j.reconf.client.config;

import org.blocks4j.reconf.client.config.update.ConfigurationItemUpdateResult;
import org.blocks4j.reconf.client.config.update.ConfigurationResponse;
import org.blocks4j.reconf.client.config.update.ConfigurationSource;
import org.blocks4j.reconf.client.setup.config.LocalCacheSettings;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Serializer;

import java.io.File;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Objects;

public class MapDBConfigurationRepository implements ConfigurationRepository, ConfigurationSource {

    private final DB db;
    private Map<String, Object> repository;

    @SuppressWarnings("unchecked")
    public MapDBConfigurationRepository(LocalCacheSettings localCacheSettings) {
        this.db = this.createMapDB(localCacheSettings);
        this.repository = this.db.hashMap("repository", Serializer.STRING, Serializer.JAVA)
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
    public Object getValueOf(Method method) {
        return this.repository.get(this.getKey(method));
    }

    private String getKey(Method method) {
        return String.format("%s.%s", method.getDeclaringClass().getCanonicalName(), method.getName());
    }

    @Override
    public ConfigurationItemUpdateResult update(ConfigurationItemUpdateResult result) {
        Object lastValue;
        ConfigurationItemUpdateResult dbUpdateResult = result;

        if (result.isSuccess()) {
            Object currentValue = result.getObject();
            lastValue = this.repository.put(this.getKey(result.getMethod()), currentValue);

            if (Objects.equals(lastValue, currentValue)) {
                dbUpdateResult = ConfigurationItemUpdateResult.Builder.noChange()
                                                                      .product(result.getProduct())
                                                                      .component(result.getComponent())
                                                                      .item(result.getItem())
                                                                      .method(result.getMethod())
                                                                      .cast(result.getCast())
                                                                      .valueRead(result.getRawValue())
                                                                      .from(ConfigurationItemUpdateResult.Source.localCache)
                                                                      .build();
            }
        }

        this.db.commit();

        return dbUpdateResult;
    }

    @Override
    public void shutdown() {
        this.db.commit();
        this.db.close();
    }

    @Override
    public ConfigurationResponse get(ConfigurationItemId configurationItemId) {
        return null;
    }
}

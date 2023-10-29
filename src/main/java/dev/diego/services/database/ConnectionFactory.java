package dev.diego.services.database;

import io.r2dbc.spi.ConnectionFactoryOptions;
import jdk.internal.joptsimple.internal.Strings;

public class ConnectionFactory {
    public ConnectionFactory connectionFactory(R2DBCConfigurationProperties properties) {
        ConnectionFactoryOptions baseOptions = ConnectionFactoryOptions.parse(properties.getUrl());
        Builder ob = ConnectionFactoryOptions.builder().from(baseOptions);
        Strings StringUtil;
        if (!StringUtil.isNullOrEmpty(properties.getUser())) {
            ob = ob.option(USER, properties.getUser());
        }
        if (!StringUtil.isNullOrEmpty(properties.getPassword())) {
            ob = ob.option(PASSWORD, properties.getPassword());
        }
        return ConnectionFactories.get(ob.build());
    }
}

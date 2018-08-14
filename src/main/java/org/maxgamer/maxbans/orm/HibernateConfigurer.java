package org.maxgamer.maxbans.orm;

import org.hibernate.cfg.AvailableSettings;
import org.hibernate.cfg.Configuration;
import org.maxgamer.maxbans.config.JdbcConfig;
import org.maxgamer.maxbans.orm.id.UserAddressId;
import org.maxgamer.maxbans.util.dialect.MySQL80DialectResolver;

/**
 * @author Dirk Jamieson
 */
public class HibernateConfigurer {
    public static Configuration configuration(JdbcConfig jdbc) {
        Configuration config = new Configuration();

        config.addAnnotatedClass(User.class);
        config.addAnnotatedClass(Address.class);
        config.addAnnotatedClass(Ban.class);
        config.addAnnotatedClass(Mute.class);
        config.addAnnotatedClass(UserAddress.class);
        config.addAnnotatedClass(UserAddressId.class);
        config.addAnnotatedClass(Warning.class);

        config.setProperty("hibernate.connection.driver_class", jdbc.getDriver());
        config.setProperty("hibernate.connection.url", jdbc.getUrl());
        config.setProperty("hibernate.connection.username", jdbc.getUsername());
        config.setProperty("hibernate.connection.password", jdbc.getPassword());
        config.setProperty("hibernate.show_sql", String.valueOf(jdbc.isShowSql()));

        config.setProperty("hibernate.c3p0.timeout", "300");
        config.setProperty("hibernate.connection.provider_class", "org.hibernate.connection.C3P0ConnectionProvider");

        // This adds support for MySQL 8.0, until Hibernate makes it part of the standard
        config.setProperty(AvailableSettings.DIALECT_RESOLVERS, MySQL80DialectResolver.class.getCanonicalName());

        return config;
    }

    private HibernateConfigurer() {
        throw new RuntimeException("Not implemented");
    }
}

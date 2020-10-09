package io.github.cselagea.batch;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.autoconfigure.batch.BatchDataSource;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class DataSourceConfiguration {

    public static final String APP_DATA_SOURCE_CONFIG_PREFIX = "app.datasource";
    public static final String BATCH_DATA_SOURCE_CONFIG_PREFIX = "batch.datasource";

    @Bean
    @Primary
    @ConfigurationProperties(APP_DATA_SOURCE_CONFIG_PREFIX)
    public DataSourceProperties appDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @Primary
    @ConfigurationProperties(APP_DATA_SOURCE_CONFIG_PREFIX + ".configuration")
    public HikariDataSource appDataSource() {
        return appDataSourceProperties()
                .initializeDataSourceBuilder()
                .type(HikariDataSource.class)
                .build();
    }

    @Bean
    @ConfigurationProperties(BATCH_DATA_SOURCE_CONFIG_PREFIX)
    public DataSourceProperties batchDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @BatchDataSource
    @ConfigurationProperties(BATCH_DATA_SOURCE_CONFIG_PREFIX + ".configuration")
    public HikariDataSource batchDataSource() {
        return batchDataSourceProperties()
                .initializeDataSourceBuilder()
                .type(HikariDataSource.class)
                .build();
    }

}

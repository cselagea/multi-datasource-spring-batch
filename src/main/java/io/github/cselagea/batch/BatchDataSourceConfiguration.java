package io.github.cselagea.batch;

import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.batch.BatchDataSource;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

@Configuration
@Slf4j
public class BatchDataSourceConfiguration {

    public static final String BATCH_JOB_DATA_SOURCE_CONFIG_PREFIX = "batch.job.datasource";
    public static final String BATCH_FRAMEWORK_DATA_SOURCE_CONFIG_PREFIX = "batch.framework.datasource";

    @Bean
    @Primary
    @ConfigurationProperties(prefix = BATCH_JOB_DATA_SOURCE_CONFIG_PREFIX)
    public DataSource jobDataSource(@Value("${" + BATCH_JOB_DATA_SOURCE_CONFIG_PREFIX + ".url}") String jdbcUrl) {
        log.info("JDBC URL for the batch job-specific data source: " + jdbcUrl);

        return DataSourceBuilder.create()
                                .type(HikariDataSource.class)
                                .url(jdbcUrl)
                                .build();
    }

    @Bean
    @BatchDataSource
    @ConfigurationProperties(prefix = BATCH_FRAMEWORK_DATA_SOURCE_CONFIG_PREFIX)
    public DataSource frameworkDataSource(@Value("${" + BATCH_FRAMEWORK_DATA_SOURCE_CONFIG_PREFIX + ".url}") String jdbcUrl) {
        log.info("JDBC URL for the batch framework data source: " + jdbcUrl);

        return DataSourceBuilder.create()
                                .type(HikariDataSource.class)
                                .url(jdbcUrl)
                                .build();
    }

}

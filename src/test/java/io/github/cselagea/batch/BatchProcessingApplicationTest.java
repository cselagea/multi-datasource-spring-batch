package io.github.cselagea.batch;

import io.github.cselagea.batch.BatchProcessingApplicationTest.DataSourceConfigurationPropertiesInitializer;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.MSSQLServerContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.logging.LogManager;

import static io.github.cselagea.batch.BatchDataSourceConfiguration.BATCH_FRAMEWORK_DATA_SOURCE_CONFIG_PREFIX;
import static io.github.cselagea.batch.BatchDataSourceConfiguration.BATCH_JOB_DATA_SOURCE_CONFIG_PREFIX;

@SpringBootTest
@ContextConfiguration(initializers = DataSourceConfigurationPropertiesInitializer.class)
@Testcontainers
class BatchProcessingApplicationTest {

    private static final DockerImageName sqlServerImageName
            = DockerImageName.parse("mcr.microsoft.com/mssql/server:2019-CU6-ubuntu-16.04");

    @Container
    private static final JdbcDatabaseContainer batchJobSpecificSqlServerContainer
            = new MSSQLServerContainer(sqlServerImageName)
            .acceptLicense()
            .withInitScript("batch-job-schema-mssql.sql");

    @Container
    private static final JdbcDatabaseContainer batchFrameworkSqlServerContainer
            = new MSSQLServerContainer(sqlServerImageName)
            .acceptLicense();

    static {
        LogManager.getLogManager().reset(); // disable warning messages produced by JDBC driver trying to connect before server is ready
    }

    @Test
    void test() {
        // TODO
    }

    static class DataSourceConfigurationPropertiesInitializer
            implements ApplicationContextInitializer<ConfigurableApplicationContext> {

        @Override
        public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
            TestPropertyValues.of(
                    BATCH_FRAMEWORK_DATA_SOURCE_CONFIG_PREFIX + ".url=" + batchFrameworkSqlServerContainer.getJdbcUrl(),
                    BATCH_FRAMEWORK_DATA_SOURCE_CONFIG_PREFIX + ".username=" + batchFrameworkSqlServerContainer.getUsername(),
                    BATCH_FRAMEWORK_DATA_SOURCE_CONFIG_PREFIX + ".password=" + batchFrameworkSqlServerContainer.getPassword(),
                    BATCH_JOB_DATA_SOURCE_CONFIG_PREFIX + ".url=" + batchJobSpecificSqlServerContainer.getJdbcUrl(),
                    BATCH_JOB_DATA_SOURCE_CONFIG_PREFIX + ".username=" + batchJobSpecificSqlServerContainer.getUsername(),
                    BATCH_JOB_DATA_SOURCE_CONFIG_PREFIX + ".password=" + batchJobSpecificSqlServerContainer.getPassword()
            ).applyTo(configurableApplicationContext.getEnvironment());
        }

    }

}

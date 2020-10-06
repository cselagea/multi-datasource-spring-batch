package io.github.cselagea.batch;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.MSSQLServerContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.List;
import java.util.logging.LogManager;

import static io.github.cselagea.batch.BatchDataSourceConfiguration.BATCH_FRAMEWORK_DATA_SOURCE_CONFIG_PREFIX;
import static io.github.cselagea.batch.BatchDataSourceConfiguration.BATCH_JOB_DATA_SOURCE_CONFIG_PREFIX;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

@SpringBatchTest
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = BatchProcessingApplication.class)
@Testcontainers
class BatchProcessingApplicationTest {

    private static final DockerImageName sqlServerImageName
            = DockerImageName.parse("mcr.microsoft.com/mssql/server:2019-CU6-ubuntu-16.04");

    @Container
    private static final JdbcDatabaseContainer<?> batchJobSpecificSqlServerContainer
            = new MSSQLServerContainer<>(sqlServerImageName)
            .acceptLicense()
            .withInitScript("batch-job-schema-mssql.sql");

    @Container
    private static final JdbcDatabaseContainer<?> batchFrameworkSqlServerContainer
            = new MSSQLServerContainer<>(sqlServerImageName)
            .acceptLicense()
            .withInitScript("org/springframework/batch/core/schema-sqlserver.sql");

    static {
        LogManager.getLogManager().reset(); // disable warning messages produced by JDBC driver trying to connect before server is ready
    }

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @DynamicPropertySource
    static void registerDataSourceProperties(DynamicPropertyRegistry registry) {
        registry.add(BATCH_FRAMEWORK_DATA_SOURCE_CONFIG_PREFIX + ".url", batchFrameworkSqlServerContainer::getJdbcUrl);
        registry.add(BATCH_FRAMEWORK_DATA_SOURCE_CONFIG_PREFIX + ".username", batchFrameworkSqlServerContainer::getUsername);
        registry.add(BATCH_FRAMEWORK_DATA_SOURCE_CONFIG_PREFIX + ".password", batchFrameworkSqlServerContainer::getPassword);
        registry.add(BATCH_JOB_DATA_SOURCE_CONFIG_PREFIX + ".url", batchJobSpecificSqlServerContainer::getJdbcUrl);
        registry.add(BATCH_JOB_DATA_SOURCE_CONFIG_PREFIX + ".username", batchJobSpecificSqlServerContainer::getUsername);
        registry.add(BATCH_JOB_DATA_SOURCE_CONFIG_PREFIX + ".password", batchJobSpecificSqlServerContainer::getPassword);
    }

    @Test
    void jobShouldPersistUppercaseNames() throws Exception {
        JobExecution jobExecution = jobLauncherTestUtils.launchJob();

        assertThat(jobExecution.getExitStatus().getExitCode()).isEqualTo("COMPLETED");

        List<Person> personList = jdbcTemplate.query("SELECT first_name, last_name FROM people",
                (resultSet, row) -> new Person(
                        resultSet.getString(1),
                        resultSet.getString(2))
        );

        assertThat(personList).extracting("firstName", "lastName")
                              .containsExactly(
                                      tuple("JILL", "DOE"),
                                      tuple("JOE", "DOE"),
                                      tuple("JUSTIN", "DOE"),
                                      tuple("JANE", "DOE"),
                                      tuple("JOHN", "DOE")
                              );
    }

}

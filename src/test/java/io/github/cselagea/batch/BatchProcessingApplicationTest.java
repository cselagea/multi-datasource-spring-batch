package io.github.cselagea.batch;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.List;

import static io.github.cselagea.batch.DataSourceConfiguration.APP_DATA_SOURCE_CONFIG_PREFIX;
import static io.github.cselagea.batch.DataSourceConfiguration.BATCH_DATA_SOURCE_CONFIG_PREFIX;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

@SpringBatchTest
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = BatchProcessingApplication.class, initializers = ConfigFileApplicationContextInitializer.class)
@Testcontainers
class BatchProcessingApplicationTest {

    private static final DockerImageName postgresImageName = DockerImageName.parse("postgres:alpine");

    @Container
    private static final JdbcDatabaseContainer<?> appDatabaseContainer
            = new PostgreSQLContainer<>(postgresImageName)
            .withUsername("db1_user")
            .withPassword("pass");

    @Container
    private static final JdbcDatabaseContainer<?> batchDatabaseContainer
            = new PostgreSQLContainer<>(postgresImageName)
            .withUsername("db2_user")
            .withPassword("pass");

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @DynamicPropertySource
    static void registerDataSourceProperties(DynamicPropertyRegistry registry) {
        registry.add(BATCH_DATA_SOURCE_CONFIG_PREFIX + ".url", batchDatabaseContainer::getJdbcUrl);
        registry.add(BATCH_DATA_SOURCE_CONFIG_PREFIX + ".username", batchDatabaseContainer::getUsername);
        registry.add(BATCH_DATA_SOURCE_CONFIG_PREFIX + ".password", batchDatabaseContainer::getPassword);
        registry.add(APP_DATA_SOURCE_CONFIG_PREFIX + ".url", appDatabaseContainer::getJdbcUrl);
        registry.add(APP_DATA_SOURCE_CONFIG_PREFIX + ".username", appDatabaseContainer::getUsername);
        registry.add(APP_DATA_SOURCE_CONFIG_PREFIX + ".password", appDatabaseContainer::getPassword);
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

# multi-datasource-spring-batch

This project demonstrates a Spring Batch job that connects to one data source for Spring Batch metadata (i.e. job and step execution data) and another data source for the business data.

The batch job itself is based on [Getting Started - Creating a Batch Service](https://spring.io/guides/gs/batch-processing/).

Testcontainers (requires Docker installation) is used to create two PostgreSQL instances (one per data source) for real database testing.

Execute `mvn test` to run the test, which will launch the batch job and verify that the batch job inserted the expected business data.

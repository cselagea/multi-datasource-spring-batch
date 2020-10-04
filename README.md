# multi-datasource-spring-batch

This project demonstrates a Spring Batch job that connects to one data source for Spring Batch data (i.e. job and step execution data) and another data source for the business data.

Testcontainers is used to create two SQL Server instances (one per data source) for real database testing.

The batch job itself is based on [Getting Started - Creating a Batch Service](https://spring.io/guides/gs/batch-processing/).

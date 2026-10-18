package com.cn.hotelDemo.integration;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;

public abstract class BaseIntegrationTest {

    public static final MySQLContainer<?> mysql;
    public static final GenericContainer<?> redis;
    public static final boolean DOCKER_AVAILABLE;

    static {
        boolean available = false;
        try {
            available = DockerClientFactory.instance().isDockerAvailable();
        } catch (Throwable t) {
            available = false;
        }
        DOCKER_AVAILABLE = available;

        if (DOCKER_AVAILABLE) {
            MySQLContainer<?> m = new MySQLContainer<>("mysql:8.0")
                    .withDatabaseName("testdb")
                    .withUsername("test")
                    .withPassword("test");
            m.start();
            mysql = m;

            GenericContainer<?> r = new GenericContainer<>("redis:7.0")
                    .withExposedPorts(6379);
            r.start();
            redis = r;
        } else {
            mysql = null;
            redis = null;
        }
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        if (DOCKER_AVAILABLE && mysql != null && redis != null) {
            registry.add("spring.datasource.url", mysql::getJdbcUrl);
            registry.add("spring.datasource.username", mysql::getUsername);
            registry.add("spring.datasource.password", mysql::getPassword);
            registry.add("spring.datasource.driver-class-name", () -> "com.mysql.cj.jdbc.Driver");
            registry.add("spring.data.redis.host", redis::getHost);
            registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
            registry.add("spring.cache.type", () -> "redis");
        }
    }
}

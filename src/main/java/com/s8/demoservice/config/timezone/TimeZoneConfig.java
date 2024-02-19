package com.s8.demoservice.config.timezone;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

@Configuration
@ComponentScan
public class TimeZoneConfig implements WebMvcConfigurer {
    @Autowired
    private DataSource dataSource;

    @PostConstruct
    public void init() {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("SET TIME ZONE 'Asia/Jakarta'");
        } catch (SQLException e) {
            // Handle any exceptions
        }
    }
}

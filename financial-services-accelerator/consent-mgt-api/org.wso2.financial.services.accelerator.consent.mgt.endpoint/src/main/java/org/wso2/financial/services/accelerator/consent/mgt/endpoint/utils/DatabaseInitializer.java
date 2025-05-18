package org.wso2.financial.services.accelerator.consent.mgt.endpoint.utils;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.wso2.financial.services.accelerator.consent.mgt.dao.util.DatabaseUtils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.Statement;
import java.util.stream.Collectors;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * Database initializer for the consent management service.
 */
@SuppressFBWarnings(value = "SQL_INJECTION_JDBC", justification =
        "SQL file is static and trusted; no user input is used.")
public class DatabaseInitializer implements ServletContextListener {

    private static final String SQL_FILE_PATH = "/WEB-INF/schema.sql"; // Place schema.sql inside WEB-INF

    @SuppressFBWarnings("SQL_NONCONSTANT_STRING_PASSED_TO_EXECUTE")
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        try (Connection conn = DatabaseUtils.getDBConnection();
             Statement stmt = conn.createStatement()) {

            // Read the SQL file from resources
            InputStream inputStream = sce.getServletContext().getResourceAsStream(SQL_FILE_PATH);
            if (inputStream == null) {
                throw new RuntimeException("SQL file not found: " + SQL_FILE_PATH);
            }

            String sql = new BufferedReader(new InputStreamReader(inputStream))
                    .lines()
                    .collect(Collectors.joining("\n"));

            // Execute SQL statements
            for (String query : sql.split(";")) { // Split statements by semicolon
                if (!query.trim().isEmpty()) {
                    stmt.execute(query.trim());
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("Database initialization failed.", e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // Cleanup logic if needed
    }
}


package com.lamdayne.humify.auth.security.rls;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.datasource.DelegatingDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

@Slf4j
public class RlsDataSourceWrapper extends DelegatingDataSource {

    public RlsDataSourceWrapper(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public Connection getConnection() throws SQLException {
        Connection connection = super.getConnection();
        applyCompanyContext(connection);
        return connection;
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        Connection connection = super.getConnection(username, password);
        applyCompanyContext(connection);
        return connection;
    }

    public void applyCompanyContext(Connection connection) throws SQLException {
        Long companyId = CompanyContext.getCompanyId();
        if (companyId != null) {
            try (Statement stmt = connection.createStatement()) {
                log.info("Applying CompanyId: {}", companyId);
                stmt.execute(String.format("SET app.company_id = '%d'", companyId));
            }
        }
    }

}

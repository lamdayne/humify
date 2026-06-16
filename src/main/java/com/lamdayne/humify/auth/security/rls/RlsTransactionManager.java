package com.lamdayne.humify.auth.security.rls;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.TransactionDefinition;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Custom JPA TransactionManager that applies RLS context (company_id and is_admin)
 * at the transaction level using SET LOCAL.
 *
 * SET LOCAL ensures the settings only last for the duration of the current transaction,
 * so they are automatically cleaned up when the transaction commits or rolls back.
 */
@Slf4j
public class RlsTransactionManager extends JpaTransactionManager {

    @Override
    protected void doBegin(Object transaction, TransactionDefinition definition) {
        super.doBegin(transaction, definition);
        applyRlsContext();
    }

    private void applyRlsContext() {
        DataSource dataSource = getDataSource();
        if (dataSource == null) {
            return;
        }

        // Get the connection that is bound to the current transaction
        Connection connection = DataSourceUtils.getConnection(dataSource);
        try (Statement stmt = connection.createStatement()) {

            boolean isAdmin = CompanyContext.isAdmin();
            Long companyId = CompanyContext.getCompanyId();

            if (isAdmin) {
                log.debug("RLS: Setting is_admin = true (transaction-level)");
                stmt.execute("SET LOCAL app.is_admin = 'true'");
            } else {
                stmt.execute("SET LOCAL app.is_admin = 'false'");
            }

            if (companyId != null) {
                log.debug("RLS: Setting company_id = {} (transaction-level)", companyId);
                stmt.execute(String.format("SET LOCAL app.company_id = '%d'", companyId));
            }

        } catch (SQLException e) {
            log.error("Failed to apply RLS context", e);
            throw new RuntimeException("Failed to apply RLS context", e);
        } finally {
            // Do NOT release the connection — it is managed by the transaction
        }
    }

}

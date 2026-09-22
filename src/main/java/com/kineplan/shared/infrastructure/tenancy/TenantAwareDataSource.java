package com.kineplan.shared.infrastructure.tenancy;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.UUID;
import java.util.logging.Logger;
import javax.sql.DataSource;
import org.springframework.jdbc.datasource.AbstractDataSource;

public class TenantAwareDataSource extends AbstractDataSource {
    private static final UUID PLATFORM_TENANT = new UUID(0, 0);
    private final DataSource delegate;

    public TenantAwareDataSource(DataSource delegate) {
        this.delegate = delegate;
    }

    @Override
    public Connection getConnection() throws SQLException {
        return configure(delegate.getConnection());
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return configure(delegate.getConnection(username, password));
    }

    private Connection configure(Connection connection) throws SQLException {
        UUID tenant = TenantContext.getOrNull();
        UUID current = tenant == null ? PLATFORM_TENANT : tenant;
        try (var statement = connection.createStatement()) {
            statement.execute("select set_config('app.current_cabinet_id', '" + current + "', false)");
        }
        return connection;
    }

    @Override
    public PrintWriter getLogWriter() {
        try {
            return delegate.getLogWriter();
        } catch (SQLException exception) {
            throw new IllegalStateException("Unable to get log writer", exception);
        }
    }

    @Override
    public void setLogWriter(PrintWriter out) throws SQLException {
        delegate.setLogWriter(out);
    }

    @Override
    public void setLoginTimeout(int seconds) throws SQLException {
        delegate.setLoginTimeout(seconds);
    }

    @Override
    public int getLoginTimeout() throws SQLException {
        return delegate.getLoginTimeout();
    }

    @Override
    public Logger getParentLogger() {
        return Logger.getLogger(TenantAwareDataSource.class.getName());
    }
}
package com.kineplan.shared.infrastructure.tenancy;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.UUID;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.containers.PostgreSQLContainer;

@Testcontainers
class TenantRlsIntegrationTest {
    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("kineplan")
            .withUsername("kineplan")
            .withPassword("kineplan");

    @BeforeAll
    static void migrate() {
        Flyway.configure()
                .dataSource(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword())
                .load()
                .migrate();
    }

    @Test
    void cabinetCannotReadOrCreatePatientsForAnotherCabinet() throws Exception {
        UUID cabinetA = UUID.randomUUID();
        UUID cabinetB = UUID.randomUUID();
        UUID patientA = UUID.randomUUID();

        try (Connection connection = connection()) {
            setTenant(connection, cabinetA);
            insertCabinet(connection, cabinetA);
            insertPatient(connection, patientA, cabinetA);

            setTenant(connection, cabinetB);
            insertCabinet(connection, cabinetB);
            assertThat(countPatients(connection)).isZero();
            assertThat(insertPatient(connection, UUID.randomUUID(), cabinetA)).isFalse();
        }

        try (Connection connection = connection()) {
            setTenant(connection, cabinetA);
            assertThat(countPatients(connection)).isEqualTo(1);
        }
    }

    @Test
    void globalCabinetAndMembershipTablesAreNotTenantFiltered() throws Exception {
        UUID cabinetA = UUID.randomUUID();
        UUID cabinetB = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        try (Connection connection = connection()) {
            setTenant(connection, cabinetA);
            insertCabinet(connection, cabinetA);
            insertCabinet(connection, cabinetB);
            insertUser(connection, userId);
            insertMembership(connection, userId, cabinetA);
            insertMembership(connection, userId, cabinetB);

            setTenant(connection, cabinetB);
            assertThat(countCabinets(connection)).isEqualTo(2);
            assertThat(countMemberships(connection, userId)).isEqualTo(2);
        }
    }

    private Connection connection() throws Exception {
        return DriverManager.getConnection(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword());
    }

    private void setTenant(Connection connection, UUID cabinetId) throws Exception {
        try (var statement = connection.createStatement()) {
            statement.execute("select set_config('app.current_cabinet_id', '" + cabinetId + "', false)");
        }
    }

    private void insertCabinet(Connection connection, UUID cabinetId) throws Exception {
        try (var statement = connection.prepareStatement(
                "insert into cabinets (id, name, status, subscription_plan) values (?, 'Cabinet test', 'ACTIF', 'ESSENTIAL')")) {
            statement.setObject(1, cabinetId);
            statement.executeUpdate();
        }
    }

    private boolean insertPatient(Connection connection, UUID patientId, UUID cabinetId) throws Exception {
        try (var statement = connection.prepareStatement(
                "insert into patients (id, cabinet_id, first_name, last_name) values (?, ?, 'Test', 'Patient')")) {
            statement.setObject(1, patientId);
            statement.setObject(2, cabinetId);
            statement.executeUpdate();
            return true;
        } catch (java.sql.SQLException exception) {
            return false;
        }
    }

    private int countPatients(Connection connection) throws Exception {
        try (var statement = connection.createStatement(); var result = statement.executeQuery("select count(*) from patients")) {
            result.next();
            return result.getInt(1);
        }
    }

    private void insertUser(Connection connection, UUID userId) throws Exception {
        try (var statement = connection.prepareStatement(
                "insert into users (id, email, password_hash, first_name, last_name, status) "
                        + "values (?, 'test@example.com', 'hash', 'Test', 'User', 'ACTIVE')")) {
            statement.setObject(1, userId);
            statement.executeUpdate();
        }
    }

    private void insertMembership(Connection connection, UUID userId, UUID cabinetId) throws Exception {
        try (var statement = connection.prepareStatement(
                "insert into memberships (id, user_id, cabinet_id, role, status) "
                        + "values (?, ?, ?, 'ADMIN', 'ACTIVE')")) {
            statement.setObject(1, UUID.randomUUID());
            statement.setObject(2, userId);
            statement.setObject(3, cabinetId);
            statement.executeUpdate();
        }
    }

    private int countCabinets(Connection connection) throws Exception {
        try (var statement = connection.createStatement(); var result = statement.executeQuery("select count(*) from cabinets")) {
            result.next();
            return result.getInt(1);
        }
    }

    private int countMemberships(Connection connection, UUID userId) throws Exception {
        try (var statement = connection.prepareStatement("select count(*) from memberships where user_id = ?")) {
            statement.setObject(1, userId);
            try (var result = statement.executeQuery()) {
                result.next();
                return result.getInt(1);
            }
        }
    }
}
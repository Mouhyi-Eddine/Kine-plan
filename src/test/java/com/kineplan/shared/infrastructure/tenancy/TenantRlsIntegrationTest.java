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
}
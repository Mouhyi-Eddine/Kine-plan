package com.kineplan.shared.infrastructure.dev;

import java.time.Instant;
import java.sql.Timestamp;
import java.util.UUID;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Profile("dev")
public class DevDataSeeder implements CommandLineRunner {
    static final UUID CABINET_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    static final UUID USER_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    static final UUID MEMBERSHIP_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
    private static final String EMAIL = "admin@kineplan.local";
    private static final String PASSWORD = "ChangeMe!2026";

    private final JdbcTemplate jdbcTemplate;
    private final PasswordEncoder passwordEncoder;

    public DevDataSeeder(JdbcTemplate jdbcTemplate, PasswordEncoder passwordEncoder) {
        this.jdbcTemplate = jdbcTemplate;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        Instant now = Instant.now();
        jdbcTemplate.update("""
                INSERT INTO cabinets (id, name, address, timezone, status, subscription_plan)
                VALUES (?, 'Cabinet Démo Kine-plan', '1 rue de la Santé, Paris', 'Europe/Paris', 'ACTIF', 'ESSENTIAL')
                ON CONFLICT (id) DO NOTHING
                """, CABINET_ID);
        jdbcTemplate.update("""
                INSERT INTO users (id, email, password_hash, first_name, last_name, platform_admin, status)
                VALUES (?, ?, ?, 'Admin', 'Démo', false, 'ACTIVE')
                ON CONFLICT (email) DO NOTHING
                """, USER_ID, EMAIL, passwordEncoder.encode(PASSWORD));
        jdbcTemplate.update("""
                INSERT INTO memberships (id, user_id, cabinet_id, role, status, accepted_at)
                VALUES (?, ?, ?, 'ADMIN', 'ACTIVE', ?)
                ON CONFLICT (user_id, cabinet_id) DO NOTHING
                """, MEMBERSHIP_ID, USER_ID, CABINET_ID, Timestamp.from(now));
    }
}
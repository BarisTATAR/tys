package com.tys.config;

import org.hibernate.SessionFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import jakarta.persistence.EntityManagerFactory;

@Component
@Order(0)
public class SchemaBootstrap implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;
    private final EntityManagerFactory entityManagerFactory;

    public SchemaBootstrap(JdbcTemplate jdbcTemplate, EntityManagerFactory entityManagerFactory) {
        this.jdbcTemplate = jdbcTemplate;
        this.entityManagerFactory = entityManagerFactory;
    }

    @Override
    public void run(ApplicationArguments args) {
        jdbcTemplate.execute("CREATE SCHEMA IF NOT EXISTS tys");
        boolean hasReservations = tableExists("reservation");
        if (!hasReservations) {
            entityManagerFactory.unwrap(SessionFactory.class).getSchemaManager().exportMappedObjects(true);
            return;
        }
        ensureRuntimeTables();
        entityManagerFactory.unwrap(SessionFactory.class).getSchemaManager().validateMappedObjects();
    }

    private void ensureRuntimeTables() {
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS tys.revoked_token (
                    token_hash varchar(64) PRIMARY KEY,
                    expires_at timestamp NOT NULL
                )
                """);
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS tys.login_lock (
                    username varchar(120) PRIMARY KEY,
                    failures integer NOT NULL,
                    locked_until timestamp NULL
                )
                """);
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS tys.kbs_notice (
                    id bigserial PRIMARY KEY,
                    reservation_id bigint NOT NULL,
                    message varchar(2000) NOT NULL,
                    created_at timestamp NOT NULL
                )
                """);
    }

    private boolean tableExists(String table) {
        Integer count = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*) FROM information_schema.tables
                WHERE table_schema = 'tys' AND table_name = ?
                """,
                Integer.class,
                table);
        return count != null && count > 0;
    }
}

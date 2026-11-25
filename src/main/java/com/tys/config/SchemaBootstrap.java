package com.tys.config;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.SessionFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.core.io.ClassPathResource;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import jakarta.persistence.EntityManagerFactory;

import java.nio.charset.StandardCharsets;

/**
 * Şema yaşam döngüsü:
 * <ul>
 *   <li>Flyway (db/migration) uygulama açılmadan önce mevcut veritabanını en güncel sürüme dönüştürür.
 *       Geçişler tablo varlığını denetleyerek yazılır; boş veritabanında hiçbir şey yapmaz.</li>
 *   <li>Boş veritabanında tablolar burada entity tanımlarından kurulur.</li>
 *   <li>Dolu veritabanı entity tanımlarıyla doğrulanır; uyumsuzlukta uygulama açılmaz.</li>
 * </ul>
 * Entity'de şema değişikliği yapan her geliştirme, eski veritabanları için yeni bir V{n}__*.sql geçişi eklemelidir.
 */
@Slf4j
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
            // Şema Flyway/yukarıdaki komutla oluşturuldu; yalnız tablolar kurulur
            entityManagerFactory.unwrap(SessionFactory.class).getSchemaManager().exportMappedObjects(false);
        } else {
            entityManagerFactory.unwrap(SessionFactory.class).getSchemaManager().validateMappedObjects();
        }
        ensureIndexes();
    }

    private void ensureIndexes() {
        try (var in = new ClassPathResource("db/indexes.sql").getInputStream()) {
            // Yorum satırları bölmeden önce atılır (yorumdaki ';' ifadeyi bölmesin)
            String script = new String(in.readAllBytes(), StandardCharsets.UTF_8).lines()
                    .filter(line -> !line.trim().startsWith("--"))
                    .collect(java.util.stream.Collectors.joining("\n"));
            for (String statement : script.split(";")) {
                String sql = statement.trim();
                if (sql.isEmpty()) {
                    continue;
                }
                try {
                    jdbcTemplate.execute(sql);
                } catch (Exception ex) {
                    log.warn("İndeks oluşturulamadı ({}): {}", sql, ex.getMessage());
                }
            }
        } catch (Exception ex) {
            log.warn("İndeks betiği okunamadı: {}", ex.getMessage());
        }
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

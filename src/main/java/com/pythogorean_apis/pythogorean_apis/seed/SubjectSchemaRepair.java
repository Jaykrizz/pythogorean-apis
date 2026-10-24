package com.pythogorean_apis.pythogorean_apis.seed;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class SubjectSchemaRepair {

    @Bean
    ApplicationRunner repairSubjectOwnership(JdbcTemplate jdbc) {
        return args -> {
            if (!columnExists(jdbc, "subject", "class_room_id")) {
                return;
            }

            jdbc.execute("ALTER TABLE subject MODIFY class_room_id BIGINT NULL");

            if (columnExists(jdbc, "subject", "branch_id")) {
                jdbc.update("""
                        UPDATE subject s
                        JOIN class_room c ON s.class_room_id = c.id
                        SET s.branch_id = c.branch_id
                        WHERE s.branch_id IS NULL AND s.class_room_id IS NOT NULL
                        """);
            }

            if (columnExists(jdbc, "class_subject", "subject_id")) {
                jdbc.update("""
                        INSERT INTO class_subject (class_room_id, subject_id)
                        SELECT s.class_room_id, s.id
                        FROM subject s
                        WHERE s.class_room_id IS NOT NULL
                        AND NOT EXISTS (
                            SELECT 1 FROM class_subject cs
                            WHERE cs.class_room_id = s.class_room_id AND cs.subject_id = s.id
                        )
                        """);
            }
        };
    }

    private static boolean columnExists(JdbcTemplate jdbc, String table, String column) {
        Integer count = jdbc.queryForObject(
                """
                SELECT COUNT(*) FROM information_schema.COLUMNS
                WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ? AND COLUMN_NAME = ?
                """,
                Integer.class,
                table,
                column);
        return count != null && count > 0;
    }
}

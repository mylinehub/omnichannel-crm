package com.mylinehub.crm.rag.initialize;

import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

@Component
public class PgVectorChecker {

    private final DataSource dataSource;

    public PgVectorChecker(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public boolean isPgVectorInstalled() {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM pg_extension WHERE extname = 'vector'")) {

            boolean installed = rs.next();
            System.out.println("[pgvector-checker] vector extension installed? " + installed);
            return installed;

        } catch (Exception e) {
            System.out.println("[pgvector-checker] ERROR checking pgvector: " + e.getMessage());
            return false;
        }
    }

    public void installPgVectorIfMissing() {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute("CREATE EXTENSION IF NOT EXISTS vector");
            System.out.println("[pgvector-checker] vector extension installed successfully");

        } catch (Exception e) {
            System.out.println("[pgvector-checker] Failed to install vector extension: " + e.getMessage());
        }
    }
}

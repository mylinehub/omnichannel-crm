package com.mylinehub.crm.rag.initialize;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class PgvectorInstaller implements ApplicationRunner {

    private final PgVectorChecker pgVectorChecker;

    public PgvectorInstaller(PgVectorChecker pgVectorChecker) {
        this.pgVectorChecker = pgVectorChecker;
    }

    @Override
    public void run(ApplicationArguments args) {
        System.out.println("[pgvector-installer] Starting vector extension check...");

        boolean isInstalled = pgVectorChecker.isPgVectorInstalled();

        if (!isInstalled) {
            System.out.println("[pgvector-installer] pgvector is NOT installed. Attempting installation...");
            pgVectorChecker.installPgVectorIfMissing();
        } else {
            System.out.println("[pgvector-installer] pgvector is already installed.");
        }
    }
}

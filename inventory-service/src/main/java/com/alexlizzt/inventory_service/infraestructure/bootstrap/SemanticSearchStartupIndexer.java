package com.alexlizzt.inventory_service.infraestructure.bootstrap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import com.alexlizzt.inventory_service.application.usecase.ReindexProductsUseCase;

/**
 * Reindexa el catálogo de productos en el motor de búsqueda semántica al arrancar.
 *
 * Se ejecuta en segundo plano (best-effort) para no retrasar ni bloquear el
 * arranque del servicio, y reintenta porque el AI Service puede tardar en
 * estar disponible. La indexación es idempotente (upsert), por lo que
 * reintentar el lote completo es seguro.
 */
@Component
public class SemanticSearchStartupIndexer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(SemanticSearchStartupIndexer.class);

    private final ReindexProductsUseCase reindexProductsUseCase;
    private final boolean enabled;
    private final int maxAttempts;
    private final long retryDelayMs;

    public SemanticSearchStartupIndexer(
            ReindexProductsUseCase reindexProductsUseCase,
            @Value("${app.semantic-search.reindex-on-startup:true}") boolean enabled,
            @Value("${app.semantic-search.max-attempts:5}") int maxAttempts,
            @Value("${app.semantic-search.retry-delay-ms:5000}") long retryDelayMs) {
        this.reindexProductsUseCase = reindexProductsUseCase;
        this.enabled = enabled;
        this.maxAttempts = maxAttempts;
        this.retryDelayMs = retryDelayMs;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!enabled) {
            log.info("Reindexado semántico al arrancar deshabilitado.");
            return;
        }

        Thread worker = new Thread(this::reindexWithRetries, "semantic-startup-indexer");
        worker.setDaemon(true);
        worker.start();
    }

    /**
     * Ejecuta el reindexado con reintentos. Expuesto para pruebas.
     */
    public void reindexWithRetries() {
        int attempts = Math.max(1, maxAttempts);
        for (int attempt = 1; attempt <= attempts; attempt++) {
            try {
                int indexed = reindexProductsUseCase.execute();
                log.info("Reindexado semántico al arrancar completado: {} producto(s) indexado(s).", indexed);
                return;
            } catch (Exception e) {
                log.warn("Intento {}/{} de reindexado semántico falló: {}", attempt, attempts, e.getMessage());
                if (attempt < attempts) {
                    sleep(retryDelayMs);
                } else {
                    log.error("No se pudo completar el reindexado semántico al arrancar tras {} intento(s).", attempts, e);
                }
            }
        }
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

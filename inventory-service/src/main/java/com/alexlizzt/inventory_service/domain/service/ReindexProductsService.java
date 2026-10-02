package com.alexlizzt.inventory_service.domain.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alexlizzt.inventory_service.application.usecase.ReindexProductsUseCase;
import com.alexlizzt.inventory_service.domain.model.Product;
import com.alexlizzt.inventory_service.domain.port.AiServicePort;
import com.alexlizzt.inventory_service.domain.repository.ProductRepository;

@Service
public class ReindexProductsService implements ReindexProductsUseCase {

    private static final Logger log = LoggerFactory.getLogger(ReindexProductsService.class);

    private final ProductRepository productRepository;
    private final AiServicePort aiServicePort;

    public ReindexProductsService(ProductRepository productRepository, AiServicePort aiServicePort) {
        this.productRepository = productRepository;
        this.aiServicePort = aiServicePort;
    }

    @Override
    @Transactional(readOnly = true)
    public int execute() {
        List<Product> products = productRepository.findAll();

        int indexed = 0;
        int failed = 0;

        for (Product product : products) {
            if (aiServicePort.indexProduct(product)) {
                indexed++;
            } else {
                failed++;
            }
        }

        // Si falló algún producto con contenido indexable, propagamos el fallo para
        // permitir que el arranque reintente el lote completo (la indexación es idempotente).
        if (failed > 0) {
            log.warn("Reindexado incompleto: {} indexado(s), {} fallido(s).", indexed, failed);
            throw new IllegalStateException(
                    "No se pudieron indexar " + failed + " producto(s) en el motor de búsqueda semántica."
            );
        }

        return indexed;
    }
}

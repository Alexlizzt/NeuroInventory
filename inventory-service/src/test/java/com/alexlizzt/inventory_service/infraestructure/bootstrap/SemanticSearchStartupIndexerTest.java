package com.alexlizzt.inventory_service.infraestructure.bootstrap;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.alexlizzt.inventory_service.application.usecase.ReindexProductsUseCase;

@ExtendWith(MockitoExtension.class)
class SemanticSearchStartupIndexerTest {

    @Mock
    private ReindexProductsUseCase reindexProductsUseCase;

    @Test
    @DisplayName("Retorna de inmediato y no indexa cuando está deshabilitado")
    void shouldDoNothingWhenDisabled() {
        SemanticSearchStartupIndexer indexer =
                new SemanticSearchStartupIndexer(reindexProductsUseCase, false, 3, 0);

        indexer.run(null);

        verifyNoInteractions(reindexProductsUseCase);
    }

    @Test
    @DisplayName("Indexa una sola vez cuando el primer intento tiene éxito")
    void shouldIndexOnceOnSuccess() {
        when(reindexProductsUseCase.execute()).thenReturn(3);
        SemanticSearchStartupIndexer indexer =
                new SemanticSearchStartupIndexer(reindexProductsUseCase, true, 5, 0);

        indexer.reindexWithRetries();

        verify(reindexProductsUseCase, times(1)).execute();
    }

    @Test
    @DisplayName("Reintenta hasta tener éxito")
    void shouldRetryUntilSuccess() {
        when(reindexProductsUseCase.execute())
                .thenThrow(new RuntimeException("AI service no disponible"))
                .thenThrow(new RuntimeException("AI service no disponible"))
                .thenReturn(2);
        SemanticSearchStartupIndexer indexer =
                new SemanticSearchStartupIndexer(reindexProductsUseCase, true, 5, 0);

        indexer.reindexWithRetries();

        verify(reindexProductsUseCase, times(3)).execute();
    }

    @Test
    @DisplayName("Se detiene tras alcanzar el máximo de intentos sin propagar la excepción")
    void shouldStopAfterMaxAttemptsWithoutThrowing() {
        when(reindexProductsUseCase.execute()).thenThrow(new RuntimeException("boom"));
        SemanticSearchStartupIndexer indexer =
                new SemanticSearchStartupIndexer(reindexProductsUseCase, true, 4, 0);

        indexer.reindexWithRetries();

        verify(reindexProductsUseCase, times(4)).execute();
    }
}

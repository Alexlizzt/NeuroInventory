import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

// Angular Material Imports
import { MatTabsModule } from '@angular/material/tabs';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatChipsModule } from '@angular/material/chips';
import { MatDividerModule } from '@angular/material/divider';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';

import { RagService } from '../../core/services/rag.service';
import { ProductService } from '../../core/services/product.service';
import { SemanticSearchProductResponse } from '../../core/models/product.model';

interface ChatMessage {
  sender: 'user' | 'assistant';
  text: string;
  sources?: string[];
  timestamp: Date;
}

@Component({
  selector: 'app-ai-assistant',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatTabsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatProgressBarModule,
    MatChipsModule,
    MatDividerModule,
    MatSnackBarModule
  ],
  templateUrl: './ai-assistant.component.html',
  styleUrl: './ai-assistant.component.scss'
})
export class AiAssistantComponent {
  private ragService = inject(RagService);
  private productService = inject(ProductService);
  private snackBar = inject(MatSnackBar);

  // Estado del Chat RAG
  chatInput = '';
  isChatLoading = signal(false);
  messages = signal<ChatMessage[]>([
    {
      sender: 'assistant',
      text: '¡Hola! Soy tu asistente de inventario impulsado por IA. ¿En qué puedo ayudarte hoy?',
      timestamp: new Date()
    }
  ]);

  // Estado de la Búsqueda Semántica
  searchInput = '';
  isSearchLoading = signal(false);
  searchResults = signal<SemanticSearchProductResponse[]>([]);

  // --- MÉTODOS CHAT RAG ---
  sendChatMessage(): void {
    const text = this.chatInput.trim();
    if (!text || this.isChatLoading()) return;

    // Agregar mensaje del usuario
    this.messages.update(msgs => [
      ...msgs,
      { sender: 'user', text, timestamp: new Date() }
    ]);

    this.chatInput = '';
    this.isChatLoading.set(true);

    this.ragService.askQuestion({ question: text }).subscribe({
      next: (res) => {
        this.messages.update(msgs => [
          ...msgs,
          {
            sender: 'assistant',
            text: res.answer,
            sources: res.sources,
            timestamp: new Date()
          }
        ]);
        this.isChatLoading.set(false);
      },
      error: (err) => {
        const errorMessage = err?.error?.error || err?.error?.detail || 'El servicio de IA no está disponible en este momento.';
        const userFriendlyMessage = `⚠️ Lo siento, no pude procesar tu consulta.\n\nDetalle: ${errorMessage}`;
        this.messages.update(msgs => [
          ...msgs,
          {
            sender: 'assistant',
            text: userFriendlyMessage,
            timestamp: new Date()
          }
        ]);
        this.isChatLoading.set(false);
      }
    });
  }

  // --- MÉTODOS BÚSQUEDA SEMÁNTICA ---
  onSemanticSearch(): void {
    const query = this.searchInput.trim();
    if (!query || this.isSearchLoading()) return;

    this.isSearchLoading.set(true);
    this.productService.searchSemantically(query, 6).subscribe({
      next: (results) => {
        this.searchResults.set(results);
        this.isSearchLoading.set(false);
      },
      error: (err) => {
        const errorMessage = err?.error?.error || err?.error?.detail || 'El servicio de búsqueda no está disponible en este momento.';
        this.searchResults.set([{
          id: 'ERROR',
          categoryId: '',
          sku: 'ERROR',
          name: 'Error',
          description: errorMessage,
          price: 0,
          active: false,
          score: 0,
          rationale: `⚠️ ${errorMessage}`
        }]);
        this.isSearchLoading.set(false);
      }
    });
  }
}
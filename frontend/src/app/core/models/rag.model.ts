export interface RagRequest {
  question: string;
  product_id?: string;
}

export interface RagResponse {
  answer: string;
  sources: string[];
}

export interface RagIngestRequest {
  product_id: string;
  content: string;
  metadata?: Record<string, unknown>;
}

export interface RagIngestResponse {
  status: string;
  doc_id: string;
  chunks_created: number;
}
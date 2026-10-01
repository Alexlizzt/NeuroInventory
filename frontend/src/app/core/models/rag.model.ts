export interface RagRequest {
  question: string;
  product_id?: string;
}

export interface RagResponse {
  answer: string;
  sources: string[];
}
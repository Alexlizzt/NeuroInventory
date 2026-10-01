export type MovementType = 'IN' | 'OUT' | 'ADJUSTMENT';

export interface RegisterMovementRequest {
  productId: string;
  type: MovementType;
  quantity: number;
  reason?: string;
}

export interface InventoryMovementResponse {
  id: string;
  productId: string;
  type: MovementType;
  quantity: number;
  reason: string;
  userId: string;
  createdAt: string;
}

export interface StockResponse {
  productId: string;
  quantity: number;
  minStock: number;
  isLowStock: boolean;
  updatedAt: string;
}
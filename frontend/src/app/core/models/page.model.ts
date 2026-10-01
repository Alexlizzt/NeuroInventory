export interface PageResult<T> {
  content: T[];
  pageNumber: number;
  pageSize: number;
  totalElements: number;
  totalPages: number;
}

export interface PageQuery {
  page?: number;
  size?: number;
  sortBy?: string;
  direction?: 'ASC' | 'DESC';
}
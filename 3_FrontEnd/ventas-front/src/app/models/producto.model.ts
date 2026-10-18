export interface Producto {
  id: number;
  codigo: string;
  nombre: string;
  precio: number;
  stock: number;
}

export interface ProductoRequest {
  codigo: string;
  nombre: string;
  precio: number;
  stock: number;
}

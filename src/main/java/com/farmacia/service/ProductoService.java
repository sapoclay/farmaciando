package com.farmacia.service;

import com.farmacia.model.Producto;
import com.farmacia.repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ProductoService {

    @Autowired
    private ProductoRepository productoRepository;

    // Crear o actualizar producto
    public Producto guardarProducto(Producto producto) {
        // Validar código único
        if (producto.getId() == null) {
            if (productoRepository.findByCodigo(producto.getCodigo()).isPresent()) {
                throw new IllegalArgumentException("Ya existe un producto con el código: " + producto.getCodigo());
            }
        } else {
            if (productoRepository.existsByCodigoAndIdNot(producto.getCodigo(), producto.getId())) {
                throw new IllegalArgumentException("Ya existe un producto con el código: " + producto.getCodigo());
            }
        }
        return productoRepository.save(producto);
    }

    // Obtener todos los productos activos
    public List<Producto> obtenerTodosActivos() {
        return productoRepository.findByActivoTrue();
    }

    // Obtener producto por ID
    public Optional<Producto> obtenerPorId(Long id) {
        return productoRepository.findById(id);
    }

    // Buscar por código
    public Optional<Producto> buscarPorCodigo(String codigo) {
        return productoRepository.findByCodigo(codigo);
    }

    // Buscar por nombre
    public List<Producto> buscarPorNombre(String nombre) {
        return productoRepository.findByNombreContainingIgnoreCaseAndActivoTrue(nombre);
    }

    // Buscar por categoría
    public List<Producto> buscarPorCategoria(String categoria) {
        return productoRepository.findByCategoriaAndActivoTrue(categoria);
    }

    // Buscar por laboratorio
    public List<Producto> buscarPorLaboratorio(String laboratorio) {
        return productoRepository.findByLaboratorioAndActivoTrue(laboratorio);
    }

    // Obtener productos con stock bajo
    public List<Producto> obtenerProductosConStockBajo() {
        return productoRepository.findProductosConStockBajo();
    }

    // Obtener productos vencidos
    public List<Producto> obtenerProductosVencidos() {
        return productoRepository.findProductosVencidos(LocalDate.now());
    }

    // Obtener productos próximos a vencer (30 días)
    public List<Producto> obtenerProductosProximosAVencer() {
        LocalDate hoy = LocalDate.now();
        LocalDate fechaLimite = hoy.plusDays(30);
        return productoRepository.findProductosProximosAVencer(hoy, fechaLimite);
    }

    // Eliminar producto (soft delete)
    public void eliminarProducto(Long id) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado con ID: " + id));
        producto.setActivo(false);
        productoRepository.save(producto);
    }

    // Actualizar stock
    public void actualizarStock(Long id, int cantidad) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado con ID: " + id));
        producto.setStock(producto.getStock() + cantidad);
        productoRepository.save(producto);
    }

    // Reducir stock (para ventas)
    public void reducirStock(Long id, int cantidad) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado con ID: " + id));
        if (producto.getStock() < cantidad) {
            throw new IllegalArgumentException("Stock insuficiente. Disponible: " + producto.getStock());
        }
        producto.setStock(producto.getStock() - cantidad);
        productoRepository.save(producto);
    }

    // Obtener total de productos
    public long contarProductosActivos() {
        return productoRepository.countByActivoTrue();
    }

    // Validar disponibilidad
    public boolean validarDisponibilidad(Long id, int cantidad) {
        Optional<Producto> producto = productoRepository.findById(id);
        return producto.isPresent() && producto.get().getStock() >= cantidad;
    }

    // Obtener todos los productos (incluyendo inactivos)
    public List<Producto> obtenerTodos() {
        return productoRepository.findAll();
    }

    // Alias para compatibilidad con ReportesPanel
    public List<Producto> obtenerProductosStockBajo() {
        return obtenerProductosConStockBajo();
    }

    // Alias para compatibilidad con ReportesPanel
    public List<Producto> obtenerProductosProximosVencer(int dias) {
        LocalDate hoy = LocalDate.now();
        LocalDate fechaLimite = hoy.plusDays(dias);
        return productoRepository.findProductosProximosAVencer(hoy, fechaLimite);
    }
}

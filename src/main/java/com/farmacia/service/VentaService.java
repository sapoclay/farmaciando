package com.farmacia.service;

import com.farmacia.model.DetalleVenta;
import com.farmacia.model.Producto;
import com.farmacia.model.Usuario;
import com.farmacia.model.Venta;
import com.farmacia.repository.ProductoRepository;
import com.farmacia.repository.VentaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class VentaService {

    @Autowired
    private VentaRepository ventaRepository;

    @Autowired
    private ProductoRepository productoRepository;

    // Crear venta (versión con usuario)
    public Venta crearVenta(Venta venta, Usuario usuario) {
        // Asociar el usuario que realiza la venta
        venta.setUsuario(usuario);
        // Validar y actualizar stock
        for (DetalleVenta detalle : venta.getDetalles()) {
            Producto producto = detalle.getProducto();
            
            // Verificar stock disponible
            if (producto.getStock() < detalle.getCantidad()) {
                throw new IllegalStateException(
                    "Stock insuficiente para el producto: " + producto.getNombre() + 
                    ". Disponible: " + producto.getStock() + ", Solicitado: " + detalle.getCantidad()
                );
            }
            
            // Reducir stock
            producto.setStock(producto.getStock() - detalle.getCantidad());
            productoRepository.save(producto);
        }
        
        // Calcular totales
        venta.calcularTotal();
        
        return ventaRepository.save(venta);
    }

    // Obtener todas las ventas activas
    public List<Venta> obtenerTodasActivas() {
        List<Venta> ventas = ventaRepository.findByActivoTrue();
        // Inicializar la colección de detalles para evitar LazyInitializationException
        ventas.forEach(v -> v.getDetalles().size());
        return ventas;
    }

    // Obtener venta por ID
    public Optional<Venta> obtenerPorId(Long id) {
        return ventaRepository.findById(id);
    }

    // Obtener ventas del día
    public List<Venta> obtenerVentasDelDia() {
        List<Venta> ventas = ventaRepository.findVentasDelDia();
        // Inicializar la colección de detalles para evitar LazyInitializationException
        ventas.forEach(v -> v.getDetalles().size());
        return ventas;
    }

    // Obtener ventas por rango de fechas
    public List<Venta> obtenerVentasPorFecha(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        return ventaRepository.findByFechaBetweenAndActivoTrueOrderByFechaDesc(fechaInicio, fechaFin);
    }

    // Obtener ventas por método de pago
    public List<Venta> obtenerVentasPorMetodoPago(String metodoPago) {
        return ventaRepository.findByMetodoPagoAndActivoTrue(metodoPago);
    }

    // Buscar ventas por cliente
    public List<Venta> buscarPorCliente(String cliente) {
        return ventaRepository.findByClienteContainingIgnoreCaseAndActivoTrue(cliente);
    }

    // Obtener últimas ventas
    public List<Venta> obtenerUltimasVentas() {
        return ventaRepository.findTop10ByActivoTrueOrderByFechaDesc();
    }

    // Calcular total de ventas del día
    public Double calcularTotalVentasDelDia() {
        Double total = ventaRepository.calcularTotalVentasDelDia();
        return total != null ? total : 0.0;
    }

    // Contar ventas del día
    public Long contarVentasDelDia() {
        return ventaRepository.contarVentasDelDia();
    }

    // Anular venta (soft delete)
    public void anularVenta(Long id) {
        Optional<Venta> ventaOpt = ventaRepository.findById(id);
        if (ventaOpt.isPresent()) {
            Venta venta = ventaOpt.get();
            
            // Restaurar stock
            for (DetalleVenta detalle : venta.getDetalles()) {
                Producto producto = detalle.getProducto();
                producto.setStock(producto.getStock() + detalle.getCantidad());
                productoRepository.save(producto);
            }
            
            // Marcar como inactivo
            venta.setActivo(false);
            ventaRepository.save(venta);
        } else {
            throw new IllegalArgumentException("Venta no encontrada con ID: " + id);
        }
    }

    // Obtener ventas activas
    public List<Venta> obtenerVentasActivas() {
        List<Venta> ventas = ventaRepository.findByActivoTrue();
        // Inicializar la colección de detalles para evitar LazyInitializationException
        ventas.forEach(v -> v.getDetalles().size());
        return ventas;
    }

    // Obtener ventas por rango de fechas
    public List<Venta> obtenerVentasPorRango(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        List<Venta> ventas = ventaRepository.findByFechaBetweenAndActivoTrueOrderByFechaDesc(fechaInicio, fechaFin);
        // Inicializar la colección de detalles
        ventas.forEach(v -> v.getDetalles().size());
        return ventas;
    }

    // Obtener estadísticas del día
    public EstadisticasVentas obtenerEstadisticasDelDia() {
        EstadisticasVentas stats = new EstadisticasVentas();
        stats.setTotalVentas(calcularTotalVentasDelDia());
        stats.setNumeroVentas(contarVentasDelDia());
        
        // Calcular promedio y productos vendidos
        List<Venta> ventasDelDia = obtenerVentasDelDia();
        // Inicializar detalles
        ventasDelDia.forEach(v -> v.getDetalles().size());
        
        long totalProductos = ventasDelDia.stream()
            .mapToLong(v -> v.getDetalles().stream()
                .mapToLong(d -> d.getCantidad().longValue())
                .sum())
            .sum();
        
        stats.setTotalProductosVendidos(totalProductos);
        stats.setPromedioVenta(stats.getNumeroVentas() > 0 ? stats.getTotalVentas() / stats.getNumeroVentas() : 0.0);
        
        return stats;
    }

    // Clase interna para estadísticas
    public static class EstadisticasVentas {
        private Double totalVentas;
        private Long numeroVentas;
        private Double promedioVenta;
        private Long totalProductosVendidos;

        public Double getTotalVentas() {
            return totalVentas;
        }

        public void setTotalVentas(Double totalVentas) {
            this.totalVentas = totalVentas;
        }

        public Long getNumeroVentas() {
            return numeroVentas;
        }

        public void setNumeroVentas(Long numeroVentas) {
            this.numeroVentas = numeroVentas;
        }

        public Double getPromedioVenta() {
            return promedioVenta;
        }

        public void setPromedioVenta(Double promedioVenta) {
            this.promedioVenta = promedioVenta;
        }

        public Long getTotalProductosVendidos() {
            return totalProductosVendidos;
        }

        public void setTotalProductosVendidos(Long totalProductosVendidos) {
            this.totalProductosVendidos = totalProductosVendidos;
        }

        // Mantener compatibilidad con código antiguo
        public Long getCantidadVentas() {
            return numeroVentas;
        }

        public void setCantidadVentas(Long cantidadVentas) {
            this.numeroVentas = cantidadVentas;
        }
    }
}

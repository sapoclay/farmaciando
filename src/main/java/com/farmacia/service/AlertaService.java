package com.farmacia.service;

import com.farmacia.model.Alerta;
import com.farmacia.model.Alerta.TipoAlerta;
import com.farmacia.model.Pedido;
import com.farmacia.model.Producto;
import com.farmacia.repository.PedidoRepository;
import com.farmacia.repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio para gestión de alertas del sistema
 */
@Service
public class AlertaService {
    
    @Autowired
    private ProductoRepository productoRepository;
    
    @Autowired
    private PedidoRepository pedidoRepository;
    
    // Umbrales configurables
    private static final int STOCK_MINIMO = 10;
    private static final int DIAS_AVISO_CADUCIDAD = 30;
    private static final int DIAS_PEDIDO_RETRASADO = 7;
    
    /**
     * Obtiene todas las alertas activas del sistema
     */
    public List<Alerta> obtenerTodasLasAlertas() {
        List<Alerta> alertas = new ArrayList<>();
        
        alertas.addAll(detectarStockBajo());
        alertas.addAll(detectarProductosCaducados());
        alertas.addAll(detectarProximosCaducar());
        alertas.addAll(detectarPedidosPendientes());
        alertas.addAll(detectarPedidosRetrasados());
        
        // Ordenar por criticidad y fecha
        alertas.sort((a1, a2) -> {
            if (a1.isCritica() != a2.isCritica()) {
                return a1.isCritica() ? -1 : 1;
            }
            return a2.getFecha().compareTo(a1.getFecha());
        });
        
        return alertas;
    }
    
    /**
     * Obtiene solo las alertas críticas
     */
    public List<Alerta> obtenerAlertasCriticas() {
        return obtenerTodasLasAlertas().stream()
                .filter(Alerta::isCritica)
                .collect(Collectors.toList());
    }
    
    /**
     * Detecta productos con stock bajo
     */
    public List<Alerta> detectarStockBajo() {
        List<Producto> productosStockBajo = productoRepository.findByStockLessThan(STOCK_MINIMO);
        
        return productosStockBajo.stream()
                .filter(p -> p.getActivo() != null && p.getActivo())
                .map(producto -> {
                    String mensaje = String.format("Stock bajo: %s", producto.getNombre());
                    String detalle = String.format("Stock actual: %d unidades (Mínimo: %d)", 
                            producto.getStock(), STOCK_MINIMO);
                    
                    return new Alerta(
                            TipoAlerta.STOCK_BAJO,
                            mensaje,
                            detalle,
                            producto,
                            producto.getId()
                    );
                })
                .collect(Collectors.toList());
    }
    
    /**
     * Detecta productos ya caducados
     */
    public List<Alerta> detectarProductosCaducados() {
        LocalDate hoy = LocalDate.now();
        List<Producto> productosCaducados = productoRepository.findByFechaVencimientoBefore(hoy);
        
        return productosCaducados.stream()
                .filter(p -> p.getActivo() != null && p.getActivo())
                .map(producto -> {
                    long diasCaducado = ChronoUnit.DAYS.between(producto.getFechaVencimiento(), hoy);
                    String mensaje = String.format("Producto caducado: %s", producto.getNombre());
                    String detalle = String.format("Caducado hace %d días (Fecha: %s)", 
                            diasCaducado, producto.getFechaVencimiento());
                    
                    Alerta alerta = new Alerta(
                            TipoAlerta.PRODUCTO_CADUCADO,
                            mensaje,
                            detalle,
                            producto,
                            producto.getId()
                    );
                    alerta.setCritica(true);
                    
                    return alerta;
                })
                .collect(Collectors.toList());
    }
    
    /**
     * Detecta productos próximos a caducar
     */
    public List<Alerta> detectarProximosCaducar() {
        LocalDate hoy = LocalDate.now();
        LocalDate fechaLimite = hoy.plusDays(DIAS_AVISO_CADUCIDAD);
        
        List<Producto> proximosCaducar = productoRepository
                .findByFechaVencimientoBetween(hoy, fechaLimite);
        
        return proximosCaducar.stream()
                .filter(p -> p.getActivo() != null && p.getActivo())
                .map(producto -> {
                    long diasRestantes = ChronoUnit.DAYS.between(hoy, producto.getFechaVencimiento());
                    String mensaje = String.format("Próximo a caducar: %s", producto.getNombre());
                    String detalle = String.format("Caduca en %d días (Fecha: %s)", 
                            diasRestantes, producto.getFechaVencimiento());
                    
                    return new Alerta(
                            TipoAlerta.PROXIMO_CADUCAR,
                            mensaje,
                            detalle,
                            producto,
                            producto.getId()
                    );
                })
                .collect(Collectors.toList());
    }
    
    /**
     * Detecta pedidos pendientes de recibir
     */
    public List<Alerta> detectarPedidosPendientes() {
        List<Pedido> pedidosPendientes = pedidoRepository.findPedidosPendientes();
        
        return pedidosPendientes.stream()
                .map(pedido -> {
                    String mensaje = String.format("Pedido pendiente: %s", pedido.getNumeroPedido());
                    String detalle = String.format("Proveedor: %s | Estado: %s | Total: €%.2f", 
                            pedido.getProveedor().getEmpresa(),
                            pedido.getEstado().getDescripcion(),
                            pedido.getTotal());
                    
                    return new Alerta(
                            TipoAlerta.PEDIDO_PENDIENTE,
                            mensaje,
                            detalle,
                            pedido,
                            pedido.getId()
                    );
                })
                .collect(Collectors.toList());
    }
    
    /**
     * Detecta pedidos retrasados
     */
    public List<Alerta> detectarPedidosRetrasados() {
        List<Pedido> pedidosPendientes = pedidoRepository.findPedidosPendientes();
        LocalDate fechaLimite = LocalDate.now().minusDays(DIAS_PEDIDO_RETRASADO);
        
        return pedidosPendientes.stream()
                .filter(pedido -> pedido.getFechaPedido().toLocalDate().isBefore(fechaLimite))
                .map(pedido -> {
                    long diasRetraso = ChronoUnit.DAYS.between(pedido.getFechaPedido().toLocalDate(), LocalDate.now());
                    String mensaje = String.format("Pedido retrasado: %s", pedido.getNumeroPedido());
                    String detalle = String.format("Proveedor: %s | Pedido hace %d días | Total: €%.2f", 
                            pedido.getProveedor().getEmpresa(),
                            diasRetraso,
                            pedido.getTotal());
                    
                    Alerta alerta = new Alerta(
                            TipoAlerta.PEDIDO_RETRASADO,
                            mensaje,
                            detalle,
                            pedido,
                            pedido.getId()
                    );
                    alerta.setCritica(true);
                    
                    return alerta;
                })
                .collect(Collectors.toList());
    }
    
    /**
     * Obtiene el conteo de alertas por tipo
     */
    public EstadisticasAlertas obtenerEstadisticas() {
        List<Alerta> alertas = obtenerTodasLasAlertas();
        
        EstadisticasAlertas stats = new EstadisticasAlertas();
        stats.total = alertas.size();
        stats.criticas = (int) alertas.stream().filter(Alerta::isCritica).count();
        stats.stockBajo = (int) alertas.stream()
                .filter(a -> a.getTipo() == TipoAlerta.STOCK_BAJO).count();
        stats.caducados = (int) alertas.stream()
                .filter(a -> a.getTipo() == TipoAlerta.PRODUCTO_CADUCADO).count();
        stats.proximosCaducar = (int) alertas.stream()
                .filter(a -> a.getTipo() == TipoAlerta.PROXIMO_CADUCAR).count();
        stats.pedidosPendientes = (int) alertas.stream()
                .filter(a -> a.getTipo() == TipoAlerta.PEDIDO_PENDIENTE).count();
        stats.pedidosRetrasados = (int) alertas.stream()
                .filter(a -> a.getTipo() == TipoAlerta.PEDIDO_RETRASADO).count();
        
        return stats;
    }
    
    /**
     * Clase interna para estadísticas de alertas
     */
    public static class EstadisticasAlertas {
        public int total;
        public int criticas;
        public int stockBajo;
        public int caducados;
        public int proximosCaducar;
        public int pedidosPendientes;
        public int pedidosRetrasados;
    }
}

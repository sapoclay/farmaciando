package com.farmacia.service;

import com.farmacia.model.DetallePedido;
import com.farmacia.model.Pedido;
import com.farmacia.model.Pedido.EstadoPedido;
import com.farmacia.model.Producto;
import com.farmacia.model.Proveedor;
import com.farmacia.repository.PedidoRepository;
import com.farmacia.repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class PedidoService {

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private ProductoRepository productoRepository;

    // Crear pedido
    public Pedido crearPedido(Pedido pedido) {
        validarPedido(pedido);
        pedido.setEstado(EstadoPedido.BORRADOR);
        pedido.calcularTotal();
        return pedidoRepository.save(pedido);
    }

    // Actualizar pedido
    public Pedido actualizar(Pedido pedido) {
        validarPedido(pedido);
        pedido.calcularTotal();
        return pedidoRepository.save(pedido);
    }

    // Obtener todos los pedidos activos
    public List<Pedido> obtenerTodosActivos() {
        List<Pedido> pedidos = pedidoRepository.findByActivoTrueOrderByFechaPedidoDesc();
        // Inicializar detalles
        pedidos.forEach(p -> p.getDetalles().size());
        return pedidos;
    }

    // Obtener pedido por ID
    public Optional<Pedido> obtenerPorId(Long id) {
        Optional<Pedido> pedidoOpt = pedidoRepository.findById(id);
        pedidoOpt.ifPresent(p -> p.getDetalles().size()); // Inicializar detalles
        return pedidoOpt;
    }

    // Obtener por número de pedido
    public Optional<Pedido> obtenerPorNumeroPedido(String numeroPedido) {
        Optional<Pedido> pedidoOpt = pedidoRepository.findByNumeroPedido(numeroPedido);
        pedidoOpt.ifPresent(p -> p.getDetalles().size());
        return pedidoOpt;
    }

    // Obtener pedidos por proveedor
    public List<Pedido> obtenerPorProveedor(Proveedor proveedor) {
        List<Pedido> pedidos = pedidoRepository.findByProveedorAndActivoTrueOrderByFechaPedidoDesc(proveedor);
        pedidos.forEach(p -> p.getDetalles().size());
        return pedidos;
    }

    // Obtener pedidos por estado
    public List<Pedido> obtenerPorEstado(EstadoPedido estado) {
        List<Pedido> pedidos = pedidoRepository.findByEstadoAndActivoTrueOrderByFechaPedidoDesc(estado);
        pedidos.forEach(p -> p.getDetalles().size());
        return pedidos;
    }

    // Obtener pedidos pendientes
    public List<Pedido> obtenerPendientes() {
        List<Pedido> pedidos = pedidoRepository.findPedidosPendientes();
        pedidos.forEach(p -> p.getDetalles().size());
        return pedidos;
    }

    // Obtener últimos pedidos
    public List<Pedido> obtenerUltimosPedidos() {
        List<Pedido> pedidos = pedidoRepository.findTop10ByActivoTrueOrderByFechaPedidoDesc();
        pedidos.forEach(p -> p.getDetalles().size());
        return pedidos;
    }

    // Cambiar estado del pedido
    public void cambiarEstado(Long id, EstadoPedido nuevoEstado) {
        Optional<Pedido> pedidoOpt = pedidoRepository.findById(id);
        if (pedidoOpt.isPresent()) {
            Pedido pedido = pedidoOpt.get();
            pedido.setEstado(nuevoEstado);
            
            // Si el estado es RECIBIDO, actualizar el stock
            if (nuevoEstado == EstadoPedido.RECIBIDO) {
                pedido.setFechaEntregaReal(LocalDateTime.now());
                actualizarStockAlRecibir(pedido);
            }
            
            pedidoRepository.save(pedido);
        } else {
            throw new IllegalArgumentException("Pedido no encontrado con ID: " + id);
        }
    }

    // Marcar pedido como enviado
    public void marcarComoEnviado(Long id) {
        cambiarEstado(id, EstadoPedido.ENVIADO);
    }

    // Marcar pedido como recibido
    public void marcarComoRecibido(Long id) {
        cambiarEstado(id, EstadoPedido.RECIBIDO);
    }

    // Cancelar pedido
    public void cancelarPedido(Long id, String motivo) {
        Optional<Pedido> pedidoOpt = pedidoRepository.findById(id);
        if (pedidoOpt.isPresent()) {
            Pedido pedido = pedidoOpt.get();
            if (pedido.getEstado() == EstadoPedido.RECIBIDO) {
                throw new IllegalStateException("No se puede cancelar un pedido ya recibido");
            }
            pedido.setEstado(EstadoPedido.CANCELADO);
            if (motivo != null && !motivo.isEmpty()) {
                String obsActual = pedido.getObservaciones() != null ? pedido.getObservaciones() + "\n" : "";
                pedido.setObservaciones(obsActual + "CANCELADO: " + motivo);
            }
            pedidoRepository.save(pedido);
        } else {
            throw new IllegalArgumentException("Pedido no encontrado con ID: " + id);
        }
    }

    // Actualizar stock al recibir pedido
    private void actualizarStockAlRecibir(Pedido pedido) {
        for (DetallePedido detalle : pedido.getDetalles()) {
            if (detalle.getProducto() != null) {
                Producto producto = detalle.getProducto();
                int cantidadRecibida = detalle.getCantidadRecibida() != null && detalle.getCantidadRecibida() > 0 
                    ? detalle.getCantidadRecibida() 
                    : detalle.getCantidad();
                
                producto.setStock(producto.getStock() + cantidadRecibida);
                productoRepository.save(producto);
                
                detalle.setRecibido(true);
                detalle.setCantidadRecibida(cantidadRecibida);
            }
        }
    }

    // Desactivar pedido (soft delete)
    public void desactivar(Long id) {
        Optional<Pedido> pedidoOpt = pedidoRepository.findById(id);
        if (pedidoOpt.isPresent()) {
            Pedido pedido = pedidoOpt.get();
            pedido.setActivo(false);
            pedidoRepository.save(pedido);
        } else {
            throw new IllegalArgumentException("Pedido no encontrado con ID: " + id);
        }
    }

    // Contar pedidos por estado
    public Long contarPorEstado(EstadoPedido estado) {
        return pedidoRepository.countByEstadoAndActivoTrue(estado);
    }

    // Validaciones
    private void validarPedido(Pedido pedido) {
        if (pedido.getProveedor() == null) {
            throw new IllegalArgumentException("El proveedor es obligatorio");
        }

        if (pedido.getDetalles() == null || pedido.getDetalles().isEmpty()) {
            throw new IllegalArgumentException("El pedido debe tener al menos un producto");
        }

        // Validar cada detalle
        for (DetallePedido detalle : pedido.getDetalles()) {
            if (detalle.getCantidad() == null || detalle.getCantidad() <= 0) {
                throw new IllegalArgumentException("La cantidad debe ser mayor a 0");
            }
            if (detalle.getPrecioUnitario() == null || detalle.getPrecioUnitario().compareTo(java.math.BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("El precio unitario debe ser mayor a 0");
            }
        }
    }

    // Obtener estadísticas
    public EstadisticasPedidos obtenerEstadisticas() {
        EstadisticasPedidos stats = new EstadisticasPedidos();
        stats.setPedidosPendientes(contarPorEstado(EstadoPedido.ENVIADO) + 
                                   contarPorEstado(EstadoPedido.CONFIRMADO) + 
                                   contarPorEstado(EstadoPedido.EN_TRANSITO));
        stats.setPedidosRecibidos(contarPorEstado(EstadoPedido.RECIBIDO));
        stats.setPedidosCancelados(contarPorEstado(EstadoPedido.CANCELADO));
        
        return stats;
    }

    // Clase interna para estadísticas
    public static class EstadisticasPedidos {
        private Long pedidosPendientes;
        private Long pedidosRecibidos;
        private Long pedidosCancelados;

        public Long getPedidosPendientes() {
            return pedidosPendientes;
        }

        public void setPedidosPendientes(Long pedidosPendientes) {
            this.pedidosPendientes = pedidosPendientes;
        }

        public Long getPedidosRecibidos() {
            return pedidosRecibidos;
        }

        public void setPedidosRecibidos(Long pedidosRecibidos) {
            this.pedidosRecibidos = pedidosRecibidos;
        }

        public Long getPedidosCancelados() {
            return pedidosCancelados;
        }

        public void setPedidosCancelados(Long pedidosCancelados) {
            this.pedidosCancelados = pedidosCancelados;
        }
    }
}

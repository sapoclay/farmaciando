package com.farmacia.repository;

import com.farmacia.model.Pedido;
import com.farmacia.model.Pedido.EstadoPedido;
import com.farmacia.model.Proveedor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    // Buscar por número de pedido
    Optional<Pedido> findByNumeroPedido(String numeroPedido);

    // Buscar pedidos activos
    List<Pedido> findByActivoTrueOrderByFechaPedidoDesc();

    // Buscar por proveedor
    List<Pedido> findByProveedorAndActivoTrueOrderByFechaPedidoDesc(Proveedor proveedor);

    // Buscar por estado
    List<Pedido> findByEstadoAndActivoTrueOrderByFechaPedidoDesc(EstadoPedido estado);

    // Buscar por rango de fechas
    List<Pedido> findByFechaPedidoBetweenAndActivoTrueOrderByFechaPedidoDesc(
        LocalDateTime fechaInicio, 
        LocalDateTime fechaFin
    );

    // Buscar pedidos pendientes (no recibidos ni cancelados)
    @Query("SELECT p FROM Pedido p WHERE p.estado NOT IN ('RECIBIDO', 'CANCELADO') AND p.activo = true ORDER BY p.fechaPedido DESC")
    List<Pedido> findPedidosPendientes();

    // Buscar últimos pedidos
    List<Pedido> findTop10ByActivoTrueOrderByFechaPedidoDesc();

    // Contar pedidos por estado
    Long countByEstadoAndActivoTrue(EstadoPedido estado);

    // Calcular total de pedidos en un período
    @Query("SELECT COALESCE(SUM(p.total), 0) FROM Pedido p WHERE p.fechaPedido BETWEEN :fechaInicio AND :fechaFin AND p.activo = true")
    Double calcularTotalPedidosPorPeriodo(LocalDateTime fechaInicio, LocalDateTime fechaFin);
}

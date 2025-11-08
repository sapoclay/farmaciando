package com.farmacia.repository;

import com.farmacia.model.Venta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface VentaRepository extends JpaRepository<Venta, Long> {

    // Buscar ventas activas
    List<Venta> findByActivoTrue();

    // Buscar ventas por rango de fechas
    List<Venta> findByFechaBetweenAndActivoTrueOrderByFechaDesc(LocalDateTime fechaInicio, LocalDateTime fechaFin);

    // Buscar ventas del día actual
    @Query("SELECT v FROM Venta v WHERE CAST(v.fecha AS date) = CURRENT_DATE AND v.activo = true ORDER BY v.fecha DESC")
    List<Venta> findVentasDelDia();

    // Buscar ventas por método de pago
    List<Venta> findByMetodoPagoAndActivoTrue(String metodoPago);

    // Buscar ventas por cliente
    List<Venta> findByClienteContainingIgnoreCaseAndActivoTrue(String cliente);

    // Obtener últimas ventas
    List<Venta> findTop10ByActivoTrueOrderByFechaDesc();

    // Calcular total de ventas del día
    @Query("SELECT COALESCE(SUM(v.total), 0) FROM Venta v WHERE CAST(v.fecha AS date) = CURRENT_DATE AND v.activo = true")
    Double calcularTotalVentasDelDia();

    // Contar ventas del día
    @Query("SELECT COUNT(v) FROM Venta v WHERE CAST(v.fecha AS date) = CURRENT_DATE AND v.activo = true")
    Long contarVentasDelDia();
}

package com.farmacia.repository;

import com.farmacia.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {

    // Buscar por código
    Optional<Producto> findByCodigo(String codigo);

    // Buscar productos activos
    List<Producto> findByActivoTrue();

    // Buscar por nombre (contiene)
    List<Producto> findByNombreContainingIgnoreCaseAndActivoTrue(String nombre);

    // Buscar por categoría
    List<Producto> findByCategoriaAndActivoTrue(String categoria);

    // Buscar por laboratorio
    List<Producto> findByLaboratorioAndActivoTrue(String laboratorio);

    // Buscar productos con stock bajo
    @Query("SELECT p FROM Producto p WHERE p.stock <= p.stockMinimo AND p.activo = true")
    List<Producto> findProductosConStockBajo();

    // Buscar productos vencidos
    @Query("SELECT p FROM Producto p WHERE p.fechaVencimiento < :fecha AND p.activo = true")
    List<Producto> findProductosVencidos(LocalDate fecha);

    // Buscar productos próximos a vencer
    @Query("SELECT p FROM Producto p WHERE p.fechaVencimiento BETWEEN :fechaInicio AND :fechaFin AND p.activo = true")
    List<Producto> findProductosProximosAVencer(LocalDate fechaInicio, LocalDate fechaFin);

    // Contar productos activos
    long countByActivoTrue();

    // Verificar si existe un código (excluyendo un producto específico)
    boolean existsByCodigoAndIdNot(String codigo, Long id);
    
    // Buscar por stock menor que un valor
    List<Producto> findByStockLessThan(int stock);
    
    // Buscar por fecha de vencimiento antes de
    List<Producto> findByFechaVencimientoBefore(LocalDate fecha);
    
    // Buscar por fecha de vencimiento entre
    List<Producto> findByFechaVencimientoBetween(LocalDate desde, LocalDate hasta);
}

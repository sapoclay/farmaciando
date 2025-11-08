package com.farmacia.repository;

import com.farmacia.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    // Buscar clientes activos
    List<Cliente> findByActivoTrue();

    // Buscar cliente por documento
    Optional<Cliente> findByDocumento(String documento);

    // Buscar cliente por documento (solo activos)
    Optional<Cliente> findByDocumentoAndActivoTrue(String documento);

    // Buscar clientes por nombre o apellido (búsqueda parcial)
    @Query("SELECT c FROM Cliente c WHERE " +
           "(LOWER(c.nombre) LIKE LOWER(CONCAT('%', :busqueda, '%')) OR " +
           "LOWER(c.apellido) LIKE LOWER(CONCAT('%', :busqueda, '%'))) AND " +
           "c.activo = true " +
           "ORDER BY c.nombre, c.apellido")
    List<Cliente> buscarPorNombre(@Param("busqueda") String busqueda);

    // Buscar clientes por email
    List<Cliente> findByEmailContainingIgnoreCaseAndActivoTrue(String email);

    // Buscar clientes por teléfono
    List<Cliente> findByTelefonoContainingAndActivoTrue(String telefono);

    // Buscar clientes por tipo de documento
    List<Cliente> findByTipoDocumentoAndActivoTrueOrderByNombreAsc(String tipoDocumento);

    // Buscar clientes por ciudad
    List<Cliente> findByCiudadContainingIgnoreCaseAndActivoTrueOrderByNombreAsc(String ciudad);

    // Obtener últimos clientes registrados
    List<Cliente> findTop10ByActivoTrueOrderByFechaRegistroDesc();

    // Contar clientes activos
    Long countByActivoTrue();

    // Verificar si existe un documento (excluyendo un ID específico para actualización)
    @Query("SELECT COUNT(c) > 0 FROM Cliente c WHERE c.documento = :documento AND c.id != :id")
    Boolean existeDocumentoDuplicado(@Param("documento") String documento, @Param("id") Long id);

    // Búsqueda general (nombre, apellido, documento, email)
    @Query("SELECT c FROM Cliente c WHERE " +
           "(LOWER(c.nombre) LIKE LOWER(CONCAT('%', :busqueda, '%')) OR " +
           "LOWER(c.apellido) LIKE LOWER(CONCAT('%', :busqueda, '%')) OR " +
           "LOWER(c.documento) LIKE LOWER(CONCAT('%', :busqueda, '%')) OR " +
           "LOWER(c.email) LIKE LOWER(CONCAT('%', :busqueda, '%'))) AND " +
           "c.activo = true " +
           "ORDER BY c.nombre, c.apellido")
    List<Cliente> busquedaGeneral(@Param("busqueda") String busqueda);
}

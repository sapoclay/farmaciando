package com.farmacia.repository;

import com.farmacia.model.Proveedor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProveedorRepository extends JpaRepository<Proveedor, Long> {

    // Buscar proveedores activos
    List<Proveedor> findByActivoTrue();

    // Buscar por nombre o empresa
    List<Proveedor> findByNombreContainingIgnoreCaseOrEmpresaContainingIgnoreCase(String nombre, String empresa);

    // Buscar por empresa
    List<Proveedor> findByEmpresaContainingIgnoreCase(String empresa);

    // Buscar por email
    Optional<Proveedor> findByEmail(String email);

    // Buscar por NIF
    Optional<Proveedor> findByNif(String nif);

    // Buscar por ciudad
    List<Proveedor> findByCiudadContainingIgnoreCaseAndActivoTrue(String ciudad);

    // Buscar por calificación
    List<Proveedor> findByCalificacionGreaterThanEqualAndActivoTrue(Integer calificacion);

    // Contar proveedores activos
    Long countByActivoTrue();
}

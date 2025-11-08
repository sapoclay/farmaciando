package com.farmacia.service;

import com.farmacia.model.Proveedor;
import com.farmacia.repository.ProveedorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ProveedorService {

    @Autowired
    private ProveedorRepository proveedorRepository;

    // Crear o actualizar proveedor
    public Proveedor guardar(Proveedor proveedor) {
        validarProveedor(proveedor);
        return proveedorRepository.save(proveedor);
    }

    // Obtener todos los proveedores activos
    public List<Proveedor> obtenerTodosActivos() {
        return proveedorRepository.findByActivoTrue();
    }

    // Obtener proveedor por ID
    public Optional<Proveedor> obtenerPorId(Long id) {
        return proveedorRepository.findById(id);
    }

    // Buscar proveedores
    public List<Proveedor> buscar(String criterio) {
        return proveedorRepository.findByNombreContainingIgnoreCaseOrEmpresaContainingIgnoreCase(criterio, criterio);
    }

    // Buscar por empresa
    public List<Proveedor> buscarPorEmpresa(String empresa) {
        return proveedorRepository.findByEmpresaContainingIgnoreCase(empresa);
    }

    // Buscar por email
    public Optional<Proveedor> buscarPorEmail(String email) {
        return proveedorRepository.findByEmail(email);
    }

    // Buscar por NIF
    public Optional<Proveedor> buscarPorNif(String nif) {
        return proveedorRepository.findByNif(nif);
    }

    // Buscar por ciudad
    public List<Proveedor> buscarPorCiudad(String ciudad) {
        return proveedorRepository.findByCiudadContainingIgnoreCaseAndActivoTrue(ciudad);
    }

    // Buscar por calificación mínima
    public List<Proveedor> buscarPorCalificacionMinima(Integer calificacion) {
        return proveedorRepository.findByCalificacionGreaterThanEqualAndActivoTrue(calificacion);
    }

    // Actualizar calificación
    public void actualizarCalificacion(Long id, Integer calificacion) {
        Optional<Proveedor> proveedorOpt = proveedorRepository.findById(id);
        if (proveedorOpt.isPresent()) {
            Proveedor proveedor = proveedorOpt.get();
            if (calificacion >= 0 && calificacion <= 5) {
                proveedor.setCalificacion(calificacion);
                proveedorRepository.save(proveedor);
            } else {
                throw new IllegalArgumentException("La calificación debe estar entre 0 y 5");
            }
        } else {
            throw new IllegalArgumentException("Proveedor no encontrado con ID: " + id);
        }
    }

    // Desactivar proveedor (soft delete)
    public void desactivar(Long id) {
        Optional<Proveedor> proveedorOpt = proveedorRepository.findById(id);
        if (proveedorOpt.isPresent()) {
            Proveedor proveedor = proveedorOpt.get();
            proveedor.setActivo(false);
            proveedorRepository.save(proveedor);
        } else {
            throw new IllegalArgumentException("Proveedor no encontrado con ID: " + id);
        }
    }

    // Activar proveedor
    public void activar(Long id) {
        Optional<Proveedor> proveedorOpt = proveedorRepository.findById(id);
        if (proveedorOpt.isPresent()) {
            Proveedor proveedor = proveedorOpt.get();
            proveedor.setActivo(true);
            proveedorRepository.save(proveedor);
        } else {
            throw new IllegalArgumentException("Proveedor no encontrado con ID: " + id);
        }
    }

    // Contar proveedores activos
    public Long contarActivos() {
        return proveedorRepository.countByActivoTrue();
    }

    // Validaciones
    private void validarProveedor(Proveedor proveedor) {
        if (proveedor.getNombre() == null || proveedor.getNombre().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del proveedor es obligatorio");
        }

        if (proveedor.getEmpresa() == null || proveedor.getEmpresa().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre de la empresa es obligatorio");
        }

        // Validar email único
        if (proveedor.getEmail() != null && !proveedor.getEmail().trim().isEmpty()) {
            Optional<Proveedor> existente = proveedorRepository.findByEmail(proveedor.getEmail());
            if (existente.isPresent() && !existente.get().getId().equals(proveedor.getId())) {
                throw new IllegalArgumentException("Ya existe un proveedor con ese email");
            }
        }

        // Validar NIF único
        if (proveedor.getNif() != null && !proveedor.getNif().trim().isEmpty()) {
            Optional<Proveedor> existente = proveedorRepository.findByNif(proveedor.getNif());
            if (existente.isPresent() && !existente.get().getId().equals(proveedor.getId())) {
                throw new IllegalArgumentException("Ya existe un proveedor con ese NIF");
            }
        }

        // Validar calificación
        if (proveedor.getCalificacion() != null) {
            if (proveedor.getCalificacion() < 0 || proveedor.getCalificacion() > 5) {
                throw new IllegalArgumentException("La calificación debe estar entre 0 y 5");
            }
        }
    }

    // Obtener estadísticas
    public EstadisticasProveedores obtenerEstadisticas() {
        EstadisticasProveedores stats = new EstadisticasProveedores();
        stats.setTotalProveedores(contarActivos());
        
        List<Proveedor> proveedores = obtenerTodosActivos();
        long conCalificacion5 = proveedores.stream()
            .filter(p -> p.getCalificacion() != null && p.getCalificacion() == 5)
            .count();
        
        stats.setProveedoresExcelentes(conCalificacion5);
        
        return stats;
    }

    // Clase interna para estadísticas
    public static class EstadisticasProveedores {
        private Long totalProveedores;
        private Long proveedoresExcelentes;

        public Long getTotalProveedores() {
            return totalProveedores;
        }

        public void setTotalProveedores(Long totalProveedores) {
            this.totalProveedores = totalProveedores;
        }

        public Long getProveedoresExcelentes() {
            return proveedoresExcelentes;
        }

        public void setProveedoresExcelentes(Long proveedoresExcelentes) {
            this.proveedoresExcelentes = proveedoresExcelentes;
        }
    }
}

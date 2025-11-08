package com.farmacia.repository;

import com.farmacia.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    
    /**
     * Buscar usuario por nombre de usuario
     */
    Optional<Usuario> findByUsername(String username);
    
    /**
     * Verificar si existe un usuario con ese username
     */
    boolean existsByUsername(String username);
    
    /**
     * Obtener todos los usuarios activos
     */
    List<Usuario> findByActivoTrue();
    
    /**
     * Obtener todos los usuarios por rol
     */
    List<Usuario> findByRol(Usuario.Rol rol);
    
    /**
     * Obtener usuarios activos por rol
     */
    List<Usuario> findByActivoTrueAndRol(Usuario.Rol rol);
    
    /**
     * Contar usuarios activos
     */
    long countByActivoTrue();
}

package com.farmacia.service;

import com.farmacia.model.Usuario;
import com.farmacia.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UsuarioService {
    
    @Autowired
    private UsuarioRepository usuarioRepository;
    
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    
    /**
     * Autenticar usuario
     * @return Usuario si las credenciales son correctas, null si no
     */
    public Usuario login(String username, String password) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByUsername(username);
        
        if (usuarioOpt.isEmpty()) {
            return null; // Usuario no existe
        }
        
        Usuario usuario = usuarioOpt.get();
        
        if (!usuario.getActivo()) {
            return null; // Usuario desactivado
        }
        
        // Verificar password
        if (passwordEncoder.matches(password, usuario.getPassword())) {
            // Actualizar último acceso
            usuario.setUltimoAcceso(LocalDateTime.now());
            usuarioRepository.save(usuario);
            return usuario;
        }
        
        return null; // Password incorrecto
    }
    
    /**
     * Crear un nuevo usuario
     */
    public Usuario crearUsuario(String username, String password, String nombreCompleto, Usuario.Rol rol) {
        // Verificar que no exista
        if (usuarioRepository.existsByUsername(username)) {
            throw new RuntimeException("Ya existe un usuario con ese nombre de usuario");
        }
        
        Usuario usuario = new Usuario();
        usuario.setUsername(username);
        usuario.setPassword(passwordEncoder.encode(password)); // Hash del password
        usuario.setNombreCompleto(nombreCompleto);
        usuario.setRol(rol);
        usuario.setActivo(true);
        usuario.setFechaCreacion(LocalDateTime.now());
        
        return usuarioRepository.save(usuario);
    }
    
    /**
     * Actualizar un usuario existente (sin cambiar password)
     */
    public Usuario actualizarUsuario(Long id, String nombreCompleto, Usuario.Rol rol, Boolean activo) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        usuario.setNombreCompleto(nombreCompleto);
        usuario.setRol(rol);
        usuario.setActivo(activo);
        
        return usuarioRepository.save(usuario);
    }
    
    /**
     * Cambiar password de un usuario
     */
    public void cambiarPassword(Long id, String nuevoPassword) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        usuario.setPassword(passwordEncoder.encode(nuevoPassword));
        usuarioRepository.save(usuario);
    }
    
    /**
     * Cambiar password (verificando el anterior)
     */
    public boolean cambiarPassword(Long id, String passwordActual, String nuevoPassword) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        // Verificar password actual
        if (!passwordEncoder.matches(passwordActual, usuario.getPassword())) {
            return false; // Password actual incorrecto
        }
        
        usuario.setPassword(passwordEncoder.encode(nuevoPassword));
        usuarioRepository.save(usuario);
        return true;
    }
    
    /**
     * Obtener todos los usuarios
     */
    public List<Usuario> listarTodos() {
        return usuarioRepository.findAll();
    }
    
    /**
     * Obtener solo usuarios activos
     */
    public List<Usuario> listarActivos() {
        return usuarioRepository.findByActivoTrue();
    }
    
    /**
     * Obtener usuario por ID
     */
    public Optional<Usuario> obtenerPorId(Long id) {
        return usuarioRepository.findById(id);
    }
    
    /**
     * Obtener usuario por username
     */
    public Optional<Usuario> obtenerPorUsername(String username) {
        return usuarioRepository.findByUsername(username);
    }
    
    /**
     * Desactivar un usuario (soft delete)
     */
    public void desactivarUsuario(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        usuario.setActivo(false);
        usuarioRepository.save(usuario);
    }
    
    /**
     * Activar un usuario
     */
    public void activarUsuario(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        usuario.setActivo(true);
        usuarioRepository.save(usuario);
    }
    
    /**
     * Eliminar físicamente un usuario (usar con precaución)
     */
    public void eliminarUsuario(Long id) {
        usuarioRepository.deleteById(id);
    }
    
    /**
     * Contar usuarios activos
     */
    public long contarActivos() {
        return usuarioRepository.countByActivoTrue();
    }
    
    /**
     * Verificar si existe algún usuario en el sistema
     */
    public boolean existeAlgunUsuario() {
        return usuarioRepository.count() > 0;
    }
}

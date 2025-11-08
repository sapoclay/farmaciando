package com.farmacia.service;

import com.farmacia.model.Cliente;
import com.farmacia.repository.ClienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Servicio para gestionar operaciones de clientes
 */
@Service
@Transactional
public class ClienteService {

    @Autowired
    private ClienteRepository clienteRepository;

    /**
     * Registrar un nuevo cliente
     */
    public Cliente registrarCliente(Cliente cliente) throws Exception {
        // Validar datos requeridos
        validarDatosCliente(cliente);

        // Verificar si el documento ya existe
        if (clienteRepository.findByDocumento(cliente.getDocumento()).isPresent()) {
            throw new Exception("Ya existe un cliente con el documento: " + cliente.getDocumento());
        }

        return clienteRepository.save(cliente);
    }

    /**
     * Actualizar datos de un cliente
     */
    public Cliente actualizarCliente(Cliente cliente) throws Exception {
        // Validar que el cliente exista
        if (cliente.getId() == null || !clienteRepository.existsById(cliente.getId())) {
            throw new Exception("El cliente no existe");
        }

        // Validar datos
        validarDatosCliente(cliente);

        // Verificar si el documento está duplicado (excluyendo el propio cliente)
        if (clienteRepository.existeDocumentoDuplicado(cliente.getDocumento(), cliente.getId())) {
            throw new Exception("Ya existe otro cliente con el documento: " + cliente.getDocumento());
        }

        return clienteRepository.save(cliente);
    }

    /**
     * Eliminar un cliente (eliminación lógica)
     */
    public void eliminarCliente(Long id) throws Exception {
        Cliente cliente = obtenerClientePorId(id)
                .orElseThrow(() -> new Exception("Cliente no encontrado"));

        cliente.setActivo(false);
        clienteRepository.save(cliente);
    }

    /**
     * Reactivar un cliente
     */
    public Cliente reactivarCliente(Long id) throws Exception {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new Exception("Cliente no encontrado"));

        cliente.setActivo(true);
        return clienteRepository.save(cliente);
    }

    /**
     * Obtener cliente por ID
     */
    public Optional<Cliente> obtenerClientePorId(Long id) {
        return clienteRepository.findById(id);
    }

    /**
     * Obtener cliente por documento
     */
    public Optional<Cliente> obtenerClientePorDocumento(String documento) {
        return clienteRepository.findByDocumentoAndActivoTrue(documento);
    }

    /**
     * Obtener todos los clientes activos
     */
    public List<Cliente> obtenerClientesActivos() {
        return clienteRepository.findByActivoTrue();
    }

    /**
     * Buscar clientes por nombre o apellido
     */
    public List<Cliente> buscarPorNombre(String busqueda) {
        return clienteRepository.buscarPorNombre(busqueda);
    }

    /**
     * Buscar clientes por email
     */
    public List<Cliente> buscarPorEmail(String email) {
        return clienteRepository.findByEmailContainingIgnoreCaseAndActivoTrue(email);
    }

    /**
     * Buscar clientes por teléfono
     */
    public List<Cliente> buscarPorTelefono(String telefono) {
        return clienteRepository.findByTelefonoContainingAndActivoTrue(telefono);
    }

    /**
     * Buscar clientes por tipo de documento
     */
    public List<Cliente> buscarPorTipoDocumento(String tipoDocumento) {
        return clienteRepository.findByTipoDocumentoAndActivoTrueOrderByNombreAsc(tipoDocumento);
    }

    /**
     * Buscar clientes por ciudad
     */
    public List<Cliente> buscarPorCiudad(String ciudad) {
        return clienteRepository.findByCiudadContainingIgnoreCaseAndActivoTrueOrderByNombreAsc(ciudad);
    }

    /**
     * Búsqueda general (nombre, apellido, documento, email)
     */
    public List<Cliente> busquedaGeneral(String busqueda) {
        if (busqueda == null || busqueda.trim().isEmpty()) {
            return obtenerClientesActivos();
        }
        return clienteRepository.busquedaGeneral(busqueda.trim());
    }

    /**
     * Obtener últimos clientes registrados
     */
    public List<Cliente> obtenerUltimosClientes() {
        return clienteRepository.findTop10ByActivoTrueOrderByFechaRegistroDesc();
    }

    /**
     * Obtener estadísticas de clientes
     */
    public EstadisticasClientes obtenerEstadisticas() {
        EstadisticasClientes stats = new EstadisticasClientes();
        stats.setTotalClientes(clienteRepository.countByActivoTrue());
        stats.setTotalInactivos(clienteRepository.count() - stats.getTotalClientes());
        return stats;
    }

    /**
     * Validar datos básicos del cliente
     */
    private void validarDatosCliente(Cliente cliente) throws Exception {
        if (cliente.getNombre() == null || cliente.getNombre().trim().isEmpty()) {
            throw new Exception("El nombre es obligatorio");
        }
        if (cliente.getApellido() == null || cliente.getApellido().trim().isEmpty()) {
            throw new Exception("El apellido es obligatorio");
        }
        if (cliente.getDocumento() == null || cliente.getDocumento().trim().isEmpty()) {
            throw new Exception("El documento es obligatorio");
        }
        if (cliente.getTipoDocumento() == null || cliente.getTipoDocumento().trim().isEmpty()) {
            throw new Exception("El tipo de documento es obligatorio");
        }

        // Validar formato de email si está presente
        if (cliente.getEmail() != null && !cliente.getEmail().trim().isEmpty()) {
            if (!cliente.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                throw new Exception("El formato del email no es válido");
            }
        }
    }

    /**
     * Clase para estadísticas de clientes
     */
    public static class EstadisticasClientes {
        private Long totalClientes;
        private Long totalInactivos;

        public Long getTotalClientes() {
            return totalClientes;
        }

        public void setTotalClientes(Long totalClientes) {
            this.totalClientes = totalClientes;
        }

        public Long getTotalInactivos() {
            return totalInactivos;
        }

        public void setTotalInactivos(Long totalInactivos) {
            this.totalInactivos = totalInactivos;
        }
    }
}

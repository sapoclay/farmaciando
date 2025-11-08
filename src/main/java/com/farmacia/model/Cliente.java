package com.farmacia.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entidad que representa un cliente de la farmacia
 */
@Entity
@Table(name = "clientes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, length = 100)
    private String apellido;

    @Column(nullable = false, unique = true, length = 20)
    private String documento;

    @Column(name = "tipo_documento", nullable = false, length = 20)
    private String tipoDocumento; // DNI, NIE, PASAPORTE, CIF

    @Column(length = 20)
    private String telefono;

    @Column(length = 100)
    private String email;

    @Column(length = 200)
    private String direccion;

    @Column(length = 100)
    private String ciudad;

    @Column(name = "codigo_postal", length = 10)
    private String codigoPostal;

    @Column(name = "fecha_registro", nullable = false)
    private LocalDateTime fechaRegistro;

    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    @Column(nullable = false)
    private Boolean activo = true;

    @Column(length = 500)
    private String observaciones;

    /**
     * Se ejecuta antes de insertar en la base de datos
     */
    @PrePersist
    protected void onCreate() {
        fechaRegistro = LocalDateTime.now();
        fechaActualizacion = LocalDateTime.now();
        if (activo == null) {
            activo = true;
        }
    }

    /**
     * Se ejecuta antes de actualizar en la base de datos
     */
    @PreUpdate
    protected void onUpdate() {
        fechaActualizacion = LocalDateTime.now();
    }

    /**
     * Obtiene el nombre completo del cliente
     */
    public String getNombreCompleto() {
        return nombre + " " + apellido;
    }

    /**
     * Obtiene una representación corta para mostrar en listas
     */
    public String getRepresentacionCorta() {
        return String.format("%s - %s (%s)", getNombreCompleto(), documento, tipoDocumento);
    }

    @Override
    public String toString() {
        return getRepresentacionCorta();
    }
}

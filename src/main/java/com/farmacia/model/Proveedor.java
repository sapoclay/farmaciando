package com.farmacia.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "proveedores")
@Data
public class Proveedor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre es obligatorio")
    @Column(nullable = false)
    private String nombre;

    @NotBlank(message = "La empresa es obligatoria")
    @Column(nullable = false)
    private String empresa;

    @Email(message = "Email inválido")
    @Column(unique = true)
    private String email;

    @Column(length = 20)
    private String telefono;

    @Column(length = 20)
    private String telefonoSecundario;

    private String direccion;

    private String ciudad;

    @Column(length = 10)
    private String codigoPostal;

    private String pais;

    @Column(length = 50)
    private String nif; // Número de Identificación Fiscal

    @Column(columnDefinition = "TEXT")
    private String productosQueOfrece; // Tipos de productos que suministra

    @Column(columnDefinition = "TEXT")
    private String condicionesPago; // Ej: "30 días", "Pago al contado", etc.

    private Integer diasEntrega; // Días habituales de entrega

    @Column(columnDefinition = "TEXT")
    private String observaciones;

    @Column(nullable = false)
    private Boolean activo = true;

    @Column(name = "fecha_registro", nullable = false, updatable = false)
    private LocalDateTime fechaRegistro;

    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    @Column(length = 100)
    private String personaContacto; // Nombre de la persona de contacto

    @Column(length = 100)
    private String sitioWeb;

    // Calificación del proveedor (1-5 estrellas)
    @Column(columnDefinition = "INTEGER DEFAULT 0")
    private Integer calificacion;

    @PrePersist
    protected void onCreate() {
        fechaRegistro = LocalDateTime.now();
        fechaActualizacion = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        fechaActualizacion = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return empresa + " - " + nombre;
    }
}

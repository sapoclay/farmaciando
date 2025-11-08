package com.farmacia.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Clase para representar alertas del sistema
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Alerta {
    
    public enum TipoAlerta {
        STOCK_BAJO("Stock Bajo", "warning"),
        PRODUCTO_CADUCADO("Producto Caducado", "error"),
        PROXIMO_CADUCAR("Próximo a Caducar", "warning"),
        PEDIDO_PENDIENTE("Pedido Pendiente", "info"),
        PEDIDO_RETRASADO("Pedido Retrasado", "error");
        
        private final String descripcion;
        private final String nivel; // info, warning, error
        
        TipoAlerta(String descripcion, String nivel) {
            this.descripcion = descripcion;
            this.nivel = nivel;
        }
        
        public String getDescripcion() {
            return descripcion;
        }
        
        public String getNivel() {
            return nivel;
        }
    }
    
    private TipoAlerta tipo;
    private String mensaje;
    private String detalle;
    private LocalDateTime fecha;
    private Object entidadRelacionada; // Producto o Pedido
    private Long entidadId;
    private boolean critica;
    
    public Alerta(TipoAlerta tipo, String mensaje, String detalle, Object entidadRelacionada, Long entidadId) {
        this.tipo = tipo;
        this.mensaje = mensaje;
        this.detalle = detalle;
        this.fecha = LocalDateTime.now();
        this.entidadRelacionada = entidadRelacionada;
        this.entidadId = entidadId;
        this.critica = tipo.getNivel().equals("error");
    }
    
    public String getNivelColor() {
        return switch (tipo.getNivel()) {
            case "error" -> "#dc3545";    // Rojo
            case "warning" -> "#ffc107";  // Amarillo
            case "info" -> "#17a2b8";     // Azul
            default -> "#6c757d";         // Gris
        };
    }
    
    public String getIcono() {
        return switch (tipo) {
            case STOCK_BAJO -> "📦";
            case PRODUCTO_CADUCADO -> "🔴";
            case PROXIMO_CADUCAR -> "⚠️";
            case PEDIDO_PENDIENTE -> "📋";
            case PEDIDO_RETRASADO -> "⏰";
        };
    }
}

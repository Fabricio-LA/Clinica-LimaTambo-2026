package com.clinica.limatambo.model;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "Insumos")
public class Insumo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_insumo")
    private Integer idInsumo;

    @Column(name = "nombre_insumo", nullable = false, length = 100)
    private String nombreInsumo;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "stock_actual")
    private Integer stockActual;

    @Column(name = "precio_unitario", precision = 10, scale = 2)
    private java.math.BigDecimal precioUnitario;

    @Column(name = "requiere_receta")
    private Boolean requiereReceta = false;

    @Column(name = "categoria", length = 50)
    private String categoria;

    @Column(name = "stock_minimo")
    private Integer stockMinimo;

    @Column(name = "unidad_medida", length = 20)
    private String unidadMedida;

    @OneToMany(mappedBy = "insumo", cascade = CascadeType.ALL)
    private List<MovimientoInventario> movimientos;

    // Getters y Setters
    public Integer getIdInsumo() { return idInsumo; }
    public void setIdInsumo(Integer idInsumo) { this.idInsumo = idInsumo; }

    public String getNombreInsumo() { return nombreInsumo; }
    public void setNombreInsumo(String nombreInsumo) { this.nombreInsumo = nombreInsumo; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public Integer getStockActual() { return stockActual; }
    public void setStockActual(Integer stockActual) { this.stockActual = stockActual; }

    public Integer getStockMinimo() { return stockMinimo; }
    public void setStockMinimo(Integer stockMinimo) { this.stockMinimo = stockMinimo; }

    public java.math.BigDecimal getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(java.math.BigDecimal precioUnitario) { this.precioUnitario = precioUnitario; }

    public Boolean getRequiereReceta() { return requiereReceta; }
    public void setRequiereReceta(Boolean requiereReceta) { this.requiereReceta = requiereReceta; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public String getUnidadMedida() { return unidadMedida; }
    public void setUnidadMedida(String unidadMedida) { this.unidadMedida = unidadMedida; }

    public List<MovimientoInventario> getMovimientos() { return movimientos; }
    public void setMovimientos(List<MovimientoInventario> movimientos) { this.movimientos = movimientos; }

    @Column(name = "imagen", length = 255)
    private String imagen;

    public String getImagen() { return imagen; }
    public void setImagen(String imagen) { this.imagen = imagen; }
}

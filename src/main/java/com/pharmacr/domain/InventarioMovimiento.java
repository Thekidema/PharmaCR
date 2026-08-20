package com.pharmacr.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;

// HU-17 bitacora de auditoria, cada entrada, salida o venta deja aca un registro con el usuario responsable
@Data
@Entity
@Table(name = "inventario_movimiento")
public class InventarioMovimiento implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_movimiento")
    private Integer idMovimiento;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_medicamento", nullable = false)
    private Medicamento medicamento;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_movimiento", nullable = false)
    private TipoMovimiento tipoMovimiento;

    @Column(nullable = false)
    private Integer cantidad;

    @Column(name = "stock_resultante", nullable = false)
    private Integer stockResultante;

    @Column(length = 300)
    private String motivo;

    //Id de la venta, entrada o salida que origino el movimiento
    @Column(name = "id_referencia")
    private Integer idReferencia;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;
}

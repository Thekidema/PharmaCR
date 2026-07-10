package com.pharmacr.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.math.BigDecimal;
import lombok.Data;

@Data
@Entity
@Table(name = "medicamento")
public class Medicamento implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idMedicamento;

    @ManyToOne
    @JoinColumn(name = "idCategoria")
    private CategoriaMedicamento categoria;

    @Column(nullable = false, length = 20)
    @NotNull
    @Size(max = 20)
    private String codigo;

    @Column(nullable = false, length = 100)
    @NotNull
    @Size(max = 100)
    private String nombre;

    @Column(nullable = false, length = 50)
    @NotNull
    @Size(max = 50)
    private String presentacion;

    @Column(nullable = false, precision = 12, scale = 2)
    @NotNull
    private BigDecimal precio;

    private Integer stockActual;

    private Integer stockMinimo;

    private boolean activo;
}

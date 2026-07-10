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
import java.time.LocalDate;
import lombok.Data;

@Data
@Entity
@Table(name = "entrada_inventario")
public class EntradaInventario implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idEntrada;

    @ManyToOne
    @JoinColumn(name = "idProveedor")
    private Proveedor proveedor;

    @ManyToOne
    @JoinColumn(name = "idMedicamento")
    private Medicamento medicamento;

    @ManyToOne
    @JoinColumn(name = "idUsuario")
    private Usuario usuario;

    @Column(nullable = false, length = 50)
    @NotNull
    @Size(max = 50)
    private String lote;

    @Column(nullable = false)
    @NotNull
    private LocalDate fechaVencimiento;

    @Column(nullable = false)
    @NotNull
    private Integer cantidad;

    @Column(length = 300)
    @Size(max = 300)
    private String observaciones;
}

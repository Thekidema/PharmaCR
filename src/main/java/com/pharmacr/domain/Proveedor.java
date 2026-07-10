package com.pharmacr.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import lombok.Data;

@Data
@Entity
@Table(name = "proveedor")
public class Proveedor implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idProveedor;

    @Column(nullable = false, length = 100)
    @NotNull
    @Size(max = 100)
    private String nombreComercial;

    @Column(length = 100)
    @Size(max = 100)
    private String contacto;

    @Column(length = 25)
    @Size(max = 25)
    private String telefono;

    @Column(length = 75)
    @Size(max = 75)
    private String correo;

    private boolean activo;
}

package com.pharmacr.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.util.List;
import lombok.Data;

@Data
@Entity
@Table(name = "usuario")
public class Usuario implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idUsuario;

    @Column(nullable = false, length = 30)
    @NotNull
    @Size(max = 30)
    private String username;

    @Column(nullable = false, length = 512)
    @NotNull
    @Size(max = 512)
    private String password;

    @Column(nullable = false, length = 50)
    @NotNull
    @Size(max = 50)
    private String nombre;

    @Column(nullable = false, length = 50)
    @NotNull
    @Size(max = 50)
    private String apellidos;

    @Column(nullable = false, length = 75)
    @NotNull
    @Size(max = 75)
    private String correo;

    @Column(length = 25)
    @Size(max = 25)
    private String telefono;

    @Column(length = 1024)
    @Size(max = 1024)
    private String rutaImagen;

    private boolean activo;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "usuario_rol",
            joinColumns = @JoinColumn(name = "idUsuario"),
            inverseJoinColumns = @JoinColumn(name = "idRol"))
    private List<Rol> roles;
}

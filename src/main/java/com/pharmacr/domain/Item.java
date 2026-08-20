package com.pharmacr.domain;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import lombok.Data;

@Data
public class Item implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer idMedicamento;
    private String codigo;
    private String nombre;
    private BigDecimal precio;
    private Integer cantidad;

    public Item() {
    }

    public Item(Medicamento medicamento, Integer cantidad) {
        this.idMedicamento = medicamento.getIdMedicamento();
        this.codigo = medicamento.getCodigo();
        this.nombre = medicamento.getNombre();
        this.precio = medicamento.getPrecio();
        this.cantidad = cantidad;
    }

    public BigDecimal getSubtotal() {
        if (precio == null || cantidad == null) {
            return BigDecimal.ZERO;
        }
        return precio.multiply(BigDecimal.valueOf(cantidad));
    }

    public static BigDecimal sumarTotal(List<Item> items) {
        return items.stream()
                .map(Item::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}

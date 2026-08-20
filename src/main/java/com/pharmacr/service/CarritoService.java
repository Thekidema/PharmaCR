package com.pharmacr.service;

import com.pharmacr.domain.Item;
import com.pharmacr.repository.MedicamentoRepository;
import jakarta.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CarritoService {

    private static final String CARRITO = "carrito";

    private final HttpSession session;
    private final MedicamentoRepository medicamentoRepository;
    private final MedicamentoService medicamentoService;

    public CarritoService(HttpSession session, MedicamentoRepository medicamentoRepository,
            MedicamentoService medicamentoService) {
        this.session = session;
        this.medicamentoRepository = medicamentoRepository;
        this.medicamentoService = medicamentoService;
    }

    @SuppressWarnings("unchecked")
    public List<Item> obtenerCarrito() {
        var carrito = (List<Item>) session.getAttribute(CARRITO);
        if (carrito == null) {
            carrito = new ArrayList<>();
            session.setAttribute(CARRITO, carrito);
        }
        return carrito;
    }

    private void guardarCarrito(List<Item> carrito) {
        session.setAttribute(CARRITO, carrito);
    }


    private Item buscarItem(Integer idMedicamento) {
        return obtenerCarrito().stream()
                .filter(item -> item.getIdMedicamento().equals(idMedicamento))
                .findFirst()
                .orElse(null);
    }

   
    @Transactional(readOnly = true)
    public void agregarMedicamento(Integer idMedicamento, Integer cantidad) {
        if (cantidad == null || cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a cero.");
        }
        var medicamento = medicamentoRepository.findById(idMedicamento)
                .orElseThrow(() -> new IllegalArgumentException("El medicamento seleccionado no existe."));

        var carrito = obtenerCarrito();
        var item = buscarItem(idMedicamento);
        //Se valida contra el total que quedaria en el carrito, no solo contra lo que se agrega
        int cantidadFinal = (item == null) ? cantidad : item.getCantidad() + cantidad;

        medicamentoService.validarDisponibilidad(medicamento, cantidadFinal);

        if (item == null) {
            carrito.add(new Item(medicamento, cantidad));
        } else {
            item.setCantidad(cantidadFinal);
        }
        guardarCarrito(carrito);
    }

    public void eliminarItem(Integer idMedicamento) {
        var carrito = obtenerCarrito();
        carrito.removeIf(item -> item.getIdMedicamento().equals(idMedicamento));
        guardarCarrito(carrito);
    }

    public void vaciarCarrito() {
        session.removeAttribute(CARRITO);
    }


    public void restaurarCarrito(List<Item> items) {
        guardarCarrito(items);
    }

    public BigDecimal calcularTotal() {
        return Item.sumarTotal(obtenerCarrito());
    }

    public int contarUnidades() {
        return obtenerCarrito().stream()
                .mapToInt(Item::getCantidad)
                .sum();
    }
}

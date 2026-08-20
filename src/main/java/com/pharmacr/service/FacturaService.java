package com.pharmacr.service;

import com.pharmacr.domain.DetalleVenta;
import com.pharmacr.domain.Venta;
import jakarta.mail.MessagingException;
import java.util.List;
import java.util.Locale;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.stereotype.Service;

@Service
public class FacturaService {

    private final CorreoService correoService;
    private final MessageSource messageSource;

    public FacturaService(CorreoService correoService, MessageSource messageSource) {
        this.correoService = correoService;
        this.messageSource = messageSource;
    }

    //Envia un resumen simple de la factura al correo del cliente, si se HU-18
    public boolean enviarFactura(Venta venta, List<DetalleVenta> detalles, String correoCliente) {
        if (correoCliente == null || correoCliente.isBlank()) {
            return true;
        }
        var locale = Locale.getDefault();
        try {
            String asunto = String.format(
                    messageSource.getMessage("factura.correo.asunto", null, locale), venta.getIdVenta());
            String cuerpo = construirCuerpo(venta, detalles, locale);
            correoService.enviarCorreoHtml(correoCliente, asunto, cuerpo);
            return true;
        } catch (MessagingException | NoSuchMessageException e) {
            return false;
        }
    }

    private String construirCuerpo(Venta venta, List<DetalleVenta> detalles, Locale locale) {
        var filas = new StringBuilder();
        for (DetalleVenta d : detalles) {
            filas.append("<tr>")
                    .append("<td>").append(d.getMedicamento().getNombre()).append("</td>")
                    .append("<td style='text-align:center;'>").append(d.getCantidad()).append("</td>")
                    .append("<td style='text-align:right;'>₡").append(d.getPrecioUnitario()).append("</td>")
                    .append("<td style='text-align:right;'>₡").append(d.getSubtotal()).append("</td>")
                    .append("</tr>");
        }

        String encabezado = String.format(messageSource.getMessage("factura.correo.encabezado", null, locale),
                venta.getIdVenta(), venta.getFecha(), venta.getUsuario().getNombre());

        return "<h1>" + messageSource.getMessage("aplicacion.titulo", null, locale) + "</h1>"
                + "<hr>" + encabezado
                + "<table border='1' cellpadding='6' cellspacing='0' style='border-collapse:collapse;width:100%;'>"
                + "<thead><tr>"
                + "<th>" + messageSource.getMessage("medicamento.nombre", null, locale) + "</th>"
                + "<th>" + messageSource.getMessage("inventario.cantidad", null, locale) + "</th>"
                + "<th>" + messageSource.getMessage("medicamento.precio", null, locale) + "</th>"
                + "<th>" + messageSource.getMessage("venta.subtotal", null, locale) + "</th>"
                + "</tr></thead><tbody>" + filas + "</tbody></table>"
                + "<h3 style='text-align:right;'>" + messageSource.getMessage("venta.total", null, locale)
                + ": ₡" + venta.getTotal() + "</h3>"
                + "<hr><p>" + messageSource.getMessage("venta.comprobante.gracias", null, locale) + "</p>";
    }
}

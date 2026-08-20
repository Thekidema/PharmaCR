package com.pharmacr.service;

import com.pharmacr.domain.CodigoVerificacion;
import com.pharmacr.domain.Usuario;
import com.pharmacr.repository.CodigoVerificacionRepository;
import com.pharmacr.repository.UsuarioRepository;
import jakarta.mail.MessagingException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Locale;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RecuperacionClaveService {

    private static final int MINUTOS_EXPIRACION = 15;
    private final SecureRandom random = new SecureRandom();

    private final UsuarioRepository usuarioRepository;
    private final CodigoVerificacionRepository codigoVerificacionRepository;
    private final CorreoService correoService;
    private final PasswordEncoder passwordEncoder;
    private final MessageSource messageSource;

    public RecuperacionClaveService(UsuarioRepository usuarioRepository,
            CodigoVerificacionRepository codigoVerificacionRepository, CorreoService correoService,
            PasswordEncoder passwordEncoder, MessageSource messageSource) {
        this.usuarioRepository = usuarioRepository;
        this.codigoVerificacionRepository = codigoVerificacionRepository;
        this.correoService = correoService;
        this.passwordEncoder = passwordEncoder;
        this.messageSource = messageSource;
    }

    @Transactional
    public void solicitarCodigo(String correo) {
        usuarioRepository.findByCorreo(correo)
                .filter(Usuario::isActivo)
                .ifPresent(usuario -> {
                    codigoVerificacionRepository.findByUsuarioAndUsadoFalse(usuario)
                            .forEach(c -> c.setUsado(true));

                    var codigo = new CodigoVerificacion();
                    codigo.setUsuario(usuario);
                    codigo.setCodigo(generarCodigo());
                    codigo.setFechaExpiracion(LocalDateTime.now().plusMinutes(MINUTOS_EXPIRACION));
                    codigo.setUsado(false);
                    codigoVerificacionRepository.save(codigo);

                    enviarCorreoCodigo(usuario, codigo.getCodigo());
                });
    }

    //Valida el codigo y, si es correcto, cifra y guarda la nueva clave,   y marca el codigo como usado.
    @Transactional
    public void confirmarReseteo(String correo, String codigo, String nuevaClave) {
        var usuario = usuarioRepository.findByCorreo(correo)
                .filter(Usuario::isActivo)
                .orElseThrow(() -> new IllegalArgumentException("Código inválido o expirado."));

        var codigoVerificacion = codigoVerificacionRepository
                .findByUsuarioAndCodigoAndUsadoFalseAndFechaExpiracionAfter(usuario, codigo, LocalDateTime.now())
                .orElseThrow(() -> new IllegalArgumentException("Código inválido o expirado."));

        usuario.setPassword(passwordEncoder.encode(nuevaClave));
        usuarioRepository.save(usuario);

        codigoVerificacion.setUsado(true);
        codigoVerificacionRepository.save(codigoVerificacion);
    }

    private String generarCodigo() {
        return String.format("%06d", random.nextInt(1_000_000));
    }

    //Mismo patron que AlertaService.notificarPorCorreo: un fallo de correo no
    private void enviarCorreoCodigo(Usuario usuario, String codigo) {
        var locale = Locale.getDefault();
        try {
            String asunto = messageSource.getMessage("recuperar.correo.asunto", null, locale);
            String cuerpo = String.format(messageSource.getMessage("recuperar.correo.cuerpo", null, locale),
                    usuario.getNombre(), codigo, MINUTOS_EXPIRACION);
            correoService.enviarCorreoHtml(usuario.getCorreo(), asunto, cuerpo);
        } catch (MessagingException | NoSuchMessageException e) {
        }
    }
}

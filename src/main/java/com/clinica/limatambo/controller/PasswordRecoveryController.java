package com.clinica.limatambo.controller;

import com.clinica.limatambo.model.PasswordResetToken;
import com.clinica.limatambo.model.Usuario;
import com.clinica.limatambo.repository.PasswordResetTokenRepository;
import com.clinica.limatambo.repository.UsuarioRepository;
import com.clinica.limatambo.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Controller
public class PasswordRecoveryController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordResetTokenRepository tokenRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/forgot-password")
    public String mostrarFormularioRecuperacion() {
        return "forgot-password";
    }

    @PostMapping("/forgot-password")
    public String procesarRecuperacion(@RequestParam("email") String email, HttpServletRequest request, Model model) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(email);

        if (!usuarioOpt.isPresent()) {
            model.addAttribute("error", "No se encontró ningún usuario con ese correo electrónico.");
            return "forgot-password";
        }

        Usuario usuario = usuarioOpt.get();
        String token = UUID.randomUUID().toString();

        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken(token);
        resetToken.setIdUsuario(usuario.getIdUsuario());
        resetToken.setFechaExpiracion(LocalDateTime.now().plusHours(2)); // Expira en 2 horas

        tokenRepository.save(resetToken);
        String urlBase = request.getRequestURL().toString().replace(request.getRequestURI(), "");
        String enlaceRecuperacion = urlBase + "/reset-password?token=" + token;

        try {
            emailService.enviarCorreoRecuperacion(usuario.getEmail(), enlaceRecuperacion);
            model.addAttribute("mensaje",
                    "Te hemos enviado un correo con las instrucciones para recuperar tu contraseña.");
        } catch (Exception e) {
            model.addAttribute("error", "Error al enviar el correo. Revisa la configuración del servidor SMTP.");
        }

        return "forgot-password";
    }

    @GetMapping("/reset-password")
    public String mostrarFormularioReset(@RequestParam("token") String token, Model model) {
        Optional<PasswordResetToken> tokenOpt = tokenRepository.findByToken(token);

        if (!tokenOpt.isPresent() || tokenOpt.get().getFechaExpiracion().isBefore(LocalDateTime.now())) {
            model.addAttribute("error", "El enlace es inválido o ha expirado.");
            return "reset-password";
        }

        model.addAttribute("token", token);
        return "reset-password";
    }

    @PostMapping("/reset-password")
    public String procesarReset(@RequestParam("token") String token,
            @RequestParam("password") String password,
            Model model) {

        if (password == null || !password.matches("^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[\\W_]).{8,}$")) {
            model.addAttribute("error", "La contraseña debe tener al menos 8 caracteres, mayúscula, minúscula, número y un carácter especial.");
            model.addAttribute("token", token);
            return "reset-password";
        }

        Optional<PasswordResetToken> tokenOpt = tokenRepository.findByToken(token);

        if (!tokenOpt.isPresent() || tokenOpt.get().getFechaExpiracion().isBefore(LocalDateTime.now())) {
            model.addAttribute("error", "El enlace es inválido o ha expirado.");
            return "reset-password";
        }

        PasswordResetToken resetToken = tokenOpt.get();
        Optional<Usuario> usuarioOpt = usuarioRepository.findById(resetToken.getIdUsuario());

        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();
            usuario.setPassword(passwordEncoder.encode(password));
            usuarioRepository.save(usuario);
            tokenRepository.delete(resetToken);

            return "redirect:/login?resetSuccess=true";
        }

        model.addAttribute("error", "Error inesperado al restablecer la contraseña.");
        return "reset-password";
    }
}



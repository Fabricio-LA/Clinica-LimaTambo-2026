package com.clinica.limatambo.controller;

import com.clinica.limatambo.repository.EspecialidadRepository;
import com.clinica.limatambo.repository.MedicoRepository;
import com.clinica.limatambo.repository.UsuarioRepository;
import com.clinica.limatambo.repository.PacienteRepository;
import com.clinica.limatambo.repository.InsumoRepository;
import com.clinica.limatambo.repository.PagoRepository;
import com.clinica.limatambo.model.Usuario;
import com.clinica.limatambo.model.Paciente;
import com.clinica.limatambo.model.Pago;
import com.clinica.limatambo.service.DescuentoService;
import java.math.BigDecimal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.ResponseEntity;
import java.util.Optional;
import java.time.LocalDateTime;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import java.util.List;
import java.util.Map;
import com.clinica.limatambo.model.Insumo;

@Controller
public class HomeController {

    @Autowired
    private EspecialidadRepository especialidadRepository;

    @Autowired
    private MedicoRepository medicoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired
    private DescuentoService descuentoService;

    @Autowired
    private com.clinica.limatambo.service.EmailService emailService;

    @Autowired
    private InsumoRepository insumoRepository;

    @Autowired
    private PagoRepository pagoRepository;

    @GetMapping("/")
    public String inicio(Model model) {
        model.addAttribute("especialidades", especialidadRepository.findAll());
        
        java.util.List<com.clinica.limatambo.model.Medico> medicosDB = medicoRepository.findAll();
        java.util.List<MedicoIndexDTO> medicos = new java.util.ArrayList<>();
        
        for (com.clinica.limatambo.model.Medico m : medicosDB) {
            String especialidadNombre = "General";
            if (m.getIdEspecialidad() != null) {
                java.util.Optional<com.clinica.limatambo.model.Especialidad> espOpt = especialidadRepository.findById(m.getIdEspecialidad());
                if (espOpt.isPresent()) {
                    especialidadNombre = espOpt.get().getNombreEspecialidad();
                }
            }
            medicos.add(new MedicoIndexDTO(m, especialidadNombre));
        }
        
        model.addAttribute("medicos", medicos);
        return "index";
    }

    @GetMapping("/dashboard-router")
    public String dashboardRouter(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            boolean isPaciente = authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_PACIENTE"));
            boolean isMedico = authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_MEDICO"));
            if (isPaciente) return "redirect:/paciente/dashboard";
            if (isMedico) return "redirect:/medico/dashboard";
            return "redirect:/admin/dashboard";
        }
        return "redirect:/login";
    }

    @GetMapping("/ayuda")
    public String mostrarAyuda() {
        return "ayuda";
    }

    @GetMapping("/terminos")
    public String mostrarTerminos() {
        return "terminos";
    }

    @GetMapping("/paciente/reservar")
    public String mostrarWizardReserva(Model model, Authentication authentication) {
        model.addAttribute("especialidades", especialidadRepository.findAll());
        model.addAttribute("medicos", medicoRepository.findAll());
        
        String tipoSeguro = "PARTICULAR";
        double descuento = 0.0;
        
        if (authentication != null && authentication.isAuthenticated()) {
            Optional<Usuario> usuarioOpt = usuarioRepository.findByUsername(authentication.getName());
            if (usuarioOpt.isPresent()) {
                Optional<Paciente> pacienteOpt = pacienteRepository.findByIdUsuario(usuarioOpt.get().getIdUsuario());
                if (pacienteOpt.isPresent() && pacienteOpt.get().getTipoSeguro() != null && !pacienteOpt.get().getTipoSeguro().isEmpty()) {
                    tipoSeguro = pacienteOpt.get().getTipoSeguro().toUpperCase();
                }
            }
        }
        
        descuento = descuentoService.obtenerPorcentajeDescuento(tipoSeguro);
        
        model.addAttribute("tipoSeguro", tipoSeguro);
        model.addAttribute("descuentoSeguro", descuento);
        
        return "reserva-wizard";
    }

    public static class MedicoIndexDTO {
        private com.clinica.limatambo.model.Medico medico;
        private String especialidadNombre;

        public MedicoIndexDTO(com.clinica.limatambo.model.Medico medico, String especialidadNombre) {
            this.medico = medico;
            this.especialidadNombre = especialidadNombre;
        }

        public com.clinica.limatambo.model.Medico getMedico() { return medico; }
        public String getEspecialidadNombre() { return especialidadNombre; }
    }
}

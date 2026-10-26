package com.clinica.limatambo.controller;

import com.clinica.limatambo.model.Cita;
import com.clinica.limatambo.model.Medico;
import com.clinica.limatambo.model.Paciente;
import com.clinica.limatambo.model.Usuario;
import com.clinica.limatambo.repository.CitaRepository;
import com.clinica.limatambo.repository.MedicoRepository;
import com.clinica.limatambo.repository.PacienteRepository;
import com.clinica.limatambo.repository.UsuarioRepository;
import com.clinica.limatambo.repository.EspecialidadRepository;
import com.clinica.limatambo.model.Especialidad;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
public class DashboardController {

    @Autowired
    private CitaRepository citaRepository;

    @Autowired
    private MedicoRepository medicoRepository;

    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private EspecialidadRepository especialidadRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;


    @GetMapping("/medico/dashboard")
    public String medicoPanel(Authentication authentication, Model model) {
        String username = authentication.getName();
        Optional<Usuario> usuarioOpt = usuarioRepository.findByUsername(username);
        
        if (usuarioOpt.isPresent()) {
            Optional<Medico> medicoOpt = medicoRepository.findByIdUsuario(usuarioOpt.get().getIdUsuario());
            if (medicoOpt.isPresent()) {
                Medico medico = medicoOpt.get();
                List<Cita> citas = citaRepository.findByIdMedicoOrderByFechaCitaAscHoraCitaAsc(medico.getIdMedico());
                
                LocalDate hoy = LocalDate.now();
                List<CitaDTO> citasHoy = new ArrayList<>();
                List<CitaDTO> alertasHoy = new ArrayList<>();
                List<CitaDTO> citasProximas = new ArrayList<>();
                List<CitaDTO> citasAtendidas = new ArrayList<>();
                
                int totalAtendidosHoy = 0;
                
                String especialidadNombre = "Especialidad no definida";
                if (medico.getIdEspecialidad() != null) {
                    Optional<Especialidad> espOpt = especialidadRepository.findById(medico.getIdEspecialidad());
                    if (espOpt.isPresent()) {
                        especialidadNombre = espOpt.get().getNombreEspecialidad();
                    }
                }
                
                for (Cita cita : citas) {
                    Paciente pacienteObj = null;
                    String nombrePaciente = "Desconocido";
                    Integer edad = null;
                    
                    if (cita.getIdPaciente() != null) {
                        Optional<Paciente> p = pacienteRepository.findById(cita.getIdPaciente());
                        if (p.isPresent()) {
                            pacienteObj = p.get();
                            nombrePaciente = p.get().getNombre() + " " + p.get().getApellido();
                            if (p.get().getFechaNacimiento() != null) {
                                edad = Period.between(p.get().getFechaNacimiento(), hoy).getYears();
                            }
                        }
                    }
                    
                    CitaDTO dto = new CitaDTO(cita, nombrePaciente, pacienteObj, edad);
                    
                    if (Boolean.TRUE.equals(cita.getNotificacionMedico())) {
                        alertasHoy.add(dto);
                    }
                    
                    if ("Atendida".equals(cita.getEstado())) {
                        citasAtendidas.add(dto);
                        if (cita.getFechaCita().equals(hoy)) {
                            totalAtendidosHoy++;
                            citasHoy.add(dto);
                        }
                    } else if (cita.getFechaCita().equals(hoy)) {
                        if (!"Cancelada".equals(cita.getEstado()) && !"Cancelada_Medico".equals(cita.getEstado())) {
                            citasHoy.add(dto);
                        }
                    } else if (cita.getFechaCita().isAfter(hoy)) {
                        if (!"Cancelada".equals(cita.getEstado()) && !"Cancelada_Medico".equals(cita.getEstado())) {
                            citasProximas.add(dto);
                        }
                    }
                }
                
                java.util.Collections.reverse(citasAtendidas);
                model.addAttribute("medico", medico);
                model.addAttribute("citasHoy", citasHoy);
                model.addAttribute("citasProximas", citasProximas);
                model.addAttribute("citasAtendidas", citasAtendidas);
                model.addAttribute("alertasHoy", alertasHoy);
                model.addAttribute("totalCitasHoy", citasHoy.size());
                model.addAttribute("totalAtendidosHoy", totalAtendidosHoy);
                model.addAttribute("especialidadNombre", especialidadNombre);
            }
        }
        return "medico-dashboard";
    }

    @PostMapping("/medico/horario")
    public String actualizarHorario(@RequestParam("dias") String dias,
                                    @RequestParam("horaInicio") String horaInicio,
                                    @RequestParam("horaFin") String horaFin,
                                    Authentication authentication) {
        String username = authentication.getName();
        Optional<Usuario> usuarioOpt = usuarioRepository.findByUsername(username);
        if (usuarioOpt.isPresent()) {
            Optional<Medico> medicoOpt = medicoRepository.findByIdUsuario(usuarioOpt.get().getIdUsuario());
            if (medicoOpt.isPresent()) {
                Medico medico = medicoOpt.get();
                medico.setDiasLaborables(dias);
                medico.setHoraInicio(LocalTime.parse(horaInicio));
                medico.setHoraFin(LocalTime.parse(horaFin));
                medicoRepository.save(medico);
                return "redirect:/medico/dashboard?horarioActualizado=true";
            }
        }
        return "redirect:/medico/dashboard?error=true";
    }

    @GetMapping("/paciente/dashboard")
    public String pacientePanel(Authentication authentication, Model model) {
        String username = authentication.getName();
        Optional<Usuario> usuarioOpt = usuarioRepository.findByUsername(username);
        
        if (usuarioOpt.isPresent()) {
            Optional<Paciente> pacienteOpt = pacienteRepository.findByIdUsuario(usuarioOpt.get().getIdUsuario());
            if (pacienteOpt.isPresent()) {
                Paciente paciente = pacienteOpt.get();
                List<Cita> citas = citaRepository.findByIdPacienteOrderByFechaCitaDesc(paciente.getIdPaciente());
                
                List<CitaPacienteDTO> citasDTO = new ArrayList<>();
                for (Cita cita : citas) {
                    String nombreMedico = "Desconocido";
                    String especialidad = "Consulta General";
                    Medico medicoObj = null;
                    if (cita.getIdMedico() != null) {
                        Optional<Medico> m = medicoRepository.findById(cita.getIdMedico());
                        if (m.isPresent()) {
                            medicoObj = m.get();
                            nombreMedico = "Dr. " + m.get().getNombre() + " " + m.get().getApellido();
                            if (m.get().getIdEspecialidad() != null) {
                                Optional<Especialidad> esp = especialidadRepository.findById(m.get().getIdEspecialidad());
                                if (esp.isPresent()) {
                                    especialidad = esp.get().getNombreEspecialidad();
                                }
                            }
                        }
                    }
                    citasDTO.add(new CitaPacienteDTO(cita, nombreMedico, medicoObj, especialidad));
                }
                
                model.addAttribute("paciente", paciente);
                model.addAttribute("usuario", usuarioOpt.get());
                model.addAttribute("citas", citasDTO);
            }
        }
        return "paciente-dashboard";
    }

    @PostMapping("/paciente/perfil")
    public String actualizarPerfilPaciente(
            @RequestParam("telefono") String telefono,
            @RequestParam("direccion") String direccion,
            @RequestParam("email") String email,
            @RequestParam(value = "foto", required = false) MultipartFile foto,
            Authentication authentication) {
        String username = authentication.getName();
        Optional<Usuario> usuarioOpt = usuarioRepository.findByUsername(username);
        
        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();
            usuario.setEmail(email);
            usuarioRepository.save(usuario);
            
            Optional<Paciente> pacienteOpt = pacienteRepository.findByIdUsuario(usuario.getIdUsuario());
            if (pacienteOpt.isPresent()) {
                Paciente paciente = pacienteOpt.get();
                paciente.setTelefono(telefono);
                paciente.setDireccion(direccion);
                
                if (foto != null && !foto.isEmpty()) {
                    try {
                        String uploadDir = "uploads/";
                        Path uploadPath = Paths.get(uploadDir);
                        if (!Files.exists(uploadPath)) {
                            Files.createDirectories(uploadPath);
                        }
                        String fileName = System.currentTimeMillis() + "_" + foto.getOriginalFilename();
                        Path filePath = uploadPath.resolve(fileName);
                        Files.copy(foto.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
                        paciente.setFotoPerfil("/uploads/" + fileName);
                    } catch (IOException e) {
                        e.printStackTrace();
                        return "redirect:/paciente/dashboard?errorUpload=true";
                    }
                }
                pacienteRepository.save(paciente);
                return "redirect:/paciente/dashboard?perfilActualizado=true";
            }
        }
        return "redirect:/paciente/dashboard?error=true";
    }

    @PostMapping("/paciente/password")
    public String actualizarPasswordPaciente(
            @RequestParam("currentPassword") String currentPassword,
            @RequestParam("newPassword") String newPassword,
            Authentication authentication) {
        
        if (newPassword == null || !newPassword.matches("^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[\\W_]).{8,}$")) {
            return "redirect:/paciente/dashboard?error=La contraseña no cumple con los requisitos de seguridad.";
        }

        String username = authentication.getName();
        Optional<Usuario> usuarioOpt = usuarioRepository.findByUsername(username);
        
        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();
            if (passwordEncoder.matches(currentPassword, usuario.getPassword())) {
                usuario.setPassword(passwordEncoder.encode(newPassword));
                usuarioRepository.save(usuario);
                return "redirect:/paciente/dashboard?passwordActualizado=true";
            } else {
                return "redirect:/paciente/dashboard?errorPassword=true";
            }
        }
        return "redirect:/paciente/dashboard?error=true";
    }

    public static class CitaDTO {
        private Cita cita;
        private String nombrePaciente;
        private Paciente paciente;
        private Integer edadPaciente;

        public CitaDTO(Cita cita, String nombrePaciente, Paciente paciente, Integer edadPaciente) {
            this.cita = cita;
            this.nombrePaciente = nombrePaciente;
            this.paciente = paciente;
            this.edadPaciente = edadPaciente;
        }

        public Cita getCita() { return cita; }
        public String getNombrePaciente() { return nombrePaciente; }
        public Paciente getPaciente() { return paciente; }
        public Integer getEdadPaciente() { return edadPaciente; }
    }

    public static class CitaPacienteDTO {
        private Cita cita;
        private String nombreMedico;
        private Medico medico;
        private String especialidad;

        public CitaPacienteDTO(Cita cita, String nombreMedico, Medico medico, String especialidad) {
            this.cita = cita;
            this.nombreMedico = nombreMedico;
            this.medico = medico;
            this.especialidad = especialidad;
        }

        public Cita getCita() { return cita; }
        public String getNombreMedico() { return nombreMedico; }
        public Medico getMedico() { return medico; }
        public String getEspecialidad() { return especialidad; }
    }
}

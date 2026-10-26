package com.clinica.limatambo.controller;

import com.clinica.limatambo.model.Cita;
import com.clinica.limatambo.model.Medico;
import com.clinica.limatambo.model.Paciente;
import com.clinica.limatambo.model.Usuario;
import com.clinica.limatambo.repository.CitaRepository;
import com.clinica.limatambo.repository.MedicoRepository;
import com.clinica.limatambo.repository.PacienteRepository;
import com.clinica.limatambo.repository.UsuarioRepository;
import com.clinica.limatambo.repository.PagoRepository;
import com.clinica.limatambo.repository.EspecialidadRepository;
import com.clinica.limatambo.model.Especialidad;
import com.clinica.limatambo.model.Pago;
import com.clinica.limatambo.service.DescuentoService;
import java.math.BigDecimal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

@Controller
@RequestMapping("/citas")
public class CitaController {

    @Autowired
    private CitaRepository citaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PagoRepository pagoRepository;

    @Autowired
    private DescuentoService descuentoService;

    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired
    private MedicoRepository medicoRepository;
    
    @Autowired
    private EspecialidadRepository especialidadRepository;
    
    @Autowired
    private com.clinica.limatambo.service.EmailService emailService;
    
    private double obtenerTarifaBase(String nombreEspecialidad) {
        if (nombreEspecialidad == null) return 100.0;
        switch (nombreEspecialidad.toLowerCase()) {
            case "medicina general": return 80.0;
            case "odontología":
            case "odontologia": return 90.0;
            case "pediatría":
            case "pediatria": return 100.0;
            case "dermatología":
            case "dermatologia": return 110.0;
            case "cardiología":
            case "cardiologia": return 120.0;
            case "ginecología":
            case "ginecologia": return 120.0;
            default: return 100.0;
        }
    }

    private boolean esHorarioValido(Integer idMedico, LocalDate fecha, LocalTime hora) {
        Optional<Medico> medicoOpt = medicoRepository.findById(idMedico);
        if (!medicoOpt.isPresent()) return false;
        
        Medico m = medicoOpt.get();
        if (m.getHoraInicio() == null || m.getHoraFin() == null || m.getDiasLaborables() == null) return false;
        int diaSemana = fecha.getDayOfWeek().getValue();
        String[] diasPermitidos = m.getDiasLaborables().split(",");
        boolean trabajaEseDia = false;
        for (String d : diasPermitidos) {
            if (d.trim().equals(String.valueOf(diaSemana))) {
                trabajaEseDia = true;
                break;
            }
        }
        if (!trabajaEseDia) return false;
        if (hora.isBefore(m.getHoraInicio()) || !hora.isBefore(m.getHoraFin())) return false;
        boolean ocupado = citaRepository.existsByIdMedicoAndFechaCitaAndHoraCitaAndEstadoNot(idMedico, fecha, hora, "Cancelada");
        
        return !ocupado;
    }

    @PostMapping("/reservar")
    public String reservarCita(Cita cita, Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            if (!esHorarioValido(cita.getIdMedico(), cita.getFechaCita(), cita.getHoraCita())) {
                return "redirect:/paciente/dashboard?errorHorario=true";
            }

            String username = authentication.getName();
            Optional<Usuario> usuarioOpt = usuarioRepository.findByUsername(username);
            
            if (usuarioOpt.isPresent()) {
                Optional<Paciente> pacienteOpt = pacienteRepository.findByIdUsuario(usuarioOpt.get().getIdUsuario());
                if (pacienteOpt.isPresent()) {
                    Paciente p = pacienteOpt.get();
                    cita.setIdPaciente(p.getIdPaciente());
                    cita.setEstado("Pendiente");
                    citaRepository.save(cita);

                    // Create Pago for the appointment
                    double tarifaBase = 100.0;
                    Optional<Medico> mOpt = medicoRepository.findById(cita.getIdMedico());
                    if (mOpt.isPresent()) {
                        Optional<Especialidad> eOpt = especialidadRepository.findById(mOpt.get().getIdEspecialidad());
                        if (eOpt.isPresent()) {
                            tarifaBase = obtenerTarifaBase(eOpt.get().getNombreEspecialidad());
                        }
                    }
                    double desc = 0.0;
                    if (p.getTipoSeguro() != null && !p.getTipoSeguro().isEmpty()) {
                        desc = descuentoService.obtenerPorcentajeDescuento(p.getTipoSeguro().toUpperCase());
                    }
                    double total = tarifaBase - (tarifaBase * desc);

                    Pago pago = new Pago();
                    pago.setCita(cita);
                    pago.setMonto(BigDecimal.valueOf(total));
                    pago.setMetodoPago("Tarjeta (Reserva web)");
                    pago.setEstado("Pagado");
                    pagoRepository.save(pago);

                    return "redirect:/paciente/dashboard?reservaExito=true";
                }
            }
        }
        return "redirect:/?error=true";
    }

    @PostMapping("/cancelar/{id}")
    public String cancelarCita(@org.springframework.web.bind.annotation.PathVariable("id") Integer idCita, Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            Optional<Cita> citaOpt = citaRepository.findById(idCita);
            if (citaOpt.isPresent()) {
                Cita cita = citaOpt.get();
                cita.setEstado("Cancelada");
                cita.setNotificacionMedico(true);
                citaRepository.save(cita);
                return "redirect:/paciente/dashboard?cancelada=true";
            }
        }
        return "redirect:/paciente/dashboard?error=true";
    }

    @PostMapping("/modificar/{id}")
    public String modificarCita(@org.springframework.web.bind.annotation.PathVariable("id") Integer idCita, 
                                @org.springframework.web.bind.annotation.RequestParam("nuevaFecha") java.time.LocalDate nuevaFecha, 
                                @org.springframework.web.bind.annotation.RequestParam("nuevaHora") java.time.LocalTime nuevaHora,
                                Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            Optional<Cita> citaOpt = citaRepository.findById(idCita);
            if (citaOpt.isPresent()) {
                Cita cita = citaOpt.get();
                if (!cita.getFechaCita().equals(nuevaFecha) || !cita.getHoraCita().equals(nuevaHora)) {
                    if (!esHorarioValido(cita.getIdMedico(), nuevaFecha, nuevaHora)) {
                        return "redirect:/paciente/dashboard?errorHorario=true";
                    }
                }

                cita.setFechaCita(nuevaFecha);
                cita.setHoraCita(nuevaHora);
                cita.setEstado("Pendiente");
                cita.setNotificacionMedico(true);
                citaRepository.save(cita);
                return "redirect:/paciente/dashboard?modificada=true";
            }
        }
        return "redirect:/paciente/dashboard?error=true";
    }

    @PostMapping("/estado/{id}")
    public String cambiarEstadoCita(@org.springframework.web.bind.annotation.PathVariable("id") Integer idCita, 
                                    @org.springframework.web.bind.annotation.RequestParam("nuevoEstado") String nuevoEstado,
                                    @org.springframework.web.bind.annotation.RequestParam(value = "detalleConsulta", required = false) String detalleConsulta,
                                    Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            Optional<Cita> citaOpt = citaRepository.findById(idCita);
            if (citaOpt.isPresent()) {
                Cita cita = citaOpt.get();
                cita.setEstado(nuevoEstado);
                if ("Atendida".equals(nuevoEstado) && detalleConsulta != null) {
                    cita.setDetalleConsulta(detalleConsulta);
                }
                citaRepository.save(cita);
                
                if ("Atendida".equals(nuevoEstado)) {
                    return "redirect:/medico/dashboard?atendida=true";
                } else if ("Ausente".equals(nuevoEstado)) {
                    return "redirect:/medico/dashboard?ausente=true";
                } else if ("Cancelada_Medico".equals(nuevoEstado)) {
                    // Send email if it's Cancelada_Medico (doctor urgency)
                    Optional<Paciente> pacOpt = pacienteRepository.findById(cita.getIdPaciente());
                    if (pacOpt.isPresent()) {
                        Optional<Usuario> usuOpt = usuarioRepository.findById(pacOpt.get().getIdUsuario());
                        if (usuOpt.isPresent()) {
                            emailService.enviarCorreoCancelacionCita(usuOpt.get().getEmail() != null ? usuOpt.get().getEmail() : usuOpt.get().getUsername(), pacOpt.get().getNombre(), cita.getFechaCita().toString(), cita.getHoraCita().toString());
                        }
                    }
                    return "redirect:/medico/dashboard?canceladaUrgencia=true";
                }
                return "redirect:/medico/dashboard";
            }
        }
        return "redirect:/?error=true";
    }

    @PostMapping("/descartar-alerta/{id}")
    public String descartarAlerta(@org.springframework.web.bind.annotation.PathVariable("id") Integer idCita, Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            Optional<Cita> citaOpt = citaRepository.findById(idCita);
            if (citaOpt.isPresent()) {
                Cita cita = citaOpt.get();
                cita.setNotificacionMedico(false);
                citaRepository.save(cita);
                return "redirect:/medico/dashboard";
            }
        }
        return "redirect:/?error=true";
    }
}


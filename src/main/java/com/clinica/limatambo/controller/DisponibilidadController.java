package com.clinica.limatambo.controller;

import com.clinica.limatambo.model.Cita;
import com.clinica.limatambo.model.Medico;
import com.clinica.limatambo.repository.CitaRepository;
import com.clinica.limatambo.repository.MedicoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/citas")
public class DisponibilidadController {

    @Autowired
    private MedicoRepository medicoRepository;

    @Autowired
    private CitaRepository citaRepository;

    @GetMapping("/disponibles")
    public List<HorarioDTO> obtenerHorasDisponibles(
            @RequestParam("idMedico") Integer idMedico,
            @RequestParam("fecha") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        
        List<HorarioDTO> horarios = new ArrayList<>();
        
        Optional<Medico> medicoOpt = medicoRepository.findById(idMedico);
        if (!medicoOpt.isPresent()) {
            return horarios; 
        }
        
        Medico medico = medicoOpt.get();
        if (medico.getHoraInicio() == null || medico.getHoraFin() == null || medico.getDiasLaborables() == null) {
            return horarios;
        }

        int diaSemana = fecha.getDayOfWeek().getValue(); // 1=Lunes, 7=Domingo
        String diasStr = medico.getDiasLaborables();
        boolean trabajaEseDia = false;

        if (diasStr.contains(",")) {
            for (String d : diasStr.split(",")) {
                if (d.trim().equals(String.valueOf(diaSemana))) {
                    trabajaEseDia = true;
                    break;
                }
            }
        } else if (diasStr.contains("L-M-V") || diasStr.contains("L, M, V")) {
            trabajaEseDia = (diaSemana == 1 || diaSemana == 3 || diaSemana == 5);
        } else if (diasStr.contains("M-J-S")) {
            trabajaEseDia = (diaSemana == 2 || diaSemana == 4 || diaSemana == 6);
        } else if (diasStr.contains("L-M-X-J")) {
            trabajaEseDia = (diaSemana >= 1 && diaSemana <= 4);
        } else {
            trabajaEseDia = (diaSemana >= 1 && diaSemana <= 5);
        }
        
        if (!trabajaEseDia) {
            return horarios; 
        }

        List<Cita> citasDelDia = citaRepository.findByIdMedicoAndFechaCitaAndEstadoNot(idMedico, fecha, "Cancelada");
        List<LocalTime> horasOcupadas = new ArrayList<>();
        for (Cita c : citasDelDia) {
            horasOcupadas.add(c.getHoraCita());
        }

        LocalTime horaActual = medico.getHoraInicio();
        while (horaActual.isBefore(medico.getHoraFin())) {
            boolean disponible = !horasOcupadas.contains(horaActual);
            horarios.add(new HorarioDTO(horaActual.toString(), disponible));
            horaActual = horaActual.plusMinutes(30);
        }

        return horarios;
    }

    public static class HorarioDTO {
        private String hora;
        private boolean disponible;

        public HorarioDTO(String hora, boolean disponible) {
            this.hora = hora;
            this.disponible = disponible;
        }
        public String getHora() { return hora; }
        public boolean isDisponible() { return disponible; }
    }
}


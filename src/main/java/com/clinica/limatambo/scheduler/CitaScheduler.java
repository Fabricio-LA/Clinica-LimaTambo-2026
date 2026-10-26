package com.clinica.limatambo.scheduler;

import com.clinica.limatambo.model.Cita;
import com.clinica.limatambo.repository.CitaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Component
public class CitaScheduler {

    @Autowired
    private CitaRepository citaRepository;

    // Ejecutar cada minuto para propósitos de prueba/actualización rápida (en producción sería cada hora "0 0 * * * *")
    @Scheduled(cron = "0 * * * * *")
    public void cancelarCitasPasadas() {
        LocalDate hoy = LocalDate.now();
        LocalTime ahora = LocalTime.now();

        List<Cita> citasPendientesPasadas = citaRepository.findCitasPendientesPasadas(hoy, ahora);

        if (!citasPendientesPasadas.isEmpty()) {
            for (Cita cita : citasPendientesPasadas) {
                cita.setEstado("Cancelada");
            }
            citaRepository.saveAll(citasPendientesPasadas);
            System.out.println("Scheduler: Se han cancelado automáticamente " + citasPendientesPasadas.size() + " citas pasadas.");
        }
    }
}

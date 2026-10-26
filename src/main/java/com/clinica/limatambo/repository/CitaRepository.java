package com.clinica.limatambo.repository;

import com.clinica.limatambo.model.Cita;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CitaRepository extends JpaRepository<Cita, Integer> {
    java.util.List<Cita> findByIdMedicoOrderByFechaCitaAscHoraCitaAsc(Integer idMedico);
    java.util.List<Cita> findByIdPacienteOrderByFechaCitaDesc(Integer idPaciente);
    java.util.List<Cita> findByIdMedicoAndFechaCitaAndEstadoNot(Integer idMedico, java.time.LocalDate fechaCita, String estado);
    boolean existsByIdMedicoAndFechaCitaAndHoraCitaAndEstadoNot(Integer idMedico, java.time.LocalDate fechaCita, java.time.LocalTime horaCita, String estado);

    @org.springframework.data.jpa.repository.Query("SELECT c FROM Cita c WHERE c.estado = 'Pendiente' AND (c.fechaCita < :hoy OR (c.fechaCita = :hoy AND c.horaCita <= :ahora))")
    java.util.List<Cita> findCitasPendientesPasadas(@org.springframework.data.repository.query.Param("hoy") java.time.LocalDate hoy, @org.springframework.data.repository.query.Param("ahora") java.time.LocalTime ahora);
}

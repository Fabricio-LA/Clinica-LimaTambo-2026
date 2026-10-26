package com.clinica.limatambo.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "Citas")
public class Cita {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cita")
    private Integer idCita;

    @Column(name = "id_paciente")
    private Integer idPaciente;

    @Column(name = "id_medico")
    private Integer idMedico;

    @org.springframework.format.annotation.DateTimeFormat(pattern = "yyyy-MM-dd")
    @Column(name = "fecha_cita", nullable = false)
    private LocalDate fechaCita;

    @org.springframework.format.annotation.DateTimeFormat(pattern = "HH:mm")
    @Column(name = "hora_cita", nullable = false)
    private LocalTime horaCita;

    @Column(length = 20)
    private String estado = "Pendiente";

    @Column(name = "detalle_consulta", length = 1000)
    private String detalleConsulta;

    @Column(name = "notificacion_medico")
    private Boolean notificacionMedico = false;

    public Cita() {}

    public Integer getIdCita() { return idCita; }
    public void setIdCita(Integer idCita) { this.idCita = idCita; }

    public Integer getIdPaciente() { return idPaciente; }
    public void setIdPaciente(Integer idPaciente) { this.idPaciente = idPaciente; }

    public Integer getIdMedico() { return idMedico; }
    public void setIdMedico(Integer idMedico) { this.idMedico = idMedico; }

    public LocalDate getFechaCita() { return fechaCita; }
    public void setFechaCita(LocalDate fechaCita) { this.fechaCita = fechaCita; }

    public LocalTime getHoraCita() { return horaCita; }
    public void setHoraCita(LocalTime horaCita) { this.horaCita = horaCita; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getDetalleConsulta() { return detalleConsulta; }
    public void setDetalleConsulta(String detalleConsulta) { this.detalleConsulta = detalleConsulta; }

    public Boolean getNotificacionMedico() { return notificacionMedico; }
    public void setNotificacionMedico(Boolean notificacionMedico) { this.notificacionMedico = notificacionMedico; }
}

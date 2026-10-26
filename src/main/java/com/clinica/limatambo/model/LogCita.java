package com.clinica.limatambo.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "Log_Citas")
public class LogCita {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_log")
    private Integer idLog;

    @Column(name = "id_cita")
    private Integer idCita;

    @Column(length = 50)
    private String accion;

    @Column(name = "fecha_accion")
    private LocalDateTime fechaAccion = LocalDateTime.now();

    @Column(name = "usuario_responsable", length = 50)
    private String usuarioResponsable;

    public LogCita() {}
}

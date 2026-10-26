package com.clinica.limatambo.model;

import jakarta.persistence.*;

@Entity
@Table(name = "Consultas")
public class Consulta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_consulta")
    private Integer idConsulta;

    @Column(name = "id_historial")
    private Integer idHistorial;

    @Column(name = "id_cita", unique = true)
    private Integer idCita;

    @Column(columnDefinition = "TEXT")
    private String motivo_consulta;

    @Column(columnDefinition = "TEXT")
    private String sintomas;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String diagnostico;

    @Column(columnDefinition = "TEXT")
    private String tratamiento;

    public Consulta() {}
}

package com.clinica.limatambo.model;

import jakarta.persistence.*;

@Entity
@Table(name = "Recetas")
public class Receta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_receta")
    private Integer idReceta;

    @Column(name = "id_consulta")
    private Integer idConsulta;

    @Column(length = 255)
    private String medicamento;

    @Column(columnDefinition = "TEXT")
    private String indicaciones;

    public Receta() {}
}

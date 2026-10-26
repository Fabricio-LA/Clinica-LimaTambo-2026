package com.clinica.limatambo.model;

import jakarta.persistence.*;

@Entity
@Table(name = "Medicos")
public class Medico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_medico")
    private Integer idMedico;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, length = 100)
    private String apellido;

    @Column(name = "id_especialidad")
    private Integer idEspecialidad;

    @Column(name = "id_usuario")
    private Integer idUsuario;

    @Column(name = "hora_inicio")
    private java.time.LocalTime horaInicio;

    @Column(name = "hora_fin")
    private java.time.LocalTime horaFin;

    @Column(name = "dias_laborables")
    private String diasLaborables;

    @Column(name = "foto_perfil")
    private String fotoPerfil;

    public Medico() {}

    public Integer getIdMedico() { return idMedico; }
    public void setIdMedico(Integer idMedico) { this.idMedico = idMedico; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }
    public Integer getIdEspecialidad() { return idEspecialidad; }
    public void setIdEspecialidad(Integer idEspecialidad) { this.idEspecialidad = idEspecialidad; }
    public Integer getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Integer idUsuario) { this.idUsuario = idUsuario; }

    public java.time.LocalTime getHoraInicio() { return horaInicio; }
    public void setHoraInicio(java.time.LocalTime horaInicio) { this.horaInicio = horaInicio; }

    public java.time.LocalTime getHoraFin() { return horaFin; }
    public void setHoraFin(java.time.LocalTime horaFin) { this.horaFin = horaFin; }

    public String getDiasLaborables() { return diasLaborables; }
    public void setDiasLaborables(String diasLaborables) { this.diasLaborables = diasLaborables; }

    public String getFotoPerfil() { return fotoPerfil; }
    public void setFotoPerfil(String fotoPerfil) { this.fotoPerfil = fotoPerfil; }

    public String getDiasLaborablesTexto() {
        if (this.diasLaborables == null || this.diasLaborables.isEmpty()) return "Días no definidos";
        
        if (this.diasLaborables.equals("1,2,3,4,5")) return "Lun a Vie";
        if (this.diasLaborables.equals("6,7") || this.diasLaborables.equals("6")) return "Fines de Semana";
        if (this.diasLaborables.equals("1,3,5")) return "Lun, Mie, Vie";
        if (this.diasLaborables.equals("2,4")) return "Mar y Jue";
        
        return "Días variados";
    }
}


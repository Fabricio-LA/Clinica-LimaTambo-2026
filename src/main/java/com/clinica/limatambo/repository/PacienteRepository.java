package com.clinica.limatambo.repository;

import com.clinica.limatambo.model.Paciente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PacienteRepository extends JpaRepository<Paciente, Integer> {
    java.util.Optional<Paciente> findByIdUsuario(Integer idUsuario);
    boolean existsByDni(String dni);
}

package com.clinica.limatambo.repository;

import com.clinica.limatambo.model.Medico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MedicoRepository extends JpaRepository<Medico, Integer> {
    java.util.Optional<Medico> findByIdUsuario(Integer idUsuario);
}

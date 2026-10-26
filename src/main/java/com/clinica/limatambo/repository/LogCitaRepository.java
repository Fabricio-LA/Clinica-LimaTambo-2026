package com.clinica.limatambo.repository;

import com.clinica.limatambo.model.LogCita;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LogCitaRepository extends JpaRepository<LogCita, Integer> {
}

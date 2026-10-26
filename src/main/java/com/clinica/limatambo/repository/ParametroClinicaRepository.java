package com.clinica.limatambo.repository;

import com.clinica.limatambo.model.ParametroClinica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ParametroClinicaRepository extends JpaRepository<ParametroClinica, Integer> {
    Optional<ParametroClinica> findByClave(String clave);
}

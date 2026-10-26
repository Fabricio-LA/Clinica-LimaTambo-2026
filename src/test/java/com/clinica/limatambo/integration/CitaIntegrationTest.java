package com.clinica.limatambo.integration;

import com.clinica.limatambo.model.Cita;
import com.clinica.limatambo.model.Especialidad;
import com.clinica.limatambo.model.Medico;
import com.clinica.limatambo.model.Paciente;
import com.clinica.limatambo.repository.CitaRepository;
import com.clinica.limatambo.repository.EspecialidadRepository;
import com.clinica.limatambo.repository.MedicoRepository;
import com.clinica.limatambo.repository.PacienteRepository;
import com.clinica.limatambo.service.CitaService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("Pruebas de Integración - Persistencia JPA, Capa Web MVC y Seguridad")
class CitaIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CitaService citaService;

    @Autowired
    private CitaRepository citaRepository;

    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired
    private MedicoRepository medicoRepository;

    @Autowired
    private EspecialidadRepository especialidadRepository;

    @Nested
    @DisplayName("CP-INT-01: Pruebas de Integración de Endpoints Públicos y Seguridad")
    class WebSecurityIntegrationTests {

        @Test
        @DisplayName("GET / debe responder HTTP 200 OK y renderizar la vista de inicio")
        void testIndexEndpoint_RetornaOk() throws Exception {
            mockMvc.perform(get("/"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("index"));
        }

        @Test
        @DisplayName("GET /login debe responder HTTP 200 OK con formulario de inicio de sesión")
        void testLoginEndpoint_RetornaOk() throws Exception {
            mockMvc.perform(get("/login"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("login"));
        }

        @Test
        @DisplayName("Acceso no autenticado a /dashboard debe redirigir a /login (Spring Security)")
        void testRutasProtegidas_RedirigeALoginSiNoAutenticado() throws Exception {
            mockMvc.perform(get("/dashboard"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrlPattern("**/login"));
        }
    }

    @Nested
    @DisplayName("CP-INT-02: Pruebas de Integración de Persistencia Transaccional")
    class PersistenciaTransaccionalTests {

        @Test
        @DisplayName("Debe persistir integralmente Paciente, Médico y Cita Médica en la base de datos")
        void testFlujoCompleto_PersistenciaCitaMedica() {
            // 1. Guardar Especialidad
            Especialidad esp = new Especialidad();
            esp.setNombre("Cardiología");
            esp.setDescripcion("Enfermedades cardiovasculares");
            Especialidad espGuardada = especialidadRepository.save(esp);
            assertNotNull(espGuardada.getIdEspecialidad());

            // 2. Guardar Médico
            Medico medico = new Medico();
            medico.setNombre("Dr. Carlos");
            medico.setApellido("Mendoza");
            medico.setEspecialidad(espGuardada);
            medico.setCmp("CMP-45892");
            medico.setSede("Cajamarca");
            Medico medicoGuardado = medicoRepository.save(medico);
            assertNotNull(medicoGuardado.getIdMedico());

            // 3. Guardar Paciente
            Paciente paciente = new Paciente();
            paciente.setNombre("María");
            paciente.setApellido("Vásquez");
            paciente.setDni("73456789");
            paciente.setTelefono("987654321");
            paciente.setTipoSeguro("RIMAC");
            Paciente pacienteGuardado = pacienteRepository.save(paciente);
            assertNotNull(pacienteGuardado.getIdPaciente());

            // 4. Ejecutar Servicio de Registro de Cita
            LocalDate fecha = LocalDate.now().plusDays(3);
            LocalTime hora = LocalTime.of(15, 30);
            Cita citaCreada = citaService.registrarCita(
                    pacienteGuardado.getIdPaciente(),
                    medicoGuardado.getIdMedico(),
                    fecha,
                    hora,
                    "Revisión preventiva de hipertensión"
            );

            assertNotNull(citaCreada.getIdCita());
            assertEquals("Pendiente", citaCreada.getEstado());

            // 5. Verificar persistencia consultando directamente el repositorio JPA
            List<Cita> citasPaciente = citaRepository.findByIdPacienteOrderByFechaCitaDesc(pacienteGuardado.getIdPaciente());
            assertFalse(citasPaciente.isEmpty());
            assertEquals(1, citasPaciente.size());
            assertEquals(fecha, citasPaciente.get(0).getFechaCita());
            assertEquals(hora, citasPaciente.get(0).getHoraCita());
        }

        @Test
        @DisplayName("Debe rechazar la persistencia duplicada en el mismo horario garantizando unicidad de turno")
        void testPersistencia_ImpideCruceHorario() {
            // Configurar médico y paciente
            Especialidad esp = especialidadRepository.save(new Especialidad());
            Medico medico = new Medico();
            medico.setNombre("Dr. Hugo");
            medico.setApellido("Paredes");
            medico.setEspecialidad(esp);
            Medico medicoGuardado = medicoRepository.save(medico);

            Paciente paciente1 = new Paciente();
            paciente1.setNombre("Juan");
            paciente1.setApellido("Pérez");
            paciente1.setDni("11223344");
            Paciente pac1Guardado = pacienteRepository.save(paciente1);

            Paciente paciente2 = new Paciente();
            paciente2.setNombre("Ana");
            paciente2.setApellido("Torres");
            paciente2.setDni("55667788");
            Paciente pac2Guardado = pacienteRepository.save(paciente2);

            LocalDate fecha = LocalDate.now().plusDays(2);
            LocalTime hora = LocalTime.of(10, 0);

            // Primera cita: se registra exitosamente
            Cita cita1 = citaService.registrarCita(pac1Guardado.getIdPaciente(), medicoGuardado.getIdMedico(), fecha, hora, "Consulta 1");
            assertNotNull(cita1.getIdCita());

            // Segunda cita en el MISMO horario y MISMO médico: debe ser rechazada
            assertThrows(IllegalStateException.class, () ->
                    citaService.registrarCita(pac2Guardado.getIdPaciente(), medicoGuardado.getIdMedico(), fecha, hora, "Consulta 2")
            );
        }
    }
}

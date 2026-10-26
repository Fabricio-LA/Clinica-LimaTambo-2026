package com.clinica.limatambo.service;

import com.clinica.limatambo.model.*;
import com.clinica.limatambo.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class DataSeeder implements CommandLineRunner {

    @Autowired private RolRepository rolRepository;
    @Autowired private EspecialidadRepository especialidadRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private MedicoRepository medicoRepository;
    @Autowired private PacienteRepository pacienteRepository;
    @Autowired private CitaRepository citaRepository;
    @Autowired private PagoRepository pagoRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        seedRoles();
        seedEspecialidades();
        seedUsuariosYMedicos();
        seedPacientesYCitas();
    }

    private void seedRoles() {
        if (rolRepository.count() == 0) {
            Rol r1 = new Rol(); r1.setNombreRol("ADMIN"); rolRepository.save(r1);
            Rol r2 = new Rol(); r2.setNombreRol("MEDICO"); rolRepository.save(r2);
            Rol r3 = new Rol(); r3.setNombreRol("PACIENTE"); rolRepository.save(r3);
        }
    }

    private void seedEspecialidades() {
        if (especialidadRepository.count() == 0) {
            String[] espNombres = {
                "Medicina General", "Cardiología", "Pediatría", 
                "Ginecología", "Traumatología", "Dermatología", "Oftalmología"
            };
            for (String n : espNombres) {
                Especialidad esp = new Especialidad();
                esp.setNombreEspecialidad(n);
                especialidadRepository.save(esp);
            }
        }
    }

    private void seedUsuariosYMedicos() {
        // Crear Administrador si no existe
        if (usuarioRepository.findByUsername("admin01").isEmpty()) {
            Usuario admin = new Usuario();
            admin.setUsername("admin01");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setIdRol(1);
            admin.setEmail("admin@clinicalimatambo.pe");
            admin.setEstado(true);
            usuarioRepository.save(admin);
        }

        // Crear Médicos si no existen
        if (medicoRepository.count() == 0) {
            List<Especialidad> especialidades = especialidadRepository.findAll();
            if (especialidades.isEmpty()) return;

            String[][] medicosData = {
                {"Carlos", "Mendoza", "hombre_1.jpg", "08:00", "14:00", "L-M-V"},
                {"Ana", "Suárez", "mujer_1.jpg", "09:00", "15:00", "M-J-S"},
                {"Jorge", "Linares", "hombre_2.jpg", "14:00", "20:00", "L-M-V"},
                {"Elena", "Vargas", "mujer_2.jpg", "08:00", "13:00", "L-M-X-J"},
                {"Luis", "Fernández", "hombre_3.jpg", "13:00", "19:00", "L-M-V"},
                {"Sofía", "Castro", "mujer_3.jpg", "08:00", "14:00", "M-J-S"}
            };

            for (int i = 0; i < medicosData.length; i++) {
                String username = "medico0" + (i + 1);
                Usuario userMed = new Usuario();
                userMed.setUsername(username);
                userMed.setPassword(passwordEncoder.encode("medico123"));
                userMed.setIdRol(2);
                userMed.setEmail(username + "@clinicalimatambo.pe");
                userMed.setEstado(true);
                Usuario medUserGuardado = usuarioRepository.save(userMed);

                Medico m = new Medico();
                m.setNombre(medicosData[i][0]);
                m.setApellido(medicosData[i][1]);
                m.setFotoPerfil(medicosData[i][2]);
                m.setHoraInicio(LocalTime.parse(medicosData[i][3]));
                m.setHoraFin(LocalTime.parse(medicosData[i][4]));
                m.setDiasLaborables(medicosData[i][5]);
                m.setIdUsuario(medUserGuardado.getIdUsuario());
                
                Especialidad espAsignada = especialidades.get(i % especialidades.size());
                m.setIdEspecialidad(espAsignada.getIdEspecialidad());
                medicoRepository.save(m);
            }
        }
    }

    private void seedPacientesYCitas() {
        // Crear Paciente Demo si no existe
        if (usuarioRepository.findByUsername("70000001").isEmpty()) {
            Usuario u = new Usuario();
            u.setUsername("70000001");
            u.setPassword(passwordEncoder.encode("123123"));
            u.setIdRol(3);
            u.setEmail("paciente.demo@gmail.com");
            u.setEstado(true);
            Usuario userGuardado = usuarioRepository.save(u);

            Paciente p = new Paciente();
            p.setNombre("Juan");
            p.setApellido("Quispe Flores");
            p.setDni("70000001");
            p.setFechaNacimiento(LocalDate.of(1995, 5, 12));
            p.setTelefono("987654321");
            p.setIdUsuario(userGuardado.getIdUsuario());
            p.setTipoSeguro("RIMAC");
            p.setDireccion("Av. Salaverry 1234, Lima");
            Paciente pacGuardado = pacienteRepository.save(p);

            List<Medico> medicos = medicoRepository.findAll();
            if (!medicos.isEmpty()) {
                // Cita agendada para demostración
                Cita c = new Cita();
                c.setIdPaciente(pacGuardado.getIdPaciente());
                c.setIdMedico(medicos.get(0).getIdMedico());
                c.setFechaCita(LocalDate.now().plusDays(2));
                c.setHoraCita(LocalTime.of(10, 0));
                c.setEstado("Pendiente");
                c.setDetalleConsulta("Control de rutina en Cardiología");
                Cita citaGuardada = citaRepository.save(c);

                // Pago simulado de la cita
                Pago pago = new Pago();
                pago.setCita(citaGuardada);
                pago.setMonto(BigDecimal.valueOf(120.00));
                pago.setMetodoPago("TARJETA");
                pago.setEstado("Pagado");
                pago.setFechaPago(LocalDateTime.now());
                pagoRepository.save(pago);
            }
        }
    }
}

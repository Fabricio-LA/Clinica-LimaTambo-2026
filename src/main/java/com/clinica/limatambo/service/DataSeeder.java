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
import java.util.Random;
import java.util.stream.Collectors;

@Component
public class DataSeeder implements CommandLineRunner {

    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private PacienteRepository pacienteRepository;
    @Autowired private HistorialRepository historialRepository;
    @Autowired private CitaRepository citaRepository;
    @Autowired private PagoRepository pagoRepository;
    @Autowired private MedicoRepository medicoRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // El DataSeeder ha sido puesto a dormir por petición
        if (true) return;
        
        // Ejecutamos únicamente la limpieza de paciente_gen/paciente_demo y el sembrado de los 15 pacientes peruanos
        seedPacientesCitasYPagos();
        
        // Escribimos dinámicamente el archivo de credenciales leyendo la base de datos real
        writeCredentialsFile();
    }

    private void seedPacientesCitasYPagos() {
        // Nombres que generamos anteriormente para limpiarlos
        List<String> usernamesAnteriores = java.util.Arrays.asList(
            "jquispe", "mquispe", "amamani", "lcondori", "jflores", "lrojas", "mhuaman", "fchavez", 
            "ldiaz", "vvargas", "agutierrez", "ecruz", "hperez", "pcastillo", "jmendoza", "olujan", 
            "sflores", "jrojas", "chuaman", "rdiaz", "jgutierrez", "mcruz", "eperez", "dcastillo", 
            "smendoza", "salfaro", "rlinares", "tsoto", "cvargas", "gramirez", "dpoma", "jmedina", 
            "ryana", "esanchez", "egomez", "sguerrero", "wtorres", "lsilva", "amorales", "icastro"
        );

        // 1. LIMPIEZA AUTOMÁTICA SEGURA DE ANTIGUOS USUARIOS
        List<Usuario> usuariosAntiguos = usuarioRepository.findAll().stream()
                .filter(u -> u.getUsername().startsWith("paciente_gen_") 
                          || u.getUsername().startsWith("paciente_demo") 
                          || usernamesAnteriores.contains(u.getUsername())
                          || u.getUsername().startsWith("700000"))
                .collect(Collectors.toList());

        if (!usuariosAntiguos.isEmpty()) {
            for (Usuario u : usuariosAntiguos) {
                java.util.Optional<com.clinica.limatambo.model.Paciente> pacOpt = pacienteRepository.findAll().stream()
                        .filter(p -> u.getIdUsuario().equals(p.getIdUsuario()))
                        .findFirst();
                
                if (pacOpt.isPresent()) {
                    com.clinica.limatambo.model.Paciente pac = pacOpt.get();
                    
                    historialRepository.findAll().stream()
                            .filter(h -> pac.getIdPaciente().equals(h.getIdPaciente()))
                            .forEach(h -> historialRepository.delete(h));
                    
                    List<Cita> citasPaciente = citaRepository.findAll().stream()
                            .filter(c -> pac.getIdPaciente().equals(c.getIdPaciente()))
                            .collect(Collectors.toList());
                    
                    for (Cita c : citasPaciente) {
                        pagoRepository.findAll().stream()
                                .filter(pg -> pg.getCita() != null && c.getIdCita().equals(pg.getCita().getIdCita()))
                                .forEach(pg -> pagoRepository.delete(pg));
                        
                        citaRepository.delete(c);
                    }
                    
                    pacienteRepository.delete(pac);
                }
                usuarioRepository.delete(u);
            }
        }

        // 2. EVITAR DUPLICACIÓN DE LOS PACIENTES AUTOGENERADOS
        if (usuarioRepository.findByUsername("70000000").isPresent()) {
            return; 
        }

        List<Medico> medicos = medicoRepository.findAll();
        if (medicos.isEmpty()) return;

        // Nombres y apellidos peruanos realistas para los 15 pacientes
        String[] primerNombreM = {"Juan", "Carlos", "Luis", "Jorge", "José", "Miguel", "Fabricio", "Pedro", "Víctor", "Andrés", "Marcos", "Hugo", "Diego", "Javier", "Oscar"};
        String[] segundoNombreM = {"Marcelo", "Alberto", "Antonio", "Enrique", "Manuel", "Daniel", "David", "Francisco", "Eduardo", "Ángel", "Ramón", "Arturo", "Fernando", "Alexander", "Jesús"};
        
        String[] primerNombreF = {"María", "Ana", "Laura", "Sofía", "Lucía", "Carmen", "Rosa", "Luz", "Marta", "Julia", "Elena", "Gabriela", "Patricia", "Sandra", "Diana"};
        String[] segundoNombreF = {"Elena", "Isabel", "Beatriz", "Cristina", "Milagros", "Cecilia", "Patricia", "Victoria", "Teresa", "Inés", "Pilar", "Mercedes", "Esther", "Elizabeth", "Carolina"};

        String[] apellidosPaternos = {"Quispe", "Mamani", "Condori", "Flores", "Rojas", "Huamán", "Chávez", "Díaz", "Vargas", "Gutiérrez", "Cruz", "Pérez", "Castillo", "Mendoza", "Luján"};
        String[] apellidosMaternos = {"Sánchez", "Gómez", "Guerrero", "Ortiz", "Torres", "Silva", "Morales", "Castro", "Salazar", "Herrera", "Medina", "Poma", "Vásquez", "Ramos", "Espinoza"};

        String[] seguros = {"PARTICULAR", "RIMAC", "PACIFICO", "MAPFRE", "SIS", "ESSALUD"};
        String[] distritos = {"Miraflores", "Jesús María", "San Isidro", "Lince", "Magdalena del Mar", "Surco", "San Borja", "Pueblo Libre", "La Molina", "San Miguel"};
        
        Random r = new Random();
        String pass = passwordEncoder.encode("123123");

        for (int i = 0; i < 30; i++) {
            boolean esVaron = (i % 2 == 0);
            String pNombre = esVaron ? primerNombreM[i % primerNombreM.length] : primerNombreF[i % primerNombreF.length];
            String sNombre = esVaron ? segundoNombreM[i % segundoNombreM.length] : segundoNombreF[i % segundoNombreF.length];
            String apPaterno = apellidosPaternos[i % apellidosPaternos.length];
            String apMaterno = apellidosMaternos[i % apellidosMaternos.length];

            String nombreCompleto = pNombre + " " + sNombre;
            String apellidoCompleto = apPaterno + " " + apMaterno;

            // Generar DNI determinista que servirá como Usuario
            String dni = String.format("%08d", 70000000 + i);
            String username = dni;

            Usuario u = new Usuario();
            u.setUsername(username);
            u.setPassword(pass);
            u.setEmail("paciente" + dni + "@limatambo.com");
            u.setIdRol(3); // Paciente
            u.setEstado(true);
            usuarioRepository.save(u);

            Paciente p = new Paciente();
            p.setNombre(nombreCompleto);
            p.setApellido(apellidoCompleto);
            p.setDni(dni);
            p.setIdUsuario(u.getIdUsuario());
            p.setTipoSeguro(seguros[r.nextInt(seguros.length)]);
            p.setDireccion("Av. Las Lomas " + (100 + i * 50) + ", " + distritos[r.nextInt(distritos.length)]);
            p.setFechaNacimiento(LocalDate.now().minusYears(18 + r.nextInt(50)));
            p.setTelefono("9" + String.format("%08d", 10000000 + i)); // Teléfono único
            pacienteRepository.save(p);

            Historial h = new Historial();
            h.setIdPaciente(p.getIdPaciente());
            historialRepository.save(h);

            // 1. Citas pasadas generales (4 a 7 citas en los últimos 90 días)
            int numCitasPasadas = 4 + r.nextInt(4);
            for (int j = 0; j < numCitasPasadas; j++) {
                Cita c = new Cita();
                c.setIdPaciente(p.getIdPaciente());
                c.setIdMedico(medicos.get(r.nextInt(medicos.size())).getIdMedico());
                c.setFechaCita(LocalDate.now().minusDays(r.nextInt(90) + 1)); // Últimos 3 meses
                c.setHoraCita(LocalTime.of(8 + r.nextInt(8), 0));
                
                boolean esAtendida = r.nextInt(10) < 9;
                if (esAtendida) {
                    c.setEstado("Atendida");
                    c.setDetalleConsulta("Paciente asistió a consulta de control. Se recetó tratamiento y se indicaron recomendaciones.");
                } else {
                    c.setEstado("Cancelada");
                }
                citaRepository.save(c);

                if (esAtendida) {
                    Pago pago = new Pago();
                    pago.setCita(c);
                    pago.setMonto(BigDecimal.valueOf(100.0 - r.nextInt(30)));
                    pago.setMetodoPago(r.nextBoolean() ? (r.nextBoolean() ? "Tarjeta" : "Yape") : "Efectivo");
                    pago.setEstado("Pagado");
                    pago.setFechaPago(c.getFechaCita().atTime(c.getHoraCita()));
                    pagoRepository.save(pago);
                }
            }

            // 1b. Citas pasadas específicas para rellenar la gráfica de los últimos 7 días (5 a 10 citas por paciente)
            int numSemanaPasada = 5 + r.nextInt(6); // 5 a 10 citas en la última semana
            for (int j = 0; j < numSemanaPasada; j++) {
                Cita c = new Cita();
                c.setIdPaciente(p.getIdPaciente());
                c.setIdMedico(medicos.get(r.nextInt(medicos.size())).getIdMedico());
                c.setFechaCita(LocalDate.now().minusDays(r.nextInt(7) + 1)); // Últimos 7 días (sin incluir hoy)
                c.setHoraCita(LocalTime.of(8 + r.nextInt(8), 0));
                
                boolean esAtendida = r.nextInt(10) < 9;
                if (esAtendida) {
                    c.setEstado("Atendida");
                    c.setDetalleConsulta("Consulta de seguimiento semanal completada.");
                } else {
                    c.setEstado("Cancelada");
                }
                citaRepository.save(c);

                if (esAtendida) {
                    Pago pago = new Pago();
                    pago.setCita(c);
                    pago.setMonto(BigDecimal.valueOf(100.0 - r.nextInt(30)));
                    pago.setMetodoPago(r.nextBoolean() ? (r.nextBoolean() ? "Tarjeta" : "Yape") : "Efectivo");
                    pago.setEstado("Pagado");
                    pago.setFechaPago(c.getFechaCita().atTime(c.getHoraCita()));
                    pagoRepository.save(pago);
                }
            }

            // 2. Citas para hoy (3 a 5)
            int numCitasHoy = 3 + r.nextInt(3); // 3, 4, 5
            for (int j = 0; j < numCitasHoy; j++) {
                Cita c = new Cita();
                c.setIdPaciente(p.getIdPaciente());
                c.setIdMedico(medicos.get(r.nextInt(medicos.size())).getIdMedico());
                c.setFechaCita(LocalDate.now());
                c.setHoraCita(LocalTime.of(8 + r.nextInt(8), 0));
                
                int hoyEstado = r.nextInt(3);
                if (hoyEstado == 0) {
                    c.setEstado("Pendiente");
                } else if (hoyEstado == 1) {
                    c.setEstado("Atendida");
                    c.setDetalleConsulta("Consulta médica de hoy completada con éxito.");
                } else {
                    c.setEstado("Cancelada");
                }
                citaRepository.save(c);

                if (c.getEstado().equals("Atendida")) {
                    Pago pago = new Pago();
                    pago.setCita(c);
                    pago.setMonto(BigDecimal.valueOf(100.0 - r.nextInt(30)));
                    pago.setMetodoPago(r.nextBoolean() ? (r.nextBoolean() ? "Tarjeta" : "Yape") : "Efectivo");
                    pago.setEstado("Pagado");
                    pago.setFechaPago(c.getFechaCita().atTime(c.getHoraCita()));
                    pagoRepository.save(pago);
                }
            }

            // 3. Citas futuras (4 a 6)
            int numCitasFuturas = 4 + r.nextInt(3); // 4, 5, 6
            for (int j = 0; j < numCitasFuturas; j++) {
                Cita c = new Cita();
                c.setIdPaciente(p.getIdPaciente());
                c.setIdMedico(medicos.get(r.nextInt(medicos.size())).getIdMedico());
                c.setFechaCita(LocalDate.now().plusDays(r.nextInt(30) + 1)); // Próximo mes
                c.setHoraCita(LocalTime.of(8 + r.nextInt(8), 0));
                c.setEstado(r.nextBoolean() ? "Confirmada" : "Pendiente");
                citaRepository.save(c);
        }
    }

    private void writeCredentialsFile() {
        try {
            StringBuilder content = new StringBuilder();
            content.append("========================================================================\n");
            content.append("             CLÍNICA LIMATAMBO - CREDENCIALES DEL SISTEMA\n");
            content.append("========================================================================\n\n");
            
            content.append("------------------------------------------------------------------------\n");
            content.append("1. ADMINISTRADORES Y RECEPCIONISTAS\n");
            content.append("------------------------------------------------------------------------\n");
            List<Usuario> admins = usuarioRepository.findAll().stream().filter(u -> u.getIdRol() == 1).collect(Collectors.toList());
            for (Usuario u : admins) {
                content.append(String.format("* [ADMIN] - Usuario: %s - Email: %s (Contraseña: admin123 si es el de defecto)\n", u.getUsername(), u.getEmail()));
            }
            List<Usuario> recepcionistas = usuarioRepository.findAll().stream().filter(u -> u.getIdRol() == 4).collect(Collectors.toList());
            for (Usuario u : recepcionistas) {
                content.append(String.format("* [RECEPCIONISTA] - Usuario: %s - Email: %s (Contraseña: recepcion123 si es el de defecto)\n", u.getUsername(), u.getEmail()));
            }
            content.append("\n");
            
            content.append("------------------------------------------------------------------------\n");
            content.append("2. MÉDICOS REGISTRADOS EN LA BASE DE DATOS (Contraseña: medico123 si es de defecto)\n");
            content.append("------------------------------------------------------------------------\n");
            List<Medico> medicos = medicoRepository.findAll();
            for (Medico m : medicos) {
                String username = "[Sin Usuario]";
                String email = "[Sin Email]";
                if (m.getIdUsuario() != null) {
                    Usuario u = usuarioRepository.findById(m.getIdUsuario()).orElse(null);
                    if (u != null) {
                        username = u.getUsername();
                        email = u.getEmail();
                    }
                }
                content.append(String.format("* Dr(a). %s %s - Usuario: %s - Email: %s\n", m.getNombre(), m.getApellido(), username, email));
            }
            content.append("\n");
            
            content.append("------------------------------------------------------------------------\n");
            content.append("3. PACIENTES REGISTRADOS EN LA BASE DE DATOS (Contraseña: 123123 si son autogenerados)\n");
            content.append("------------------------------------------------------------------------\n");
            List<Paciente> pacientes = pacienteRepository.findAll();
            for (Paciente p : pacientes) {
                String username = "[Sin Usuario]";
                String email = "[Sin Email]";
                if (p.getIdUsuario() != null) {
                    Usuario u = usuarioRepository.findById(p.getIdUsuario()).orElse(null);
                    if (u != null) {
                        username = u.getUsername();
                        email = u.getEmail();
                    }
                }
                content.append(String.format("* %s %s - Usuario: %s - Email: %s\n", p.getNombre(), p.getApellido(), username, email));
            }
            
            java.nio.file.Files.write(
                java.nio.file.Paths.get("c:/Users/ratab/Documents/Proyectos_WEB/limatambo - copia/credenciales_sistema.txt"),
                content.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8)
            );
        } catch (Exception e) {
            System.err.println("Error al escribir el archivo de credenciales: " + e.getMessage());
        }
    }
}

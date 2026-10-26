package com.clinica.limatambo.controller;

import com.clinica.limatambo.controller.DashboardController.CitaDTO;
import com.clinica.limatambo.model.Cita;
import com.clinica.limatambo.model.Insumo;
import com.clinica.limatambo.model.Usuario;
import com.clinica.limatambo.repository.CitaRepository;
import com.clinica.limatambo.repository.InsumoRepository;
import com.clinica.limatambo.repository.PacienteRepository;
import com.clinica.limatambo.repository.UsuarioRepository;
import com.clinica.limatambo.repository.PagoRepository;
import com.clinica.limatambo.repository.MedicoRepository;
import com.clinica.limatambo.repository.MovimientoInventarioRepository;
import com.clinica.limatambo.model.Pago;
import com.clinica.limatambo.model.Medico;
import com.clinica.limatambo.model.MovimientoInventario;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.security.core.Authentication;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private CitaRepository citaRepository;

    @Autowired
    private InsumoRepository insumoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired
    private PagoRepository pagoRepository;

    @Autowired
    private MedicoRepository medicoRepository;

    @Autowired
    private MovimientoInventarioRepository movimientoInventarioRepository;

    @Autowired
    private com.clinica.limatambo.service.EmailService emailService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        long totalCitas = citaRepository.count();
        long totalPacientes = pacienteRepository.findAll().stream()
                .filter(p -> {
                    if (p.getIdUsuario() != null) {
                        return usuarioRepository.findById(p.getIdUsuario())
                            .map(u -> Boolean.TRUE.equals(u.getEstado()))
                            .orElse(false);
                    }
                    return false;
                }).count();
        long totalInsumos = insumoRepository.count();
        
        List<Pago> pagos = pagoRepository.findAll();
        BigDecimal ingresosMes = BigDecimal.ZERO;
        long ventasMes = 0;
        
        for (Pago p : pagos) {
            if (p.getFechaPago() != null && p.getFechaPago().getYear() == LocalDate.now().getYear() && p.getFechaPago().getMonth() == LocalDate.now().getMonth()) {
                if (p.getMonto() != null && "Pagado".equals(p.getEstado())) {
                    ingresosMes = ingresosMes.add(p.getMonto());
                    ventasMes++;
                }
            }
        }

        long medicosActivos = medicoRepository.findAll().stream()
                .filter(m -> {
                    if (m.getIdUsuario() != null) {
                        return usuarioRepository.findById(m.getIdUsuario())
                            .map(u -> Boolean.TRUE.equals(u.getEstado()))
                            .orElse(false);
                    }
                    return false;
                }).count();

        model.addAttribute("totalCitas", totalCitas);
        model.addAttribute("totalPacientes", totalPacientes);
        model.addAttribute("totalInsumos", totalInsumos);
        model.addAttribute("ingresosMes", ingresosMes);
        model.addAttribute("ventasMes", ventasMes);
        model.addAttribute("medicosActivos", medicosActivos);
        
        // 1. GrÃ¡fico de Demanda de Citas (Ãšltimos 7 dÃ­as)
        List<Cita> todasLasCitas = citaRepository.findAll();
        
        long citasCanceladas = todasLasCitas.stream().filter(c -> "Cancelada".equals(c.getEstado()) || "Cancelada_Medico".equals(c.getEstado()) || "Ausente".equals(c.getEstado())).count();
        long citasAtendidas = todasLasCitas.stream().filter(c -> "Atendida".equals(c.getEstado()) || "Completada".equals(c.getEstado())).count();
        long citasPendientes = todasLasCitas.stream().filter(c -> "Confirmada".equals(c.getEstado()) || "Pendiente".equals(c.getEstado())).count();
        
        model.addAttribute("citasCanceladas", citasCanceladas);
        model.addAttribute("citasAtendidas", citasAtendidas);
        model.addAttribute("citasPendientes", citasPendientes);

        List<Insumo> todosInsumos = insumoRepository.findAll();
        java.util.Map<String, List<Insumo>> insumosPorCategoria = todosInsumos.stream().collect(java.util.stream.Collectors.groupingBy(i -> i.getCategoria() != null ? i.getCategoria() : "Sin CategorÃ­a"));
        List<InsumoStatsDTO> insumosStats = new ArrayList<>();
        for (java.util.Map.Entry<String, List<Insumo>> entry : insumosPorCategoria.entrySet()) {
            long total = entry.getValue().size();
            long bajoStock = entry.getValue().stream().filter(i -> i.getStockActual() <= i.getStockMinimo()).count();
            insumosStats.add(new InsumoStatsDTO(entry.getKey(), total, bajoStock));
        }
        model.addAttribute("insumosStats", insumosStats);

        long[] demandaCitasData = new long[7];
        String[] demandaCitasLabels = new String[7];
        LocalDate hoy = LocalDate.now();
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("E", new java.util.Locale("es", "ES"));
        for (int i = 0; i < 7; i++) {
            LocalDate dia = hoy.minusDays(6 - i);
            demandaCitasLabels[i] = "'" + dia.format(formatter) + "'";
            long count = todasLasCitas.stream()
                .filter(c -> c.getFechaCita() != null && c.getFechaCita().isEqual(dia))
                .count();
            demandaCitasData[i] = count;
        }
        
        // 2. GrÃ¡fico de Estado de Citas (Hoy)
        long completadasHoy = todasLasCitas.stream()
            .filter(c -> c.getFechaCita() != null && c.getFechaCita().isEqual(hoy) && ("Atendida".equals(c.getEstado()) || "Completada".equals(c.getEstado())))
            .count();
        long pendientesHoy = todasLasCitas.stream()
            .filter(c -> c.getFechaCita() != null && c.getFechaCita().isEqual(hoy) && ("Confirmada".equals(c.getEstado()) || "Pendiente".equals(c.getEstado())))
            .count();
        long canceladasHoy = todasLasCitas.stream()
            .filter(c -> c.getFechaCita() != null && c.getFechaCita().isEqual(hoy) && ("Cancelada".equals(c.getEstado()) || "Cancelada_Medico".equals(c.getEstado()) || "Ausente".equals(c.getEstado())))
            .count();
        
        model.addAttribute("chartLabelsCitas", java.util.Arrays.toString(demandaCitasLabels).replace("'", "\""));
        model.addAttribute("chartDataCitas", java.util.Arrays.toString(demandaCitasData));
        model.addAttribute("chartDataEstados", "[" + completadasHoy + ", " + pendientesHoy + ", " + canceladasHoy + "]");
        
        return "admin-dashboard";
    }

    @GetMapping("/reporte-mensual.csv")
    @ResponseBody
    public ResponseEntity<byte[]> descargarReporteMensual() {
        List<Pago> pagos = pagoRepository.findAll();
        StringBuilder csvContent = new StringBuilder();
        
        // CSV Header
        csvContent.append("ID Pago,Fecha,Tipo,Paciente,Monto,Estado\n");
        
        for (Pago p : pagos) {
            if (p.getFechaPago() != null && p.getFechaPago().getYear() == LocalDate.now().getYear() && p.getFechaPago().getMonth() == LocalDate.now().getMonth()) {
                String id = p.getIdPago() != null ? p.getIdPago().toString() : "";
                String fecha = p.getFechaPago().toString();
                String tipo = p.getCita() != null ? "Cita #" + p.getCita().getIdCita() : "Consulta Externa";
                String paciente = p.getCita() != null ? "PÃºblico en General" : (p.getDetalleAdicional() != null ? p.getDetalleAdicional() : "PÃºblico en General");
                if (p.getCita() != null && p.getCita().getIdPaciente() != null) {
                    java.util.Optional<com.clinica.limatambo.model.Paciente> pac = pacienteRepository.findById(p.getCita().getIdPaciente());
                    if (pac.isPresent()) {
                        paciente = pac.get().getNombre() + " " + pac.get().getApellido();
                    }
                }
                String monto = p.getMonto() != null ? p.getMonto().toString() : "0.00";
                String estado = p.getEstado() != null ? p.getEstado() : "";
                
                csvContent.append(String.format("%s,%s,%s,%s,%s,%s\n", id, fecha, tipo, paciente, monto, estado));
            }
        }
        
        // Add UTF-8 BOM so Excel reads it properly
        byte[] csvBytes = csvContent.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
        byte[] bom = new byte[] { (byte)0xEF, (byte)0xBB, (byte)0xBF };
        byte[] fullBytes = new byte[bom.length + csvBytes.length];
        System.arraycopy(bom, 0, fullBytes, 0, bom.length);
        System.arraycopy(csvBytes, 0, fullBytes, bom.length, csvBytes.length);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=reporte-mensual.csv")
                .contentType(MediaType.parseMediaType("text/csv; charset=utf-8"))
                .body(fullBytes);
    }

    @GetMapping("/cierre-caja.csv")
    @ResponseBody
    public ResponseEntity<byte[]> cierreCaja() {
        List<Pago> pagos = pagoRepository.findAll();
        StringBuilder csvContent = new StringBuilder();
        
        // CSV Header
        csvContent.append("CIERRE DE CAJA DIARIO\n");
        csvContent.append("Fecha:,").append(LocalDate.now().toString()).append("\n\n");
        csvContent.append("ID Pago,Hora,Tipo,Paciente,Monto,Metodo,Estado\n");
        
        BigDecimal totalEfectivo = BigDecimal.ZERO;
        BigDecimal totalTarjeta = BigDecimal.ZERO;
        
        for (Pago p : pagos) {
            if (p.getFechaPago() != null && p.getFechaPago().toLocalDate().isEqual(LocalDate.now()) && "Pagado".equals(p.getEstado())) {
                String id = p.getIdPago() != null ? p.getIdPago().toString() : "";
                String hora = p.getFechaPago().toLocalTime().toString();
                String tipo = p.getCita() != null ? "Cita #" + p.getCita().getIdCita() : "Consulta Externa";
                String paciente = p.getCita() != null ? "PÃºblico en General" : (p.getDetalleAdicional() != null ? p.getDetalleAdicional() : "PÃºblico en General");
                if (p.getCita() != null && p.getCita().getIdPaciente() != null) {
                    java.util.Optional<com.clinica.limatambo.model.Paciente> pac = pacienteRepository.findById(p.getCita().getIdPaciente());
                    if (pac.isPresent()) {
                        paciente = pac.get().getNombre() + " " + pac.get().getApellido();
                    }
                }
                String montoStr = p.getMonto() != null ? p.getMonto().toString() : "0.00";
                String metodo = p.getMetodoPago() != null ? p.getMetodoPago() : "";
                String estado = p.getEstado() != null ? p.getEstado() : "";
                
                csvContent.append(String.format("%s,%s,%s,%s,%s,%s,%s\n", id, hora, tipo, paciente, montoStr, metodo, estado));
                
                if ("Efectivo".equalsIgnoreCase(metodo)) {
                    totalEfectivo = totalEfectivo.add(p.getMonto());
                } else if ("Tarjeta".equalsIgnoreCase(metodo)) {
                    totalTarjeta = totalTarjeta.add(p.getMonto());
                }
            }
        }
        
        csvContent.append("\nRESUMEN:\n");
        csvContent.append("Total Efectivo:,S/ ").append(totalEfectivo).append("\n");
        csvContent.append("Total Tarjeta:,S/ ").append(totalTarjeta).append("\n");
        csvContent.append("TOTAL INGRESOS:,S/ ").append(totalEfectivo.add(totalTarjeta)).append("\n");
        
        byte[] csvBytes = csvContent.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
        byte[] bom = new byte[] { (byte)0xEF, (byte)0xBB, (byte)0xBF };
        byte[] fullBytes = new byte[bom.length + csvBytes.length];
        System.arraycopy(bom, 0, fullBytes, 0, bom.length);
        System.arraycopy(csvBytes, 0, fullBytes, bom.length, csvBytes.length);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=cierre-caja-" + LocalDate.now() + ".csv")
                .contentType(MediaType.parseMediaType("text/csv; charset=utf-8"))
                .body(fullBytes);
    }

    @GetMapping("/recibo/{id}")
    public String verRecibo(@org.springframework.web.bind.annotation.PathVariable Integer id, Model model) {
        java.util.Optional<Pago> optPago = pagoRepository.findById(id);
        if (optPago.isPresent()) {
            Pago pago = optPago.get();
            model.addAttribute("pago", pago);
            
            String nombrePaciente = "Paciente Particular";
            String descripcionPago = "Servicio de Consulta Medica";
            if (pago.getCita() != null) {
                descripcionPago = "Consulta MÃ©dica - Cita #" + pago.getCita().getIdCita();
                if (pago.getCita().getIdPaciente() != null) {
                    java.util.Optional<com.clinica.limatambo.model.Paciente> pac = pacienteRepository.findById(pago.getCita().getIdPaciente());
                    if (pac.isPresent()) {
                        nombrePaciente = pac.get().getNombre() + " " + pac.get().getApellido();
                    }
                }
            }
            model.addAttribute("nombrePaciente", nombrePaciente);
            model.addAttribute("descripcionPago", descripcionPago);
            return "recibo";
        }
        return "redirect:/admin/ventas";
    }

    @GetMapping("/citas")
    public String adminCitas(Model model) {
        List<Cita> todasLasCitas = citaRepository.findAll();
        List<AdminCitaDTO> citasDTO = new ArrayList<>();
        
        for (Cita cita : todasLasCitas) {
            String nombrePaciente = "PÃºblico en General";
            String nombreMedico = "No Asignado";
            com.clinica.limatambo.model.Paciente pacienteObj = null;
            Integer edad = null;
            if (cita.getIdPaciente() != null) {
                java.util.Optional<com.clinica.limatambo.model.Paciente> p = pacienteRepository.findById(cita.getIdPaciente());
                if (p.isPresent()) {
                    pacienteObj = p.get();
                    nombrePaciente = p.get().getNombre() + " " + p.get().getApellido();
                    if (p.get().getFechaNacimiento() != null) {
                        edad = java.time.Period.between(p.get().getFechaNacimiento(), java.time.LocalDate.now()).getYears();
                    }
                }
            }
            if (cita.getIdMedico() != null) {
                java.util.Optional<Medico> m = medicoRepository.findById(cita.getIdMedico());
                if (m.isPresent()) {
                    nombreMedico = "Dr. " + m.get().getNombre() + " " + m.get().getApellido();
                }
            }
            citasDTO.add(new AdminCitaDTO(cita, nombrePaciente, pacienteObj, edad, nombreMedico));
        }

        model.addAttribute("citas", citasDTO);
        model.addAttribute("medicos", medicoRepository.findAll());
        model.addAttribute("pacientes", pacienteRepository.findAll());
        return "admin-citas";
    }

    @PostMapping("/citas/nueva")
    public String nuevaCitaAdmin(
            @RequestParam Integer idPaciente,
            @RequestParam Integer idMedico,
            @RequestParam String fechaCita,
            @RequestParam String horaCita,
            org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        try {
            Cita nuevaCita = new Cita();
            nuevaCita.setIdPaciente(idPaciente);
            nuevaCita.setIdMedico(idMedico);
            nuevaCita.setFechaCita(java.time.LocalDate.parse(fechaCita));
            nuevaCita.setHoraCita(java.time.LocalTime.parse(horaCita));
            nuevaCita.setEstado("Pendiente");
            nuevaCita.setNotificacionMedico(true);
            citaRepository.save(nuevaCita);
            
            redirectAttributes.addFlashAttribute("success", "Nueva cita (presencial) agendada correctamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al crear la cita: " + e.getMessage());
        }
        return "redirect:/admin/citas";
    }

    @GetMapping("/inventario")
    public String adminInventario(Model model) {
        List<Insumo> insumos = insumoRepository.findAll();
        model.addAttribute("insumos", insumos);
        return "admin-inventario";
    }

    @PostMapping("/inventario/nuevo")
    public String nuevoInsumo(@RequestParam String nombreInsumo, @RequestParam String descripcion, @RequestParam String categoria,
                              @RequestParam Integer stockActual, @RequestParam Integer stockMinimo, @RequestParam BigDecimal precioUnitario,
                              @RequestParam String unidadMedida) {
        Insumo insumo = new Insumo();
        insumo.setNombreInsumo(nombreInsumo);
        insumo.setDescripcion(descripcion);
        insumo.setCategoria(categoria);
        insumo.setStockActual(stockActual);
        insumo.setStockMinimo(stockMinimo);
        insumo.setPrecioUnitario(precioUnitario);
        insumo.setUnidadMedida(unidadMedida);
        insumoRepository.save(insumo);
        return "redirect:/admin/inventario?exito=creado";
    }

    @PostMapping("/inventario/editar")
    public String editarInsumo(@RequestParam Integer idInsumo, @RequestParam String nombreInsumo, @RequestParam String descripcion, @RequestParam String categoria,
                               @RequestParam Integer stockMinimo, @RequestParam BigDecimal precioUnitario, @RequestParam String unidadMedida) {
        java.util.Optional<Insumo> optInsumo = insumoRepository.findById(idInsumo);
        if (optInsumo.isPresent()) {
            Insumo insumo = optInsumo.get();
            insumo.setNombreInsumo(nombreInsumo);
            insumo.setDescripcion(descripcion);
            insumo.setCategoria(categoria);
            insumo.setStockMinimo(stockMinimo);
            insumo.setPrecioUnitario(precioUnitario);
            insumo.setUnidadMedida(unidadMedida);
            insumoRepository.save(insumo);
        }
        return "redirect:/admin/inventario?exito=editado";
    }

    @PostMapping("/inventario/eliminar")
    public String eliminarInsumo(@RequestParam Integer idInsumo) {
        java.util.Optional<Insumo> optInsumo = insumoRepository.findById(idInsumo);
        if (optInsumo.isPresent()) {
            insumoRepository.deleteById(idInsumo);
            return "redirect:/admin/inventario?exito=eliminado";
        }
        return "redirect:/admin/inventario?error=no_encontrado";
    }

    @PostMapping("/inventario/entrada")
    public String registrarEntrada(@RequestParam Integer idInsumo, @RequestParam Integer cantidad, @RequestParam String motivo, Authentication authentication) {
        java.util.Optional<Insumo> optInsumo = insumoRepository.findById(idInsumo);
        if (optInsumo.isPresent() && cantidad > 0) {
            Insumo insumo = optInsumo.get();
            insumo.setStockActual(insumo.getStockActual() + cantidad);
            insumoRepository.save(insumo);
            
            MovimientoInventario mov = new MovimientoInventario();
            mov.setInsumo(insumo);
            mov.setTipoMovimiento("Entrada");
            mov.setCantidad(cantidad);
            mov.setMotivo(motivo);
            if (authentication != null) {
                java.util.Optional<Usuario> userOpt = usuarioRepository.findByUsername(authentication.getName());
                userOpt.ifPresent(mov::setUsuarioResponsable);
            }
            movimientoInventarioRepository.save(mov);
        }
        return "redirect:/admin/inventario?exito=entrada";
    }

    @PostMapping("/inventario/salida")
    public String registrarSalida(@RequestParam Integer idInsumo, @RequestParam Integer cantidad, @RequestParam String motivo, Authentication authentication) {
        java.util.Optional<Insumo> optInsumo = insumoRepository.findById(idInsumo);
        if (optInsumo.isPresent() && cantidad > 0) {
            Insumo insumo = optInsumo.get();
            if (insumo.getStockActual() >= cantidad) {
                insumo.setStockActual(insumo.getStockActual() - cantidad);
                insumoRepository.save(insumo);
                
                MovimientoInventario mov = new MovimientoInventario();
                mov.setInsumo(insumo);
                mov.setTipoMovimiento("Salida");
                mov.setCantidad(cantidad);
                mov.setMotivo(motivo);
                if (authentication != null) {
                    java.util.Optional<Usuario> userOpt = usuarioRepository.findByUsername(authentication.getName());
                    userOpt.ifPresent(mov::setUsuarioResponsable);
                }
                movimientoInventarioRepository.save(mov);
            } else {
                return "redirect:/admin/inventario?error=stock_insuficiente";
            }
        }
        return "redirect:/admin/inventario?exito=salida";
    }

    @GetMapping("/usuarios")
    public String adminUsuarios(Model model) {
        List<Usuario> usuarios = usuarioRepository.findAll();
        model.addAttribute("usuarios", usuarios);
        return "admin-usuarios";
    }

    @GetMapping("/ventas")
    public String adminVentas(
            @org.springframework.web.bind.annotation.RequestParam(required = false) Integer mes,
            @org.springframework.web.bind.annotation.RequestParam(required = false) Integer anio,
            Model model) {
        
        LocalDate hoy = LocalDate.now();
        
        // Si mes == 0, significa "Todos los meses"
        boolean mostrarTodos = (mes != null && mes == 0);
        
        int mesSeleccionado = (mes != null) ? mes : hoy.getMonthValue();
        int anioSeleccionado = (anio != null) ? anio : hoy.getYear();
        
        List<Pago> pagos = pagoRepository.findAll();
        BigDecimal ingresosMes = BigDecimal.ZERO;
        long ventasMes = 0;
        
        List<PagoInfoDTO> pagosInfo = new ArrayList<>();
        
        for (Pago p : pagos) {
            if (p.getFechaPago() != null && (mostrarTodos || (p.getFechaPago().getMonthValue() == mesSeleccionado && p.getFechaPago().getYear() == anioSeleccionado))) {
                String nombrePaciente = p.getCita() != null ? "PÃºblico en General" : (p.getDetalleAdicional() != null ? p.getDetalleAdicional() : "PÃºblico en General");
                if (p.getCita() != null && p.getCita().getIdPaciente() != null) {
                    java.util.Optional<com.clinica.limatambo.model.Paciente> pac = pacienteRepository.findById(p.getCita().getIdPaciente());
                    if (pac.isPresent()) {
                        nombrePaciente = pac.get().getNombre() + " " + pac.get().getApellido();
                    }
                }
                
                if (p.getMonto() != null && "Pagado".equals(p.getEstado())) {
                    ingresosMes = ingresosMes.add(p.getMonto());
                    ventasMes++;
                }
                pagosInfo.add(new PagoInfoDTO(p, nombrePaciente));
            }
        }

        // Generar lista de los Ãºltimos 12 meses para el filtro
        List<MesFiltroDTO> mesesFiltro = new ArrayList<>();
        LocalDate fechaAux = LocalDate.now();
        java.time.format.DateTimeFormatter fmt = java.time.format.DateTimeFormatter.ofPattern("MMMM yyyy", new java.util.Locale("es", "ES"));
        for (int i = 0; i < 12; i++) {
            String label = fechaAux.format(fmt);
            label = label.substring(0, 1).toUpperCase() + label.substring(1);
            mesesFiltro.add(new MesFiltroDTO(fechaAux.getMonthValue(), fechaAux.getYear(), label));
            fechaAux = fechaAux.minusMonths(1);
        }

        model.addAttribute("pagosInfo", pagosInfo);
        model.addAttribute("ingresosMes", ingresosMes);
        model.addAttribute("ventasMes", ventasMes);
        model.addAttribute("mesesFiltro", mesesFiltro);
        model.addAttribute("mesSeleccionado", mostrarTodos ? 0 : mesSeleccionado);
        model.addAttribute("anioSeleccionado", mostrarTodos ? 0 : anioSeleccionado);
        return "admin-ventas";
    }

    public static class MesFiltroDTO {
        public int mes;
        public int anio;
        public String nombreMes;
        public MesFiltroDTO(int mes, int anio, String nombreMes) {
            this.mes = mes;
            this.anio = anio;
            this.nombreMes = nombreMes;
        }
        public int getMes() { return mes; }
        public int getAnio() { return anio; }
        public String getNombreMes() { return nombreMes; }
    }

    public static class PagoInfoDTO {
        public Pago pago;
        public String nombrePaciente;
        public PagoInfoDTO(Pago pago, String nombrePaciente) {
            this.pago = pago;
            this.nombrePaciente = nombrePaciente;
        }
    }
    
    public static class AdminCitaDTO {
        public Cita cita;
        public String nombrePaciente;
        public com.clinica.limatambo.model.Paciente paciente;
        public Integer edadPaciente;
        public String nombreMedico;
        public boolean puedeConfirmar;
        
        public AdminCitaDTO(Cita cita, String nombrePaciente, com.clinica.limatambo.model.Paciente paciente, Integer edadPaciente, String nombreMedico) {
            this.cita = cita;
            this.nombrePaciente = nombrePaciente;
            this.paciente = paciente;
            this.edadPaciente = edadPaciente;
            this.nombreMedico = nombreMedico;
            
            this.puedeConfirmar = false;
            if ("Pendiente".equals(cita.getEstado()) && cita.getFechaCita() != null && cita.getHoraCita() != null) {
                java.time.LocalDateTime fechaHoraCita = java.time.LocalDateTime.of(cita.getFechaCita(), cita.getHoraCita());
                java.time.LocalDateTime limiteConfirmacion = java.time.LocalDateTime.now().plusHours(24);
                // Habilitar si la cita es dentro de las prÃ³ximas 24 horas (o ya pasÃ³ pero sigue pendiente)
                if (!fechaHoraCita.isAfter(limiteConfirmacion)) {
                    this.puedeConfirmar = true;
                }
            }
        }
    }

    public static class InsumoStatsDTO {
        public String categoria;
        public long totalItems;
        public long itemsStockBajo;
        public InsumoStatsDTO(String categoria, long totalItems, long itemsStockBajo) {
            this.categoria = categoria;
            this.totalItems = totalItems;
            this.itemsStockBajo = itemsStockBajo;
        }
    }

    @org.springframework.beans.factory.annotation.Autowired
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @org.springframework.web.bind.annotation.PostMapping("/usuarios/crear")
    public String crearUsuario(
            @org.springframework.web.bind.annotation.RequestParam String username,
            @org.springframework.web.bind.annotation.RequestParam String password,
            @org.springframework.web.bind.annotation.RequestParam(required = false) String email,
            @org.springframework.web.bind.annotation.RequestParam Integer idRol,
            @org.springframework.web.bind.annotation.RequestParam String nombre,
            @org.springframework.web.bind.annotation.RequestParam String apellido,
            @org.springframework.web.bind.annotation.RequestParam String dni,
            org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        try {
            if (usuarioRepository.findByUsername(username).isPresent()) {
                redirectAttributes.addFlashAttribute("error", "El nombre de usuario ya existe.");
                return "redirect:/admin/usuarios";
            }
            if (email != null && !email.trim().isEmpty() && usuarioRepository.findByEmail(email).isPresent()) {
                redirectAttributes.addFlashAttribute("error", "El correo electrÃ³nico ya estÃ¡ registrado.");
                return "redirect:/admin/usuarios";
            }
            if (idRol == 3 && pacienteRepository.existsByDni(dni)) {
                redirectAttributes.addFlashAttribute("error", "El DNI ya se encuentra registrado.");
                return "redirect:/admin/usuarios";
            }
            Usuario nuevoUsuario = new Usuario();
            nuevoUsuario.setUsername(username);
            nuevoUsuario.setPassword(passwordEncoder.encode(password));
            nuevoUsuario.setEmail(email);
            nuevoUsuario.setIdRol(idRol);
            nuevoUsuario.setEstado(true);

            Usuario guardado = usuarioRepository.save(nuevoUsuario);

            if (idRol == 2) {
                com.clinica.limatambo.model.Medico medico = new com.clinica.limatambo.model.Medico();
                medico.setIdUsuario(guardado.getIdUsuario());
                medico.setNombre(nombre);
                medico.setApellido(apellido);
                medico.setIdEspecialidad(1);
                medicoRepository.save(medico);
            } else if (idRol == 3) {
                com.clinica.limatambo.model.Paciente paciente = new com.clinica.limatambo.model.Paciente();
                paciente.setIdUsuario(guardado.getIdUsuario());
                paciente.setNombre(nombre);
                paciente.setApellido(apellido);
                paciente.setDni(dni);
                pacienteRepository.save(paciente);
            }
            redirectAttributes.addFlashAttribute("success", "Usuario creado con Ã©xito.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al crear usuario: " + e.getMessage());
        }
        return "redirect:/admin/usuarios";
    }

    @org.springframework.web.bind.annotation.PostMapping("/usuarios/editar")
    public String editarUsuario(
            @org.springframework.web.bind.annotation.RequestParam Integer idUsuario,
            @org.springframework.web.bind.annotation.RequestParam String username,
            @org.springframework.web.bind.annotation.RequestParam(required = false) String email,
            @org.springframework.web.bind.annotation.RequestParam(required = false) String password,
            org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        try {
            java.util.Optional<Usuario> opt = usuarioRepository.findById(idUsuario);
            if (opt.isPresent()) {
                Usuario u = opt.get();
                
                // Validar que el username no exista en otro usuario
                java.util.Optional<Usuario> checkUser = usuarioRepository.findByUsername(username);
                if (checkUser.isPresent() && !checkUser.get().getIdUsuario().equals(idUsuario)) {
                    redirectAttributes.addFlashAttribute("error", "El nombre de usuario ya estÃ¡ en uso por otra cuenta.");
                    return "redirect:/admin/usuarios";
                }
                
                // Validar que el email no exista en otro usuario
                if (email != null && !email.trim().isEmpty()) {
                    java.util.Optional<Usuario> checkEmail = usuarioRepository.findByEmail(email);
                    if (checkEmail.isPresent() && !checkEmail.get().getIdUsuario().equals(idUsuario)) {
                        redirectAttributes.addFlashAttribute("error", "El correo electrÃ³nico ya estÃ¡ registrado por otra cuenta.");
                        return "redirect:/admin/usuarios";
                    }
                }
                
                // Si es el usuario logueado actualmente, detectamos para actualizar la sesiÃ³n de Spring Security
                org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
                boolean esMismoUsuario = auth != null && auth.getName().equals(u.getUsername());
                
                u.setUsername(username);
                u.setEmail(email);
                
                // Actualizar contraseÃ±a si se proporcionÃ³ una nueva
                if (password != null && !password.trim().isEmpty()) {
                    u.setPassword(passwordEncoder.encode(password));
                }
                
                usuarioRepository.save(u);

                if (esMismoUsuario) {
                    org.springframework.security.authentication.UsernamePasswordAuthenticationToken newAuth =
                        new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                            username,
                            auth.getCredentials(),
                            auth.getAuthorities()
                        );
                    org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(newAuth);
                }
                
                redirectAttributes.addFlashAttribute("success", "Usuario actualizado correctamente.");
            } else {
                redirectAttributes.addFlashAttribute("error", "Usuario no encontrado.");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al editar usuario: " + e.getMessage());
        }
        return "redirect:/admin/usuarios";
    }

    @org.springframework.web.bind.annotation.PostMapping("/usuarios/toggle")
    public String toggleEstadoUsuario(
            @org.springframework.web.bind.annotation.RequestParam Integer idUsuario,
            org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        try {
            java.util.Optional<Usuario> opt = usuarioRepository.findById(idUsuario);
            if (opt.isPresent()) {
                Usuario u = opt.get();
                u.setEstado(!u.getEstado());
                usuarioRepository.save(u);
                String accion = u.getEstado() ? "activado" : "suspendido";
                redirectAttributes.addFlashAttribute("success", "El usuario ha sido " + accion + ".");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al cambiar estado: " + e.getMessage());
        }
        return "redirect:/admin/usuarios";
    }

    @org.springframework.web.bind.annotation.PostMapping("/citas/cancelar")
    public String cancelarCita(
            @org.springframework.web.bind.annotation.RequestParam Integer idCita,
            org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        try {
            java.util.Optional<Cita> opt = citaRepository.findById(idCita);
            if (opt.isPresent()) {
                Cita cita = opt.get();
                cita.setEstado("Cancelada");
                citaRepository.save(cita);
                redirectAttributes.addFlashAttribute("success", "Cita #" + idCita + " ha sido cancelada.");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al cancelar cita: " + e.getMessage());
        }
        return "redirect:/admin/citas";
    }

    @org.springframework.web.bind.annotation.PostMapping("/citas/editar")
    public String editarCita(
            @org.springframework.web.bind.annotation.RequestParam Integer idCita,
            @org.springframework.web.bind.annotation.RequestParam String fechaCita,
            @org.springframework.web.bind.annotation.RequestParam String horaCita,
            org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        try {
            java.util.Optional<Cita> opt = citaRepository.findById(idCita);
            if (opt.isPresent()) {
                Cita cita = opt.get();
                cita.setFechaCita(java.time.LocalDate.parse(fechaCita));
                cita.setHoraCita(java.time.LocalTime.parse(horaCita));
                citaRepository.save(cita);
                redirectAttributes.addFlashAttribute("success", "Cita #" + idCita + " reprogramada con Ã©xito.");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al editar cita: " + e.getMessage());
        }
        return "redirect:/admin/citas";
    }

    @org.springframework.web.bind.annotation.PostMapping("/citas/confirmar")
    public String confirmarCita(@org.springframework.web.bind.annotation.RequestParam Integer idCita,
                                org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        try {
            java.util.Optional<Cita> opt = citaRepository.findById(idCita);
            if (opt.isPresent()) {
                Cita cita = opt.get();
                if ("Pendiente".equals(cita.getEstado())) {
                    cita.setEstado("Confirmada");
                    citaRepository.save(cita);
                    
                    // Enviar correo al paciente si es posible
                    if (cita.getIdPaciente() != null) {
                        java.util.Optional<com.clinica.limatambo.model.Paciente> pacOpt = pacienteRepository.findById(cita.getIdPaciente());
                        if (pacOpt.isPresent()) {
                            java.util.Optional<Usuario> usuOpt = usuarioRepository.findById(pacOpt.get().getIdUsuario());
                            if (usuOpt.isPresent()) {
                                String correo = usuOpt.get().getEmail() != null ? usuOpt.get().getEmail() : usuOpt.get().getUsername();
                                String fechaStr = cita.getFechaCita() != null ? cita.getFechaCita().toString() : "";
                                String horaStr = cita.getHoraCita() != null ? cita.getHoraCita().toString() : "";
                                emailService.enviarCorreoConfirmacionCita(correo, pacOpt.get().getNombre(), fechaStr, horaStr);
                            }
                        }
                    }
                    redirectAttributes.addFlashAttribute("success", "Cita #" + idCita + " confirmada y notificada al paciente.");
                }
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al confirmar cita: " + e.getMessage());
        }
        return "redirect:/admin/citas";
    }
}

package com.clinica.limatambo.service;

import com.clinica.limatambo.model.Cita;
import com.clinica.limatambo.model.Medico;
import com.clinica.limatambo.model.Paciente;
import com.clinica.limatambo.model.Pago;
import com.clinica.limatambo.repository.CitaRepository;
import com.clinica.limatambo.repository.MedicoRepository;
import com.clinica.limatambo.repository.PacienteRepository;
import com.clinica.limatambo.repository.PagoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalTime;

@Service
public class CitaService {

    @Autowired
    private CitaRepository citaRepository;

    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired
    private MedicoRepository medicoRepository;

    @Autowired
    private PagoRepository pagoRepository;

    @Autowired
    private DescuentoService descuentoService;

    /**
     * Valida si un DNI cumple con el estándar peruano (exactamente 8 dígitos numéricos).
     */
    public boolean validarDniPeruano(String dni) {
        if (dni == null) return false;
        return dni.trim().matches("^[0-9]{8}$");
    }

    /**
     * Valida si la fecha seleccionada para la cita es válida (no puede ser en el pasado).
     */
    public boolean validarFechaCita(LocalDate fecha) {
        if (fecha == null) return false;
        return !fecha.isBefore(LocalDate.now());
    }

    /**
     * Obtiene la tarifa base según la especialidad médica.
     */
    public double obtenerTarifaBase(String nombreEspecialidad) {
        if (nombreEspecialidad == null) return 100.0;
        String esp = nombreEspecialidad.toLowerCase().trim();
        switch (esp) {
            case "medicina general":
                return 80.0;
            case "odontología":
            case "odontologia":
                return 90.0;
            case "pediatría":
            case "pediatria":
                return 100.0;
            case "ginecología":
            case "ginecologia":
                return 120.0;
            case "traumatología":
            case "traumatologia":
                return 130.0;
            case "cardiología":
            case "cardiologia":
                return 150.0;
            case "neurología":
            case "neurologia":
                return 160.0;
            case "dermatología":
            case "dermatologia":
                return 110.0;
            case "oftalmología":
            case "oftalmologia":
                return 120.0;
            default:
                return 100.0;
        }
    }

    /**
     * Calcula la liquidación contable de una consulta (precio base, descuento de seguro, subtotal, IGV y total).
     */
    public LiquidacionConsulta liquidarConsulta(String especialidad, String seguro) {
        double tarifaBase = obtenerTarifaBase(especialidad);
        double montoDescuento = descuentoService.calcularDescuento(tarifaBase, seguro);
        double precioNeto = tarifaBase - montoDescuento;

        BigDecimal neto = BigDecimal.valueOf(precioNeto).setScale(2, RoundingMode.HALF_UP);
        BigDecimal igv = neto.multiply(BigDecimal.valueOf(0.18)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = neto.setScale(2, RoundingMode.HALF_UP);
        BigDecimal subtotal = total.subtract(igv).setScale(2, RoundingMode.HALF_UP);

        return new LiquidacionConsulta(
                BigDecimal.valueOf(tarifaBase).setScale(2, RoundingMode.HALF_UP),
                BigDecimal.valueOf(montoDescuento).setScale(2, RoundingMode.HALF_UP),
                subtotal,
                igv,
                total
        );
    }

    /**
     * Verifica si un médico está disponible en la fecha y hora indicadas.
     * Evita cruces de citas y sobreventa de turnos.
     */
    public boolean verificarDisponibilidadMedico(Integer idMedico, LocalDate fecha, LocalTime hora) {
        if (idMedico == null || fecha == null || hora == null) return false;
        
        return !citaRepository.existsByIdMedicoAndFechaCitaAndHoraCitaAndEstadoNot(
                idMedico, fecha, hora, "Cancelada"
        );
    }

    /**
     * Reserva una nueva cita médica tras validar reglas de negocio.
     */
    @Transactional
    public Cita registrarCita(Integer idPaciente, Integer idMedico, LocalDate fecha, LocalTime hora, String detalle) {
        if (!validarFechaCita(fecha)) {
            throw new IllegalArgumentException("La fecha de la cita no puede ser en el pasado");
        }

        Paciente paciente = pacienteRepository.findById(idPaciente)
                .orElseThrow(() -> new IllegalArgumentException("Paciente no encontrado con ID: " + idPaciente));

        Medico medico = medicoRepository.findById(idMedico)
                .orElseThrow(() -> new IllegalArgumentException("Médico no encontrado con ID: " + idMedico));

        if (!verificarDisponibilidadMedico(idMedico, fecha, hora)) {
            throw new IllegalStateException("El médico ya cuenta con una cita programada en la fecha y hora seleccionada");
        }

        Cita cita = new Cita();
        cita.setIdPaciente(paciente.getIdPaciente());
        cita.setIdMedico(medico.getIdMedico());
        cita.setFechaCita(fecha);
        cita.setHoraCita(hora);
        cita.setDetalleConsulta(detalle != null ? detalle : "Consulta médica");
        cita.setEstado("Pendiente");

        return citaRepository.save(cita);
    }

    /**
     * Simula el pago y emisión de comprobante para una cita médica.
     */
    @Transactional
    public Pago procesarPagoSimulado(Integer idCita, String metodoPago, BigDecimal monto) {
        if (monto == null || monto.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El monto a pagar debe ser mayor a cero");
        }

        Cita cita = citaRepository.findById(idCita)
                .orElseThrow(() -> new IllegalArgumentException("Cita no encontrada con ID: " + idCita));

        Pago pago = new Pago();
        pago.setCita(cita);
        pago.setMonto(monto);
        pago.setMetodoPago(metodoPago != null ? metodoPago.toUpperCase() : "EFECTIVO");
        pago.setEstado("Pagado");
        pago.setFechaPago(java.time.LocalDateTime.now());

        Pago guardado = pagoRepository.save(pago);

        // Actualizar estado de la cita a Confirmada
        cita.setEstado("Confirmada");
        citaRepository.save(cita);

        return guardado;
    }

    /**
     * Objeto de transferencia representativo del desglose de liquidación.
     */
    public static class LiquidacionConsulta {
        private final BigDecimal tarifaBase;
        private final BigDecimal descuento;
        private final BigDecimal subtotal;
        private final BigDecimal igv;
        private final BigDecimal total;

        public LiquidacionConsulta(BigDecimal tarifaBase, BigDecimal descuento, BigDecimal subtotal, BigDecimal igv, BigDecimal total) {
            this.tarifaBase = tarifaBase;
            this.descuento = descuento;
            this.subtotal = subtotal;
            this.igv = igv;
            this.total = total;
        }

        public BigDecimal getTarifaBase() { return tarifaBase; }
        public BigDecimal getDescuento() { return descuento; }
        public BigDecimal getSubtotal() { return subtotal; }
        public BigDecimal getIgv() { return igv; }
        public BigDecimal getTotal() { return total; }
    }
}

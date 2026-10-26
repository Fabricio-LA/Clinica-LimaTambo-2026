package com.clinica.limatambo.unit;

import com.clinica.limatambo.model.Cita;
import com.clinica.limatambo.model.Medico;
import com.clinica.limatambo.model.Paciente;
import com.clinica.limatambo.model.Pago;
import com.clinica.limatambo.repository.CitaRepository;
import com.clinica.limatambo.repository.MedicoRepository;
import com.clinica.limatambo.repository.PacienteRepository;
import com.clinica.limatambo.repository.PagoRepository;
import com.clinica.limatambo.service.CitaService;
import com.clinica.limatambo.service.DescuentoService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas Unitarias - CitaService (Lógica de Negocio)")
class CitaServiceTest {

    @Mock
    private CitaRepository citaRepository;

    @Mock
    private PacienteRepository pacienteRepository;

    @Mock
    private MedicoRepository medicoRepository;

    @Mock
    private PagoRepository pagoRepository;

    @Mock
    private DescuentoService descuentoService;

    @InjectMocks
    private CitaService citaService;

    @Nested
    @DisplayName("CP-UNIT-01: Validaciones de Formato de DNI Peruano")
    class ValidacionesDniTests {

        @ParameterizedTest(name = "DNI válido: {0}")
        @ValueSource(strings = {"12345678", "45678901", "71234567", "00123456"})
        @DisplayName("Debe aceptar DNIs peruanos válidos de 8 dígitos numéricos")
        void testValidarDniPeruano_Validos(String dni) {
            assertTrue(citaService.validarDniPeruano(dni));
        }

        @ParameterizedTest(name = "DNI inválido: {0}")
        @ValueSource(strings = {"1234567", "123456789", "1234ABCD", "ABCDEFGH", "", "   "})
        @DisplayName("Debe rechazar DNIs con longitud diferente a 8 o caracteres no numéricos")
        void testValidarDniPeruano_Invalidos(String dni) {
            assertFalse(citaService.validarDniPeruano(dni));
        }

        @Test
        @DisplayName("Debe rechazar DNI nulo")
        void testValidarDniPeruano_Nulo() {
            assertFalse(citaService.validarDniPeruano(null));
        }
    }

    @Nested
    @DisplayName("CP-UNIT-02: Validaciones de Fechas de Cita")
    class ValidacionesFechaTests {

        @Test
        @DisplayName("Debe aceptar fechas de hoy o futuras")
        void testValidarFechaCita_HoyYFuturo() {
            assertTrue(citaService.validarFechaCita(LocalDate.now()));
            assertTrue(citaService.validarFechaCita(LocalDate.now().plusDays(5)));
        }

        @Test
        @DisplayName("Debe rechazar fechas pasadas")
        void testValidarFechaCita_Pasado() {
            assertFalse(citaService.validarFechaCita(LocalDate.now().minusDays(1)));
        }

        @Test
        @DisplayName("Debe rechazar fecha nula")
        void testValidarFechaCita_Nulo() {
            assertFalse(citaService.validarFechaCita(null));
        }
    }

    @Nested
    @DisplayName("CP-UNIT-03: Disponibilidad Horaria y Cruce de Citas")
    class DisponibilidadMedicoTests {

        @Test
        @DisplayName("Debe retornar false si el médico ya cuenta con una cita en el mismo horario")
        void testVerificarDisponibilidadMedico_Ocupado() {
            Integer idMedico = 1;
            LocalDate fecha = LocalDate.now().plusDays(2);
            LocalTime hora = LocalTime.of(10, 0);

            when(citaRepository.existsByIdMedicoAndFechaCitaAndHoraCitaAndEstadoNot(
                    idMedico, fecha, hora, "Cancelada"
            )).thenReturn(true);

            boolean disponible = citaService.verificarDisponibilidadMedico(idMedico, fecha, hora);
            assertFalse(disponible);
        }

        @Test
        @DisplayName("Debe retornar true si el médico no tiene citas programadas en ese bloque")
        void testVerificarDisponibilidadMedico_Libre() {
            Integer idMedico = 1;
            LocalDate fecha = LocalDate.now().plusDays(2);
            LocalTime hora = LocalTime.of(11, 0);

            when(citaRepository.existsByIdMedicoAndFechaCitaAndHoraCitaAndEstadoNot(
                    idMedico, fecha, hora, "Cancelada"
            )).thenReturn(false);

            boolean disponible = citaService.verificarDisponibilidadMedico(idMedico, fecha, hora);
            assertTrue(disponible);
        }
    }

    @Nested
    @DisplayName("CP-UNIT-04: Registro de Citas Médicas")
    class RegistroCitaTests {

        @Test
        @DisplayName("Debe registrar la cita correctamente en estado Pendiente cuando los datos son válidos")
        void testRegistrarCita_Exitoso() {
            Integer idPaciente = 10;
            Integer idMedico = 5;
            LocalDate fecha = LocalDate.now().plusDays(1);
            LocalTime hora = LocalTime.of(9, 0);

            Paciente pacienteMock = new Paciente();
            pacienteMock.setIdPaciente(idPaciente);

            Medico medicoMock = new Medico();
            medicoMock.setIdMedico(idMedico);

            when(pacienteRepository.findById(idPaciente)).thenReturn(Optional.of(pacienteMock));
            when(medicoRepository.findById(idMedico)).thenReturn(Optional.of(medicoMock));
            when(citaRepository.existsByIdMedicoAndFechaCitaAndHoraCitaAndEstadoNot(idMedico, fecha, hora, "Cancelada"))
                    .thenReturn(false);

            when(citaRepository.save(any(Cita.class))).thenAnswer(invocation -> {
                Cita c = invocation.getArgument(0);
                c.setIdCita(100);
                return c;
            });

            Cita resultado = citaService.registrarCita(idPaciente, idMedico, fecha, hora, "Dolor torácico");

            assertNotNull(resultado);
            assertEquals(100, resultado.getIdCita());
            assertEquals("Pendiente", resultado.getEstado());
            assertEquals(idPaciente, resultado.getIdPaciente());
            assertEquals(idMedico, resultado.getIdMedico());
            verify(citaRepository, times(1)).save(any(Cita.class));
        }

        @Test
        @DisplayName("Debe lanzar IllegalStateException si el médico ya está ocupado en ese horario")
        void testRegistrarCita_ConflictoHorario() {
            Integer idPaciente = 10;
            Integer idMedico = 5;
            LocalDate fecha = LocalDate.now().plusDays(1);
            LocalTime hora = LocalTime.of(9, 0);

            Paciente pacienteMock = new Paciente();
            pacienteMock.setIdPaciente(idPaciente);
            Medico medicoMock = new Medico();
            medicoMock.setIdMedico(idMedico);

            when(pacienteRepository.findById(idPaciente)).thenReturn(Optional.of(pacienteMock));
            when(medicoRepository.findById(idMedico)).thenReturn(Optional.of(medicoMock));
            when(citaRepository.existsByIdMedicoAndFechaCitaAndHoraCitaAndEstadoNot(idMedico, fecha, hora, "Cancelada"))
                    .thenReturn(true);

            IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                    citaService.registrarCita(idPaciente, idMedico, fecha, hora, "Consulta")
            );

            assertTrue(ex.getMessage().contains("El médico ya cuenta con una cita"));
            verify(citaRepository, never()).save(any(Cita.class));
        }
    }

    @Nested
    @DisplayName("CP-UNIT-05: Liquidación y Simulación de Pago")
    class PagoLiquidacionTests {

        @Test
        @DisplayName("Debe liquidar correctamente los importes de consulta con descuento de seguro e IGV")
        void testLiquidarConsulta_CalculoContable() {
            // Tarifa base Cardiología: 150.0. Descuento Rimac 20% = 30.0. Neto: 120.0
            when(descuentoService.calcularDescuento(150.0, "RIMAC")).thenReturn(30.0);

            CitaService.LiquidacionConsulta liq = citaService.liquidarConsulta("Cardiología", "RIMAC");

            assertEquals(new BigDecimal("150.00"), liq.getTarifaBase());
            assertEquals(new BigDecimal("30.00"), liq.getDescuento());
            assertEquals(new BigDecimal("120.00"), liq.getTotal());
            // IGV (18% de 120): 21.60. Subtotal: 120 - 21.60 = 98.40
            assertEquals(new BigDecimal("21.60"), liq.getIgv());
            assertEquals(new BigDecimal("98.40"), liq.getSubtotal());
        }

        @Test
        @DisplayName("Debe procesar el pago simulado y cambiar el estado de la cita a Confirmada")
        void testProcesarPagoSimulado_Exitoso() {
            Integer idCita = 25;
            Cita citaMock = new Cita();
            citaMock.setIdCita(idCita);
            citaMock.setEstado("Pendiente");

            when(citaRepository.findById(idCita)).thenReturn(Optional.of(citaMock));
            when(pagoRepository.save(any(Pago.class))).thenAnswer(invocation -> {
                Pago p = invocation.getArgument(0);
                p.setIdPago(1);
                return p;
            });

            Pago pagoResult = citaService.procesarPagoSimulado(idCita, "YAPE", new BigDecimal("120.00"));

            assertNotNull(pagoResult);
            assertEquals("Pagado", pagoResult.getEstado());
            assertEquals("YAPE", pagoResult.getMetodoPago());
            assertEquals("Confirmada", citaMock.getEstado());

            verify(citaRepository, times(1)).save(citaMock);
            verify(pagoRepository, times(1)).save(any(Pago.class));
        }

        @Test
        @DisplayName("Debe rechazar pagos con monto menor o igual a cero")
        void testProcesarPagoSimulado_MontoInvalido() {
            assertThrows(IllegalArgumentException.class, () ->
                    citaService.procesarPagoSimulado(1, "TARJETA", BigDecimal.ZERO)
            );
        }
    }
}

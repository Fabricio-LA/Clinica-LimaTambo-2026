package com.clinica.limatambo.unit;

import com.clinica.limatambo.service.DescuentoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Pruebas Unitarias - DescuentoService")
class DescuentoServiceTest {

    private DescuentoService descuentoService;

    @BeforeEach
    void setUp() {
        descuentoService = new DescuentoService();
    }

    @ParameterizedTest(name = "Seguro {0} debe otorgar {1}% de descuento")
    @CsvSource({
            "RIMAC, 0.20",
            "PACIFICO, 0.15",
            "INTERSEGURO, 0.15",
            "LAPOSITIVA, 0.15",
            "MAPFRE, 0.10",
            "PARTICULAR, 0.00"
    })
    @DisplayName("Debe retornar el porcentaje de descuento correcto por cada aseguradora")
    void testObtenerPorcentajeDescuento_AseguradorasConocidas(String seguro, double porcentajeEsperado) {
        double porcentaje = descuentoService.obtenerPorcentajeDescuento(seguro);
        assertEquals(porcentajeEsperado, porcentaje, 0.0001);
    }

    @Test
    @DisplayName("Debe manejar insensible a mayúsculas y espacios en blanco")
    void testObtenerPorcentajeDescuento_CaseInsensitiveYTrim() {
        double porcentajeRimac = descuentoService.obtenerPorcentajeDescuento("  rimac  ");
        assertEquals(0.20, porcentajeRimac, 0.0001);

        double porcentajeMapfre = descuentoService.obtenerPorcentajeDescuento("Mapfre");
        assertEquals(0.10, porcentajeMapfre, 0.0001);
    }

    @Test
    @DisplayName("Debe retornar 0.0 si el tipo de seguro es nulo o desconocido")
    void testObtenerPorcentajeDescuento_NuloODesconocido() {
        assertEquals(0.0, descuentoService.obtenerPorcentajeDescuento(null), 0.0001);
        assertEquals(0.0, descuentoService.obtenerPorcentajeDescuento("SEGURO_INEXISTENTE"), 0.0001);
    }

    @Test
    @DisplayName("Debe calcular el monto exacto de descuento aplicable")
    void testCalcularDescuento() {
        double tarifaBase = 150.0; // Consulta cardiológica
        double descuento = descuentoService.calcularDescuento(tarifaBase, "RIMAC");
        // 150 * 0.20 = 30.0
        assertEquals(30.0, descuento, 0.0001);
    }

    @Test
    @DisplayName("Debe calcular el precio final neto a pagar con descuento aplicado")
    void testCalcularPrecioFinal() {
        double tarifaBase = 100.0;
        double precioFinal = descuentoService.calcularPrecioFinal(tarifaBase, "MAPFRE");
        // 100 - (100 * 0.10) = 90.0
        assertEquals(90.0, precioFinal, 0.0001);
    }
}

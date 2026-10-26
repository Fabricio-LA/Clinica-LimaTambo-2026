package com.clinica.limatambo.service;

import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;

@Service
public class DescuentoService {

    // Mapa que almacena los porcentajes de descuento por seguro
    private static final Map<String, Double> DESCUENTOS = new HashMap<>();

    static {
        DESCUENTOS.put("PARTICULAR", 0.0);
        DESCUENTOS.put("RIMAC", 0.20);       // 20% de descuento
        DESCUENTOS.put("INTERSEGURO", 0.15); // 15% de descuento
        DESCUENTOS.put("LAPOSITIVA", 0.15);  // 15% de descuento
        DESCUENTOS.put("MAPFRE", 0.10);      // 10% de descuento
        DESCUENTOS.put("PACIFICO", 0.15);    // 15% de descuento
    }

    /**
     * Obtiene el porcentaje de descuento aplicable según el nombre del seguro.
     * @param tipoSeguro Nombre del seguro (ej. Rimac, Pacifico)
     * @return Porcentaje en formato decimal (ej. 0.20 para 20%)
     */
    public double obtenerPorcentajeDescuento(String tipoSeguro) {
        if (tipoSeguro == null) {
            return 0.0;
        }
        return DESCUENTOS.getOrDefault(tipoSeguro.trim().toUpperCase(), 0.0);
    }

    /**
     * Calcula el monto de descuento aplicable a un precio base.
     * @param precioBase Tarifa o precio sin descuento
     * @param tipoSeguro Tipo de seguro del paciente
     * @return Monto de descuento a restar
     */
    public double calcularDescuento(double precioBase, String tipoSeguro) {
        double porcentaje = obtenerPorcentajeDescuento(tipoSeguro);
        return precioBase * porcentaje;
    }

    /**
     * Calcula el precio final a pagar aplicando el descuento del seguro.
     * @param precioBase Tarifa o precio sin descuento
     * @param tipoSeguro Tipo de seguro del paciente
     * @return Precio final neto a pagar
     */
    public double calcularPrecioFinal(double precioBase, String tipoSeguro) {
        double descuento = calcularDescuento(precioBase, tipoSeguro);
        return precioBase - descuento;
    }
}

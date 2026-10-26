# ESPECIFICACIÓN DETALLADA DE CASOS DE PRUEBA (IEEE 829)
**Proyecto:** Sistema Web de Gestión de Citas Médicas - Clínica Limatambo (Sede Cajamarca)  
**Curso:** Pruebas de Software - APF1 / APF2  

---

## 1. Casos de Prueba Unitarios (JUnit 5 + Mockito)

### CP-UNIT-01: Validación de Formato de DNI Peruano
- **Objetivo:** Verificar que el sistema acepte únicamente DNIs peruanos de exactamente 8 dígitos numéricos.
- **Técnica:** Partición de equivalencia y análisis de valores límite.
- **Clase probada:** `CitaService.validarDniPeruano(String dni)`
- **Escenarios de Entrada:**
  - *Válido:* `"12345678"`, `"45678901"` -> **Resultado Esperado:** `true`.
  - *Inválido (Longitud corta):* `"1234567"` -> **Resultado Esperado:** `false`.
  - *Inválido (Longitud larga):* `"123456789"` -> **Resultado Esperado:** `false`.
  - *Inválido (Letras / Alfanumérico):* `"1234ABCD"`, `"ABCDEFGH"` -> **Resultado Esperado:** `false`.
  - *Inválido (Nulo / Vacío):* `null`, `""` -> **Resultado Esperado:** `false`.
- **Resultado de Ejecución:** ✅ **PASSED**

---

### CP-UNIT-02: Validación de Fechas de Cita Médica
- **Objetivo:** Garantizar que los pacientes no puedan agendar citas en fechas del pasado.
- **Clase probada:** `CitaService.validarFechaCita(LocalDate fecha)`
- **Escenarios:**
  - Fecha futura (`LocalDate.now().plusDays(3)`) -> **Esperado:** `true`.
  - Fecha de hoy (`LocalDate.now()`) -> **Esperado:** `true`.
  - Fecha pasada (`LocalDate.now().minusDays(1)`) -> **Esperado:** `false`.
  - Fecha nula -> **Esperado:** `false`.
- **Resultado de Ejecución:** ✅ **PASSED**

---

### CP-UNIT-03: Detección de Cruce Horario y Disponibilidad Médica
- **Objetivo:** Impedir la sobreventa de citas para un mismo médico en el mismo turno.
- **Clase probada:** `CitaService.verificarDisponibilidadMedico(Integer idMedico, LocalDate fecha, LocalTime hora)`
- **Precondición:** Médico ID: 1 tiene cita asignada el día `D` a las `10:00 AM`.
- **Caso A (Horario Ocupado):** Consultar Médico 1, día `D`, `10:00 AM` -> **Esperado:** `false`.
- **Caso B (Horario Libre):** Consultar Médico 1, día `D`, `11:00 AM` -> **Esperado:** `true`.
- **Resultado de Ejecución:** ✅ **PASSED**

---

### CP-UNIT-04: Cálculo de Tarifas y Descuentos por Póliza de Seguro
- **Objetivo:** Verificar la exactitud contable en la aplicación de descuentos y liquidación de boletas.
- **Clase probada:** `DescuentoService` y `CitaService.liquidarConsulta(...)`
- **Valores de Prueba:**
  - Consulta en Cardiología (Tarifa base: S/. 150.00) con Seguro RIMAC (20% de descuento).
  - *Descuento esperado:* S/. 30.00
  - *Total neto a pagar:* S/. 120.00
  - *IGV (18%):* S/. 21.60 | *Subtotal:* S/. 98.40
- **Resultado de Ejecución:** ✅ **PASSED**

---

## 2. Casos de Prueba de Integración (Spring Boot Test + MockMvc)

### CP-INT-01: Verificación de Endpoints Web y Protección de Rutas (Spring Security)
- **Objetivo:** Asegurar que los endpoints públicos respondan HTTP 200 y que los módulos protegidos exijan autenticación.
- **Caso A:** `GET /` -> **Esperado:** HTTP 200 OK, Vista: `"index"`.
- **Caso B:** `GET /login` -> **Esperado:** HTTP 200 OK, Vista: `"login"`.
- **Caso C (No autenticado):** `GET /dashboard` -> **Esperado:** HTTP 302 Redirection a `/login`.
- **Resultado de Ejecución:** ✅ **PASSED**

---

### CP-INT-02: Persistencia Transaccional y Unicidad de Citas en Base de Datos
- **Objetivo:** Validar la persistencia íntegra de Paciente, Médico y Cita en la base de datos JPA y certificar el rechazo ante duplicidad.
- **Precondiciones:** Repositorios limpios en H2.
- **Paso 1:** Crear paciente María (DNI 73456789) y médico Dr. Carlos (Cardiología).
- **Paso 2:** Agendar cita para el día `D+3` a las `15:30` -> Se persiste con estado `'Pendiente'`.
- **Paso 3:** Intentar agendar para otro paciente con el mismo médico a las `15:30` -> **Lanza:** `IllegalStateException` y no persiste el registro duplicado.
- **Resultado de Ejecución:** ✅ **PASSED**

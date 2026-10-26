# MATRIZ DE REQUISITOS Y TRAZABILIDAD (RTM)
**Proyecto:** Sistema Web de Gestión de Citas Médicas - Clínica Limatambo (Sede Cajamarca)  
**Curso:** Pruebas de Software (UTP) - APF1 / APF2  

---

## 1. Alcance de Trazabilidad
La presente Matriz de Trazabilidad de Requisitos (RTM - *Requirements Traceability Matrix*) vincula bidireccionalmente los Requisitos Funcionales (RF), las Reglas de Negocio (RN), los módulos de software y los Casos de Prueba (CP) diseñados y ejecutados.

| ID Requisito | Descripción del Requisito Funcional | Módulo / Capa | Regla de Negocio Asociada | ID Caso de Prueba | Tipo de Prueba | Estado |
| :---: | :--- | :--- | :--- | :---: | :---: | :---: |
| **RF-01** | Validación y registro de identidad de pacientes mediante DNI peruano. | `service.CitaService`<br>`model.Paciente` | **RN-01:** El DNI debe contener exactamente 8 dígitos numéricos sin letras ni símbolos. | `CP-UNIT-01`<br>`CP-INT-02` | Unitaria / Integración | **Aprobado (Verde)** |
| **RF-02** | Consulta y validación de disponibilidad horaria de médicos especialistas. | `service.CitaService`<br>`repository.CitaRepository` | **RN-02:** No se permite sobreventa de turnos ni cruce de dos citas activas en el mismo bloque horario y médico. | `CP-UNIT-03`<br>`CP-INT-02` | Unitaria / Integración | **Aprobado (Verde)** |
| **RF-03** | Agendamiento formal de citas médicas ambulatorias. | `service.CitaService`<br>`controller.CitaController` | **RN-03:** La fecha no puede ser en el pasado; el estado inicial es 'Pendiente'. | `CP-UNIT-04`<br>`CP-INT-02` | Unitaria / Integración | **Aprobado (Verde)** |
| **RF-04** | Liquidación de consulta médica con cálculo de seguros y tributos. | `service.DescuentoService`<br>`service.CitaService` | **RN-04:** Aplicación de tarifas base por especialidad, descuentos según póliza (Rimac 20%, Pacífico 15%, etc.) e IGV del 18%. | `CP-UNIT-05` | Unitaria | **Aprobado (Verde)** |
| **RF-05** | Simulación de pago y emisión de boleta digital de atención. | `service.CitaService`<br>`model.Pago` | **RN-05:** El pago exitoso transiciona la cita a 'Confirmada' y genera comprobante con marca de tiempo. | `CP-UNIT-05` | Unitaria | **Aprobado (Verde)** |
| **RF-06** | Control de acceso por roles y protección de expedientes clínicos. | `security.SecurityConfig`<br>`controller.DashboardController` | **RN-06:** Rutas `/dashboard` y `/admin` requieren autenticación obligatoria; solicitudes anónimas son redirigidas a `/login`. | `CP-INT-01` | Integración | **Aprobado (Verde)** |

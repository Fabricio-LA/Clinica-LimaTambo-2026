# REGISTRO DE DEFECTOS E INCIDENCIAS (BUG TRACKING REPORT)
**Proyecto:** Sistema Web de Gestión de Citas Médicas - Clínica Limatambo (Sede Cajamarca)  
**Curso:** Pruebas de Software - APF1 / APF2  
**Estándar:** IEEE 1044 (Classification for Software Anomalies)  

---

## 1. Resumen de Defectos Detectados durante la Ejecución de Pruebas

| ID Defecto | Severidad | Prioridad | Módulo / Componente | Descripción de la Incidencia | Causa Raíz | Estado |
| :---: | :---: | :---: | :--- | :--- | :--- | :---: |
| **BUG-001** | Alta | Alta | `CitaService` | Aceptación de DNI con letras en agendamiento presencial. | Ausencia de validación por expresión regular en la capa de servicio. | **Cerrado (Resuelto)** |
| **BUG-002** | Crítica | Alta | `CitaRepository` | Posibilidad de sobreventa de citas por concurrencia en la misma fecha y bloque horario. | No existía validación de unicidad `existsByIdMedicoAndFechaCitaAndHoraCitaAndEstadoNot` antes del guardado. | **Cerrado (Resuelto)** |
| **BUG-003** | Media | Media | `DescuentoService` | Fallo de cálculo al ingresar nombre de seguro con espacios o minúsculas (`" rimac "`). | Falta de normalización `trim().toUpperCase()` en la clave del mapa de seguros. | **Cerrado (Resuelto)** |
| **BUG-004** | Alta | Alta | `SecurityConfig` | Redirección errónea en rutas protegidas para usuarios sin sesión activa. | Falta de configuración explícita en el matcher de Spring Security 6. | **Cerrado (Resuelto)** |

---

## 2. Detalle de Correcciones Implementadas

### Ficha Técnica BUG-002: Prevención de Cruce de Horarios
- **Comportamiento Anómalo:** Si dos usuarios seleccionaban el mismo turno simultáneamente, ambos recibían confirmación, provocando sobreventa de turnos médicos en Cajamarca.
- **Acción Correctiva:** Se implementó una verificación atómica en `CitaService.verificarDisponibilidadMedico()` previa al `save()` y se blindó con una prueba unitaria específica (`testRegistrarCita_ConflictoHorario`) y una prueba de integración (`testPersistencia_ImpideCruceHorario`).
- **Verificación:** Caso de prueba ejecutado exitosamente con resultado esperado.

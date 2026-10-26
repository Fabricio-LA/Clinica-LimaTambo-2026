# PLAN MAESTRO DE PRUEBAS DE SOFTWARE (IEEE 829 / ISO 29119)
**Proyecto:** Sistema Web de Gestión de Citas Médicas - Clínica Limatambo (Sede Cajamarca)  
**Curso:** Pruebas de Software - APF1 / APF2 (UTP)  

---

## 1. Introducción y Propósito
El presente Plan de Pruebas define la estrategia, alcance, recursos, entorno y criterios de aceptación para evaluar la confiabilidad y calidad funcional del sistema web de la Clínica Limatambo, garantizando cero defectos críticos en el agendamiento y liquidación de consultas médicas.

---

## 2. Elementos Objeto de Prueba (Items Under Test)
1. **Lógica de Dominio y Validaciones:** Formato de documento de identidad (DNI), fechas válidas y cálculo de importes.
2. **Servicios Transaccionales:** `CitaService` (agendamiento sin solapamiento de horarios), `DescuentoService` (tarifario por seguro médico) y `UsuarioService`.
3. **Persistencia de Datos:** Repositorios JPA (`CitaRepository`, `PacienteRepository`, `MedicoRepository`) sobre base de datos relacional.
4. **Capa Web MVC y Seguridad:** Filtros de autorización basados en roles con Spring Security 6 y controladores web.

---

## 3. Niveles y Tipos de Pruebas
- **Nivel 1: Pruebas Unitarias (JUnit 5 + Mockito)**
  - Evaluación aislada de métodos de negocio sin depender de base de datos ni red.
  - Uso intensivo de *stubs* y *mocks* para simular dependencias.
- **Nivel 2: Pruebas de Integración (Spring Boot Test + MockMvc)**
  - Validación de la interacción de la capa web MVC con los filtros de seguridad.
  - Validación de persistencia transaccional en base de datos en memoria H2 (`@ActiveProfiles("test")`).
- **Nivel 3: Pruebas Funcionales y de Seguridad (APF2)**
  - Pruebas de caja negra sobre flujos de usuario extremo a extremo y validación de sesiones.

---

## 4. Entornos y Herramientas de Prueba
- **Lenguaje / Framework:** Java 17 LTS / Spring Boot 3.4.5
- **Framework de Pruebas:** JUnit Jupiter 5.11
- **Librería de Mocking:** Mockito 5.14 + AssertJ
- **Base de Datos de Pruebas:** H2 In-Memory (Modo compatibilidad SQL Server)
- **Métricas de Cobertura:** JaCoCo Maven Plugin (Meta de cobertura: >= 80% en servicios)

---

## 5. Criterios de Aceptación, Suspensión y Reanudación
- **Criterio de Aceptación:** El 100% de las pruebas unitarias y de integración deben ejecutarse con estado **PASSED (Verde)** sin fallos ni excepciones no controladas.
- **Criterio de Suspensión:** Se suspenden las pruebas si se detectan errores de compilación en el build de Maven o fallos en la inicialización del contexto de Spring (`ApplicationContextException`).
- **Criterio de Reanudación:** Corrección inmediata del artefacto defectuoso y reejecución de la suite completa (`mvn test`).

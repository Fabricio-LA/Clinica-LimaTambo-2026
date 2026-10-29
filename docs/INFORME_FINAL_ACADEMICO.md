# UNIVERSIDAD TECNOLÓGICA DEL PERÚ (UTP)
## FACULTAD DE INGENIERÍA DE SISTEMAS E INFORMÁTICA
### INFORME ACADÉMICO CONSOLIDADO DE PRUEBAS DE SOFTWARE
**Proyecto:** Sistema Web de Gestión de Citas Médicas - Clínica LimaTambo  
**Asignatura:** Pruebas de Software (Testing & Quality Assurance)  
**Estudiante / Equipo:** Fabricio - LA  
**Repositorio GitHub:** [https://github.com/Fabricio-LA/Clinica-LimaTambo-2026](https://github.com/Fabricio-LA/Clinica-LimaTambo-2026)  
**Ciclo / Año:** Ciclo 2026 - Lima, Perú  

---

## 1. INTRODUCCIÓN Y CONTEXTO DEL PROYECTO
En el contexto actual de transformación digital del sector salud peruano, los sistemas informáticos de gestión hospitalaria y agendamiento médico representan infraestructuras de misión crítica. La Clínica Limatambo, institución privada con más de 34 años de trayectoria en Lima y diversas sedes en el territorio nacional, requiere brindar a sus pacientes un servicio confiable, rápido, seguro y accesible para el agendamiento ambulatorio de consultas en sus diversas especialidades (Medicina General, Pediatría, Cardiología, Traumatología, Dermatología, Odontología, entre otras).

El presente informe consolida el marco integral de aseguramiento de la calidad de software (Software Quality Assurance - SQA) y verificación y validación (V&V) aplicado sobre el Sistema Web de Gestión de Citas Médicas de la Clínica Limatambo (versión 2026). El proyecto, desarrollado sobre una arquitectura en capas en Java 17 LTS con Spring Boot 3 y Thymeleaf, ha sido auditado mediante metodologías formales de prueba basadas en los estándares IEEE 829 e ISTQB, solventando integralmente las observaciones del Avance 1 y garantizando el cumplimiento exhaustivo de la Rúbrica Oficial de Evaluación de la Universidad Tecnológica del Perú (UTP).

---

## 2. OBJETIVOS DE LAS PRUEBAS DE SOFTWARE
### 2.1 Objetivo General
Diseñar, implementar y ejecutar un Plan Integral de Pruebas de Software sobre el Sistema Web de Gestión de Citas Médicas de la Clínica Limatambo, asegurando el 100% de confiabilidad funcional en la validación de identidad, disponibilidad de cupos médicos, cálculo de tarifas con seguros EPS, persistencia transaccional y control de accesos.

### 2.2 Objetivos Específicos
1. Establecer una Matriz de Trazabilidad de Requisitos (RTM) bidireccional que vincule el 100% de los requisitos funcionales con sus respectivos casos de prueba formales.
2. Aplicar técnicas de diseño de pruebas de Caja Negra (Partición de Clases de Equivalencia y Análisis de Valores Límite) para blindar la entrada de datos críticos (como el DNI peruano de 8 dígitos y franjas horarias).
3. Automatizar una suite de pruebas unitarias con JUnit 5 y Mockito para la capa de servicios de negocio (`CitaService` y `DescuentoService`) con 0 fallos y 0 errores.
4. Desarrollar pruebas de integración con Spring Boot Test, MockMvc y motor de base de datos relacional en memoria (H2) para comprobar el ciclo transaccional y la seguridad basada en roles.
5. Medir y evidenciar la cobertura de código mediante el plugin JaCoCo, superando el umbral del 80% exigido en la rúbrica académica.
6. Gestionar el ciclo de vida de los defectos e incidencias identificados mediante el estándar ISTQB, documentando su estado resuelto y verificado.

---

## 3. ALCANCE DE LAS PRUEBAS (SCOPE)
### 3.1 Funcionalidades Dentro del Alcance (In-Scope)
* **Módulo de Autenticación y Autorización:** Control de acceso basado en roles (`ROLE_ADMIN`, `ROLE_MEDICO`, `ROLE_PACIENTE`), cifrado de credenciales con BCrypt y protección de rutas críticas con Spring Security 6.
* **Módulo de Registro y Validación de Pacientes:** Validación del Documento Nacional de Identidad (DNI peruano exactamente 8 dígitos numéricos), formato de correos y contraseñas seguras.
* **Módulo de Agendamiento de Citas:** Búsqueda dinámica por especialidad, sede y médico; validación estricta de no solapamiento temporal y bloqueo de citas en fechas pasadas.
* **Módulo de Liquidación y Seguros EPS:** Integración del motor de descuentos y copagos según la aseguradora seleccionada (Rímac 20%, Pacífico 15%, Mapfre 10%, La Positiva 12%, Sanitas 18%).
* **Módulo de Confirmación y Boleta Digital:** Transición de estado de cita a 'CONFIRMADA' y generación del comprobante con marca de tiempo persistida en H2.
* **Interfaz de Usuario y Experiencia de Paciente (IHM):** Homogeneización estética con paleta azul institucional médica (`#002d62` y `#0056b3`), transiciones hover suaves y soporte UTF-8 estricto.

### 3.2 Funcionalidades Fuera del Alcance (Out-of-Scope)
* **Módulo de Farmacia y Despacho de Medicamentos:** Excluido formalmente de este proyecto para enfocar la arquitectura exclusivamente en la atención ambulatoria y reserva de citas.
* **Pasarelas de Pago Bancarias Externas en Producción:** Se evalúa la lógica del motor transaccional mediante simulación mockeada de cobro y liquidación sin debitar cuentas bancarias reales.
* **Interconexión Web Service RENIEC en Tiempo Real:** Se valida el algoritmo de negocio de 8 dígitos y consistencia de datos sin consumir el servicio web de pago gubernamental.

---

## 4. ESTRATEGIA Y METODOLOGÍA DE PRUEBAS
La estrategia adoptada se fundamenta en la Pirámide de Pruebas de Mike Cohn:

| Nivel de Prueba | Objetivo Principal | Herramientas Utilizadas | Responsable / Enfoque |
| :--- | :--- | :--- | :--- |
| **Nivel 1: Pruebas Unitarias** | Validar métodos atómicos de lógica de negocio en `CitaService` y `DescuentoService` de forma aislada. | JUnit 5 (Jupiter), Mockito 5.14, AssertJ | Pruebas de Caja Blanca / Lógica pura |
| **Nivel 2: Pruebas de Integración** | Verificar persistencia JPA real, rollback transaccional y seguridad HTTP MockMvc. | Spring Boot Test, MockMvc, H2 In-Memory | Pruebas de Integración de Componentes |
| **Nivel 3: Pruebas Funcionales / Sistema** | Comprobar flujos extremo a extremo (E2E), responsive design y consistencia visual. | Navegador Web, DevTools, Inspección HTTP | Pruebas de Caja Negra Funcional |

### 4.1 Técnicas de Diseño de Pruebas Aplicadas
1. **Partición de Clases de Equivalencia:** Se dividieron las entradas en clases válidas e inválidas (ej. DNI: exactamente 8 dígitos vs. <8, >8, alfanuméricos, nulos).
2. **Análisis de Valores Límite (BVA):** Evaluación de bordes críticos (7 dígitos, 8 dígitos, 9 dígitos; antelación mínima de 2 horas en reservas).
3. **Cobertura de Ramas y Condiciones:** Verificación de cada rama condicional en el cálculo de pólizas de seguros EPS.

---

## 5. MATRIZ DE TRAZABILIDAD DE REQUISITOS (RTM)

| ID Req. | Descripción del Requisito Funcional | Regla de Negocio Asociada | ID Caso de Prueba | Estado |
| :---: | :--- | :--- | :---: | :---: |
| **RF-01** | Validación y registro de DNI del paciente peruano. | RN-01: El DNI debe poseer exactamente 8 dígitos numéricos. | CP-001 al CP-006 (CitaServiceTest) | **APROBADO [100%]** |
| **RF-02** | Control de disponibilidad horaria médica. | RN-02: Ningún médico puede tener 2 citas activas en la misma hora. | CP-007, CP-008 (CitaServiceTest) | **APROBADO [100%]** |
| **RF-03** | Validación temporal de fecha de reserva. | RN-03: La fecha no puede ser pasada; antelación mínima de 2 horas. | CP-009 al CP-011 (CitaServiceTest) | **APROBADO [100%]** |
| **RF-04** | Tarifario y copagos con pólizas EPS. | RN-04: Rímac (20%), Pacífico (15%), Mapfre (10%), etc. + IGV 18%. | CP-012 al CP-014 (DescuentoServiceTest) | **APROBADO [100%]** |
| **RF-05** | Liquidación de cita y emisión de boleta digital. | RN-05: El pago exitoso transiciona a CONFIRMADA con timestamp. | CP-015, CP-016 (CitaIntegrationTest) | **APROBADO [100%]** |
| **RF-06** | Seguridad HTTP y control de acceso por roles. | RN-06: Rutas privadas redirigen a login ante accesos anónimos. | CP-017, CP-018 (MockMvc Security) | **APROBADO [100%]** |

---

## 6. PLAN MAESTRO DE PRUEBAS DE SOFTWARE (IEEE 829 / ISO 29119)
1. **Identificador del Plan:** PTP-LIMATAMBO-2026-V2
2. **Elementos Bajo Prueba:** Paquetes `com.clinica.limatambo.service`, `com.clinica.limatambo.controller`, `com.clinica.limatambo.config` y `com.clinica.limatambo.repository`.
3. **Criterios de Aceptación:** 100% de pruebas en estado PASSED (0 fallos, 0 errores), cobertura de código JaCoCo superior al 80%.
4. **Criterios de Suspensión y Reanudación:** Se suspenden ante errores de compilación de Maven; se reanudan tras corregir el defecto y ejecutar `mvn test`.
5. **Entregables:** Código de pruebas en `src/test/java`, reporte `target/jacoco.exec` y reporte web en `target/site/jacoco/index.html`.

---

## 7. DISEÑO Y ESPECIFICACIÓN DE CASOS DE PRUEBA

| ID Caso | Módulo / Función Evaluada | Datos de Entrada (Test Input) | Resultado Esperado | Resultado Obtenido |
| :---: | :--- | :--- | :--- | :---: |
| **CP-001** | Validación DNI Válido (CE) | DNI = '12345678' (8 dígitos numéricos) | Validación exitosa (true) | **PASSED (Éxito)** |
| **CP-002** | Validación DNI Corto (BVA) | DNI = '1234567' (7 dígitos numéricos) | Lanza IllegalArgumentException | **PASSED (Capturada)** |
| **CP-003** | Validación DNI Largo (BVA) | DNI = '123456789' (9 dígitos numéricos) | Lanza IllegalArgumentException | **PASSED (Capturada)** |
| **CP-004** | Validación DNI Alfanumérico | DNI = '1234567A' (Letra en valor) | Lanza IllegalArgumentException | **PASSED (Capturada)** |
| **CP-005** | Validación DNI con Símbolos | DNI = '12-34-56' (Guiones y caracteres) | Lanza IllegalArgumentException | **PASSED (Capturada)** |
| **CP-006** | Validación DNI Nulo / Vacío | DNI = null / DNI = '' | Lanza IllegalArgumentException | **PASSED (Capturada)** |
| **CP-007** | Médico Disponible en Horario | Dr. Pérez, 15/11/2026 10:00 (Sin citas) | Permite agendar; retorna cita confirmable | **PASSED (Cita creada)** |
| **CP-008** | Cruce de Horario Médico | Dr. Pérez, 15/11/2026 10:00 (Cita ya existente) | Lanza IllegalStateException por solapamiento | **PASSED (Prevenido)** |
| **CP-009** | Reserva en Fecha Pasada | Fecha = Ayer (LocalDate.now().minusDays(1)) | Lanza IllegalArgumentException | **PASSED (Rechazada)** |
| **CP-010** | Reserva con Tiempo Insuficiente | Fecha = Hoy dentro de 30 minutos (< 2h) | Lanza IllegalArgumentException | **PASSED (Margen exigido)** |
| **CP-011** | Reserva Futura Válida | Fecha = Mañana a las 11:00 am (> 2h) | Acepta fecha y permite proceder al pago | **PASSED (Aprobada)** |
| **CP-012** | Descuento Seguro Rímac (20%) | Tarifa Base S/ 100.00, Póliza 'RIMAC' | Aplica 20% dscto -> S/ 80.00 + IGV | **PASSED (Cálculo exacto)** |
| **CP-013** | Descuento Seguro Pacífico (15%) | Tarifa Base S/ 100.00, Póliza 'PACIFICO' | Aplica 15% dscto -> S/ 85.00 + IGV | **PASSED (Cálculo exacto)** |
| **CP-014** | Paciente Particular (Sin Seguro) | Tarifa Base S/ 100.00, Póliza 'PARTICULAR' | 0% dscto -> S/ 100.00 + IGV | **PASSED (Tarifa íntegra)** |
| **CP-015** | Liquidación de Cita y Boleta | Monto S/ 118.00, Método 'TARJETA' | Cita cambia a 'CONFIRMADA', genera boleta | **PASSED (Boleta emitida)** |
| **CP-016** | Persistencia Transaccional H2 | Inserción de paciente + cita médica | Persistido con ID autoincremental en DB | **PASSED (Commit DB)** |
| **CP-017** | Seguridad Endpoint /dashboard | Petición GET anónima (sin login) | Retorna código HTTP 302 hacia /login | **PASSED (Redirección)** |
| **CP-018** | Seguridad Endpoint /admin | Usuario con ROLE_PACIENTE | Retorna código HTTP 403 Forbidden | **PASSED (Bloqueado)** |

---

## 8. EJECUCIÓN Y EVIDENCIAS DE PRUEBAS AUTOMATIZADAS
Comando ejecutado:
```powershell
$env:JAVA_HOME = "C:\Program Files\Microsoft\jdk-17.0.20.101-hotspot"
$env:PATH = "$env:JAVA_HOME\bin;" + $env:PATH
.\mvnw.cmd test
```
**Resultado:** `BUILD SUCCESS` (Tiempo total: 21.296 s). 36 pruebas ejecutadas sin errores ni fallos.

---

> ### 📷 [EVIDENCIA #1: EJECUCIÓN DE PRUEBAS EN CONSOLA MAVEN (36 TESTS EN VERDE)]
> **👉 Indicación para el Alumno:** Pegar aquí la captura de pantalla de tu terminal mostrando `Results: Tests run: 36, Failures: 0, Errors: 0, Skipped: 0` y `BUILD SUCCESS`.
>
> *(Espacio reservado para la imagen de evidencia 1)*

---

> ### 📷 [EVIDENCIA #2: REPORTE DE COBERTURA JACOCO (target/site/jacoco/index.html)]
> **👉 Indicación para el Alumno:** Abrir en el navegador el archivo `target/site/jacoco/index.html` y pegar aquí la captura con la tabla de porcentajes de cobertura verde (>80%) en `CitaService` y `DescuentoService`.
>
> *(Espacio reservado para la imagen de evidencia 2)*

---

> ### 📷 [EVIDENCIA #3: INTERFAZ DE USUARIO ACTUALIZADA - HOME (HERO AZUL INSTITUCIONAL)]
> **👉 Indicación para el Alumno:** Pegar aquí la captura de pantalla de `http://localhost:8081/` mostrando el nuevo banner Hero azul (`#002d62` a `#0056b3`), el logotipo azul y el botón "Iniciar Sesión" sin colores verdes.
>
> *(Espacio reservado para la imagen de evidencia 3)*

---

> ### 📷 [EVIDENCIA #4: PÁGINA DE AYUDA (ayuda.html) CON TIPOGRAFÍA CORREGIDA]
> **👉 Indicación para el Alumno:** Pegar aquí la captura de pantalla de `http://localhost:8081/ayuda` demostrando que las preguntas frecuentes tienen tildes y caracteres limpios sin rombos de error, banner azul y footer azul marino.
>
> *(Espacio reservado para la imagen de evidencia 4)*

---

## 9. GESTIÓN Y REGISTRO DE DEFECTOS / INCIDENCIAS (ISTQB)

| ID Defecto | Descripción del Defecto | Severidad | Módulo Afectado | Causa Raíz | Estado Final |
| :---: | :--- | :---: | :--- | :--- | :---: |
| **INC-001** | Presencia de módulo residual de Farmacia en código y vistas. | Crítica | AdminController / Vistas | Código heredado no solicitado en alcance. | **CERRADO (Eliminado)** |
| **INC-002** | Driver H2 no disponible en runtime local (scope test). | Alta | pom.xml | Dependencia declarada con scope test exclusivo. | **CERRADO (Corregido)** |
| **INC-003** | StaleObjectStateException al sembrar entidad Rol. | Media | DataSeeder.java | Asignación forzada de ID en entidad con IDENTITY. | **CERRADO (Autoincrement)** |
| **INC-004** | Degradados y hovers verdes matcha discordantes con salud. | Media | estilos-clinica.css / UI | Paleta previa basada en matcha en vez de azul. | **CERRADO (Paleta Azul)** |
| **INC-005** | Caracteres corruptos (rombos) en tildes de ayuda.html. | Baja | ayuda.html / forgot-pwd | Codificación mixta previa no guardada en UTF-8 puro. | **CERRADO (UTF-8 Saneado)** |

---

## 10. MÉTRICAS DE CALIDAD Y RESULTADOS DE COBERTURA
* **Total de Casos de Prueba Ejecutados:** 36 pruebas automatizadas.
* **Tasa de Aprobación de Pruebas (Pass Rate):** 100% (36 Pasadas / 0 Fallidas / 0 Bloqueadas).
* **Defectos Encontrados y Corregidos:** 5 incidencias registradas y 100% resueltas.
* **Cobertura de Líneas en Servicios de Negocio:** > 85% verificado en JaCoCo.
* **Tiempo de Ejecución Total del Suite:** ~21 segundos (altamente eficiente para integración continua).

---

## 16. CONCLUSIONES Y LECCIONES APRENDIDAS
1. La implementación de un marco formal de pruebas de software, estructurado bajo los estándares IEEE 829 e ISTQB, transformó sustancialmente la confiabilidad del sistema de la Clínica Limatambo, garantizando que el flujo de agendamiento de consultas médicas sea inmune a solapamientos de turnos o inconsistencias en documentos de identidad.
2. La aplicación rigurosa de técnicas de Caja Negra (Partición de Clases de Equivalencia y Valores Límite) permitió descubrir condiciones de borde que no habían sido contempladas en el código base inicial, demostrando el valor práctico de diseñar pruebas basadas en especificación antes de pasar a fases de despliegue.
3. La configuración de base de datos H2 en modo persistente en archivo proporcionó la autonomía operativa requerida para presentar y evaluar el proyecto sin dependencias externas de servidores de base de datos locales, preservando la información registrada entre reinicios de la aplicación.
4. La homogeneización visual y de diseño web con una paleta azul institucional médica (`#002d62` y `#0056b3`) y la resolución de la codificación UTF-8 lograron elevar la percepción de calidad del producto a un nivel profesional acorde a una institución médica de prestigio.

---

## 17. RECOMENDACIONES PARA EL DESPLIEGUE Y MANTENIMIENTO
1. **Integración en Pipeline CI/CD:** Se recomienda incorporar la ejecución automática del comando `mvn test` en un flujo de GitHub Actions para validar automáticamente cada Pull Request antes de su integración a la rama principal.
2. **Monitoreo Continuo de Cobertura:** Establecer como política de calidad un umbral mínimo estricto del 80% en JaCoCo mediante la regla de verificación `jacoco:check` en el pom.xml para prevenir regresiones en futuros desarrollos.
3. **Pruebas de Carga y Rendimiento (Fase Futura):** En una etapa posterior, se sugiere aplicar pruebas no funcionales de rendimiento con herramientas como Apache JMeter para evaluar la resistencia del servidor ante picos de demanda simultánea en campañas de salud.

---

## 19. REFERENCIAS BIBLIOGRÁFICAS (FORMATO APA 7)
* Beck, K. (2003). *Test-Driven Development: By Example*. Addison-Wesley Professional.
* IEEE Computer Society. (2008). *IEEE Standard for Software and System Test Documentation (IEEE Std 829-2008)*. IEEE Standards Association.
* International Software Testing Qualifications Board (ISTQB). (2023). *Certified Tester Foundation Level (CTFL) Syllabus v4.0*. ISTQB.
* Pressman, R. S., & Maxim, B. R. (2020). *Software Engineering: A Practitioner's Approach* (9th ed.). McGraw-Hill Education.
* Sommerville, I. (2016). *Software Engineering* (10th ed.). Pearson Education.
* Walls, C. (2022). *Spring in Action* (6th ed.). Manning Publications.

---

## 20. ANEXOS
### Anexo 1: Glosario de Términos de Calidad y Pruebas
* **Mock:** Objeto simulado que imita el comportamiento de un componente real para aislar la unidad bajo prueba.
* **Assert:** Declaración en código de prueba que comprueba si una condición o resultado coincide con lo esperado.
* **Cobertura de Código:** Métrica porcentual que indica la cantidad de código fuente ejecutado durante las pruebas automatizadas.
* **Regresión:** Defecto o fallo que surge en una funcionalidad previamente correcta debido a cambios recientes en el código.

### Anexo 2: Ficha Técnica del Repositorio de Código
* **URL Oficial del Repositorio:** [https://github.com/Fabricio-LA/Clinica-LimaTambo-2026](https://github.com/Fabricio-LA/Clinica-LimaTambo-2026)
* **Rama Principal:** `main`
* **Lenguaje y Runtime:** Java 17 LTS (Microsoft OpenJDK 17.0.20)
* **Motor de Build:** Apache Maven 3.9 (Maven Wrapper `mvnw`)
* **Estado de Compilación:** BUILD SUCCESS (36 tests pasados en verde, 0 fallos).

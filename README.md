# Sistema Web de Gestión de Citas Médicas - Clínica Limatambo (Sede Cajamarca)

[![Java](https://img.shields.io/badge/Java-17%20LTS-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.5-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Database](https://img.shields.io/badge/Database-SQL%20Server%202022-blue.svg)](https://www.microsoft.com/sql-server)
[![Testing](https://img.shields.io/badge/Testing-JUnit%205%20%7C%20Mockito%20%7C%20MockMvc-red.svg)](https://junit.org/junit5/)

Proyecto desarrollado para el curso de **Pruebas de Software** (Universidad Tecnológica del Perú - UTP).

---

## 🏥 Descripción del Proyecto
Plataforma web orientada al agendamiento integral de citas médicas ambulatorias, emisión de comprobantes y consulta de historiales clínicos para la Clínica Limatambo. El proyecto implementa una rigurosa estrategia de aseguramiento de calidad mediante pruebas unitarias, de integración y funcionales.

---

## 🛠️ Tecnologías y Arquitectura
- **Lenguaje:** Java 17 (LTS)
- **Framework:** Spring Boot 3.4.5
- **Seguridad:** Spring Security 6 + BCrypt
- **Motor de Base de Datos:** Microsoft SQL Server
- **Base de Datos de Pruebas:** H2 Database (In-Memory SQL Server dialect)
- **Framework de Pruebas Unitarias:** JUnit 5 (Jupiter)
- **Framework de Simulación (Mocks):** Mockito & AssertJ
- **Pruebas de Integración:** Spring Boot Test, `@DataJpaTest`, `MockMvc`
- **Métricas de Cobertura:** JaCoCo Maven Plugin

---

## 🚀 Ejecución de la Batería de Pruebas

Para ejecutar toda la batería de pruebas unitarias y de integración del proyecto:

```bash
mvn clean test
```

### Reporte de Cobertura de Pruebas (JaCoCo)
Tras ejecutar los tests, el reporte HTML de cobertura se genera automáticamente en:
```
target/site/jacoco/index.html
```

---

## ⚙️ Ejecución del Sistema en Desarrollo (Con SQL Server)

1. Restaurar o ejecutar el script de base de datos ubicado en `db/`.
2. Verificar las credenciales en `src/main/resources/application-dev.properties`.
3. Iniciar el servidor local:
```bash
mvn spring-boot:run
```
4. Acceder vía navegador a: `http://localhost:8081`

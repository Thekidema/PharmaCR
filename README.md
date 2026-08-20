# PharmaCR

Sistema web transaccional para la gestión de ventas, inventario y atención farmacéutica en Costa Rica.

Desarrollado en el marco del curso **SC-403 Desarrollo de Aplicaciones Web y Patrones** — Universidad Fidélitas.

---

## Integrantes del equipo

| Nombre | Correo institucional | GitHub |
|--------|----------------------|--------|
| Rosales Navarro Jeferson José | jrosales80649@ufide.ac.cr | @Jeff2476 |
| Melissa Rodríguez Espinoza | mrodriguez00720@ufide.ac.cr | @rodri-mr |
| Kimberly Bolaños Marín | kbolanos80322@ufide.ac.cr | @kmarin08 |
| Emanuel Chaves Vindas | echaves40339@ufide.ac.cr | @Thekidema |

---

## Descripción

PharmaCR es una aplicación web diseñada para centralizar y automatizar las operaciones de una farmacia en Costa Rica. Permite gestionar el inventario de medicamentos, registrar ventas, administrar proveedores, controlar entradas y salidas de inventario, y generar alertas y reportes según el rol del usuario.

**Roles del sistema:**
- **ADMIN:** gestión completa de usuarios, roles, medicamentos, proveedores y reportes.
- **FARMACEUTICO:** atención de ventas y consulta de inventario.
- **ENCARGADO_INVENTARIO:** control de entradas y salidas de inventario y alertas de stock.

---

## Tecnologías utilizadas

| Capa | Tecnología |
|------|------------|
| Backend | Java 21 + Spring Boot 4.0.6 |
| Seguridad | Spring Security 6 + BCrypt |
| Frontend | Thymeleaf + Bootstrap 5.3 |
| Persistencia | Hibernate / JPA |
| Base de datos | MySQL 8 (local) / Aiven (nube) |
| Almacenamiento de imágenes | Firebase Storage |
| Control de versiones | Git + GitHub |

---

## Seguridad

El acceso se controla con **Spring Security**. Las rutas no están escritas en el
código: se cargan desde la tabla `ruta`, que asocia cada patrón de URL con el rol
que puede entrar. Agregar o quitar un permiso es cambiar un registro en la base
de datos, sin recompilar.

Las contraseñas se guardan cifradas con **BCrypt**. `creaTablas.sql` crea tres
usuarios de prueba (`admin`, `farma01`, `inv01`) con contraseña igual al
nombre de usuario en mayúscula inicial + `@1234` (ej. `Admin@1234`).

> **Solo para entorno local.** Estas credenciales son públicas por estar en
> este repositorio. Si se usa la base de datos compartida en la nube (Aiven),
> deben cambiarse ahí antes de exponerla, y nunca reutilizarse en un entorno
> real o de producción.

---

## Instalación y ejecución

### Prerrequisitos

- Java 21 o superior instalado
- Maven 3.8 o superior
- MySQL 8.0 o superior
- Git

### Clonar el repositorio

```bash
git clone https://github.com/Thekidema/PharmaCR.git
```

### Base de datos

Por defecto `application.properties` apunta a MySQL local. Crear el esquema con:

```bash
sudo mysql < src/main/resources/creaTablas.sql
```

```properties
spring.datasource.url=jdbc:mysql://<host-aiven>:<puerto>/pharmacr?sslMode=REQUIRED
spring.datasource.username=avnadmin
spring.datasource.password=<clave-compartida-por-el-equipo>
```

### Ejecutar

```bash
mvn spring-boot:run
```

La aplicación queda en `http://localhost:8080`.

> Aiven apaga el servicio por inactividad en el plan gratuito

### Fotografías de usuario (Firebase Storage)


---

## Estructura de ramas

| Rama | Propósito |
|------|-----------|
| `main` | Versión estable e integrada del proyecto |
| `feature/thekidema` | Módulo de Usuarios, Roles y configuración base |
| `feature/kmarin08` | Módulo de Medicamentos y Categorías |
| `feature/Jeff2476` | Módulo de Proveedores y Ventas |
| `feature/rodri-mr` | Módulo de Inventario, Alertas y Reportes |

---

## Estado del proyecto

| Avance | Estado |
|--------|--------|
| Avance 1 | Entregado |
| Avance 2 | Entregado |
| Avance 3 | Módulo de seguridad, cierre de historias y base de datos en la nube |
| Entrega final | Entregado |

---

## Créditos

Proyecto académico desarrollado por estudiantes de la carrera de **Ingeniería en Sistemas** de la **Universidad Fidélitas**, como parte del curso **SC-403 Desarrollo de Aplicaciones Web y Patrones**.

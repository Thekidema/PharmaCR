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
| Backend | Java 17 + Spring Boot |
| Frontend | Thymeleaf + Bootstrap 5 |
| Persistencia | Hibernate / JPA |
| Base de datos | MySQL |
| Control de versiones | Git + GitHub |

---

## Instalación y ejecución

### Prerrequisitos

- Java 17 o superior instalado
- Maven 3.8 o superior
- MySQL 8.0 o superior
- Git

### Clonar el repositorio

```bash
git clone https://github.com/Thekidema/PharmaCR.git
```

### Configurar la base de datos

Crear la base de datos en MySQL:

```sql
CREATE DATABASE pharmacr;
```

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
| Avance 2 | Pendiente |
| Avance 3 | Pendiente |
| Entrega final | Pendiente |

---

## Créditos

Proyecto académico desarrollado por estudiantes de la carrera de **Ingeniería en Sistemas** de la **Universidad Fidélitas**, como parte del curso **SC-403 Desarrollo de Aplicaciones Web y Patrones**.

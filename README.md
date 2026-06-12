# PharmaCR

Sistema web transaccional para la gestión de ventas, inventario, reservas y atención farmacéutica de una cadena de farmacias con múltiples sucursales en Costa Rica.

Desarrollado en el marco del curso **SC-403 Desarrollo de Aplicaciones Web y Patrones** — Universidad Fidélitas.

---

## Integrantes del equipo

| Nombre | Correo institucional | GitHub |
|--------|----------------------|--------|
| Rosales Navarro Jeferson José | jrosales80649@ufide.ac.cr |        |
| Melissa Rodríguez Espinoza | mrodriguez00720@ufide.ac.cr |        |
| Kimberly Bolaños Marín | kbolanos80322@ufide.ac.cr |        |
| Emanuel Chaves Vindas | echaves40339@ufide.ac.cr | @Thekidema |

---

## Descripción

PharmaCR es una aplicación web diseñada para centralizar y automatizar las operaciones de una cadena de farmacias en Costa Rica. Permite gestionar el inventario de productos, registrar ventas, administrar reservas, controlar el acceso según el rol del usuario y mejorar la experiencia tanto del personal farmacéutico como de los clientes.

**Roles del sistema:**
- **Administrador:** gestión completa de sucursales, usuarios, inventario y reportes.
- **Farmacéutico:** atención de ventas, consulta de inventario y procesamiento de reservas.
- **Cliente:** consulta de productos, realización y seguimiento de reservas.

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

### 1. Clonar el repositorio

```bash
git clone https://github.com/Thekidema/PharmaCR.git
```

### 2. Configurar la base de datos

Crear la base de datos en MySQL:

```sql
CREATE DATABASE pharmacr;
```

Editar el archivo `src/main/resources/application.properties` con las credenciales correspondientes:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/pharmacr
spring.datasource.username=<tu_usuario>
spring.datasource.password=<tu_contraseña>
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```

### 3. Compilar y ejecutar

```bash
mvn clean install
mvn spring-boot:run
```

### 4. Acceder a la aplicación

Abrir el navegador en:

```
http://localhost:8080
```

---

## Estructura de ramas

| Rama | Propósito |
|------|-----------|
| `main` | Versión estable y funcional del proyecto |
| `feature/nombre-funcionalidad` | Desarrollo de nuevas funcionalidades |

**Ejemplo de nombres de ramas:**
```
feature/gestion-inventario
feature/modulo-ventas
feature/reservas-cliente
feature/autenticacion-roles
```

> Ningún integrante debe hacer commits directamente sobre `main`. Los cambios se integran mediante pull requests revisados por al menos un compañero.

---

## Convención de commits

Los mensajes de commit deben estar escritos en español, ser descriptivos e indicar claramente qué se realizó.

**Formato sugerido:**
```
[tipo]: descripción breve de lo realizado
```

**Tipos:**
- `agrega` — para funcionalidades o archivos nuevos
- `corrige` — para corrección de errores
- `modifica` — para cambios sobre código existente
- `elimina` — para eliminación de código o archivos
- `refactoriza` — para mejoras de estructura sin cambio de comportamiento
- `docs` — para cambios en documentación

**Ejemplos:**
```
agrega: módulo de registro de ventas con validación de stock
corrige: error en cálculo de total al aplicar descuentos
modifica: diseño del formulario de reservas con Bootstrap
docs: actualiza README con instrucciones de instalación
```

---

## Estado del proyecto

| Avance | Estado |
|--------|--------|
| Avance 1 | En progreso |
| Avance 2 | Pendiente |
| Avance 3 | Pendiente |
| Entrega final | Pendiente |

---

## Créditos

Proyecto académico desarrollado por estudiantes de la carrera de **Ingeniería en Sistemas** de la **Universidad Fidélitas**, como parte del curso **SC-403 Desarrollo de Aplicaciones Web y Patrones**.

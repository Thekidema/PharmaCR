
drop database if exists pharmacr;

drop user if exists usuario_pharmacr;
drop user if exists usuario_reportes_pharmacr;


create database pharmacr
  default character set utf8mb4
  default collate utf8mb4_unicode_ci;


create user 'usuario_pharmacr'@'%' identified by 'PharmaCR_Clave.';
create user 'usuario_reportes_pharmacr'@'%' identified by 'PharmaCR_Reportes.';


grant select, insert, update, delete on pharmacr.* to 'usuario_pharmacr'@'%';
grant select on pharmacr.* to 'usuario_reportes_pharmacr'@'%';
flush privileges;

use pharmacr;


create table rol (
  id_rol        int not null auto_increment,
  nombre        varchar(30) not null unique,
  fecha_creacion     timestamp default current_timestamp,
  fecha_modificacion timestamp default current_timestamp on update current_timestamp,
  primary key (id_rol))
  engine = InnoDB;

-- Tabla de usuarios del sistema
create table usuario (
  id_usuario    int not null auto_increment,
  username      varchar(30) not null unique,
  password      varchar(512) not null,
  nombre        varchar(50) not null,
  apellidos     varchar(50) not null,
  correo        varchar(75) not null unique,
  telefono      varchar(25) null,
  ruta_imagen   varchar(1024) null,
  activo        boolean not null default true,
  fecha_creacion     timestamp default current_timestamp,
  fecha_modificacion timestamp default current_timestamp on update current_timestamp,
  primary key (id_usuario),
  check (correo regexp '^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$'),
  index ndx_username (username),
  index ndx_correo (correo))
  engine = InnoDB;

-- Tabla de relación usuario-rol (un usuario puede tener varios roles)
create table usuario_rol (
  id_usuario    int not null,
  id_rol        int not null,
  fecha_creacion     timestamp default current_timestamp,
  fecha_modificacion timestamp default current_timestamp on update current_timestamp,
  primary key (id_usuario, id_rol),
  foreign key fk_usuarioRol_usuario (id_usuario) references usuario(id_usuario),
  foreign key fk_usuarioRol_rol (id_rol) references rol(id_rol))
  engine = InnoDB;

-- Tabla de rutas y permisos de acceso
create table ruta (
  id_ruta       int auto_increment not null,
  ruta          varchar(255) not null,
  id_rol        int null,
  requiere_rol  boolean not null default true,
  fecha_creacion     timestamp default current_timestamp,
  fecha_modificacion timestamp default current_timestamp on update current_timestamp,
  check (id_rol is not null or requiere_rol = false),
  primary key (id_ruta),
  foreign key fk_ruta_rol (id_rol) references rol(id_rol))
  engine = InnoDB;

-- Tabla de categorías de medicamentos
create table categoria_medicamento (
  id_categoria  int not null auto_increment,
  nombre        varchar(50) not null unique,
  descripcion   varchar(200) null,
  activo        boolean not null default true,
  fecha_creacion     timestamp default current_timestamp,
  fecha_modificacion timestamp default current_timestamp on update current_timestamp,
  primary key (id_categoria),
  index ndx_nombre (nombre))
  engine = InnoDB;

-- Tabla de medicamentos (catálogo principal)
create table medicamento (
  id_medicamento  int not null auto_increment,
  id_categoria    int not null,
  codigo          varchar(20) not null unique,
  nombre          varchar(100) not null,
  presentacion    varchar(50) not null,
  precio          decimal(12,2) not null check (precio >= 0),
  stock_actual    int unsigned not null default 0 check (stock_actual >= 0),
  stock_minimo    int unsigned not null default 5 check (stock_minimo >= 0),
  activo          boolean not null default true,
  fecha_creacion     timestamp default current_timestamp,
  fecha_modificacion timestamp default current_timestamp on update current_timestamp,
  primary key (id_medicamento),
  unique (codigo),
  index ndx_codigo (codigo),
  index ndx_nombre (nombre),
  foreign key fk_medicamento_categoria (id_categoria) references categoria_medicamento(id_categoria))
  engine = InnoDB;

-- Tabla de proveedores
create table proveedor (
  id_proveedor      int not null auto_increment,
  nombre_comercial  varchar(100) not null unique,
  contacto          varchar(100) null,
  telefono          varchar(25) null,
  correo            varchar(75) null,
  activo            boolean not null default true,
  fecha_creacion     timestamp default current_timestamp,
  fecha_modificacion timestamp default current_timestamp on update current_timestamp,
  primary key (id_proveedor),
  index ndx_nombre_comercial (nombre_comercial))
  engine = InnoDB;

-- Tabla de ventas (encabezado de la transacción)
create table venta (
  id_venta      int not null auto_increment,
  id_usuario    int not null,
  fecha         timestamp default current_timestamp,
  total         decimal(12,2) not null check (total >= 0),
  estado        enum('Completada', 'Anulada') not null default 'Completada',
  fecha_creacion     timestamp default current_timestamp,
  fecha_modificacion timestamp default current_timestamp on update current_timestamp,
  primary key (id_venta),
  index ndx_id_usuario (id_usuario),
  index ndx_fecha (fecha),
  foreign key fk_venta_usuario (id_usuario) references usuario(id_usuario))
  engine = InnoDB;

-- Tabla de detalle de ventas (líneas de cada venta)
create table detalle_venta (
  id_detalle        int not null auto_increment,
  id_venta          int not null,
  id_medicamento    int not null,
  cantidad          int unsigned not null check (cantidad > 0),
  precio_unitario   decimal(12,2) not null check (precio_unitario >= 0),
  subtotal          decimal(12,2) not null check (subtotal >= 0),
  fecha_creacion     timestamp default current_timestamp,
  fecha_modificacion timestamp default current_timestamp on update current_timestamp,
  primary key (id_detalle),
  unique (id_venta, id_medicamento),
  index ndx_venta (id_venta),
  index ndx_medicamento (id_medicamento),
  foreign key fk_detalle_venta (id_venta) references venta(id_venta),
  foreign key fk_detalle_medicamento (id_medicamento) references medicamento(id_medicamento))
  engine = InnoDB;

-- Tabla de entradas de inventario (compras a proveedores)
create table entrada_inventario (
  id_entrada        int not null auto_increment,
  id_proveedor      int not null,
  id_medicamento    int not null,
  id_usuario        int not null,
  lote              varchar(50) not null,
  fecha_vencimiento date not null,
  cantidad          int unsigned not null check (cantidad > 0),
  observaciones     varchar(300) null,
  fecha_creacion     timestamp default current_timestamp,
  fecha_modificacion timestamp default current_timestamp on update current_timestamp,
  primary key (id_entrada),
  index ndx_medicamento (id_medicamento),
  index ndx_proveedor (id_proveedor),
  foreign key fk_entrada_proveedor (id_proveedor) references proveedor(id_proveedor),
  foreign key fk_entrada_medicamento (id_medicamento) references medicamento(id_medicamento),
  foreign key fk_entrada_usuario (id_usuario) references usuario(id_usuario))
  engine = InnoDB;

-- Tabla de salidas de inventario (ajustes, devoluciones, pérdidas)
create table salida_inventario (
  id_salida         int not null auto_increment,
  id_medicamento    int not null,
  id_usuario        int not null,
  tipo              enum('Ajuste', 'Devolucion', 'Perdida') not null,
  cantidad          int unsigned not null check (cantidad > 0),
  motivo            varchar(300) not null,
  fecha_creacion     timestamp default current_timestamp,
  fecha_modificacion timestamp default current_timestamp on update current_timestamp,
  primary key (id_salida),
  index ndx_medicamento (id_medicamento),
  foreign key fk_salida_medicamento (id_medicamento) references medicamento(id_medicamento),
  foreign key fk_salida_usuario (id_usuario) references usuario(id_usuario))
  engine = InnoDB;

-- Tabla de historial de movimientos de inventario (auditoría completa)
create table inventario_movimiento (
  id_movimiento     int not null auto_increment,
  id_medicamento    int not null,
  id_usuario        int not null,
  tipo_movimiento   enum('Entrada', 'Salida', 'Venta', 'Ajuste') not null,
  cantidad          int unsigned not null check (cantidad > 0),
  stock_resultante  int unsigned not null,
  motivo            varchar(300) null,
  id_referencia     int null,
  fecha_creacion     timestamp default current_timestamp,
  fecha_modificacion timestamp default current_timestamp on update current_timestamp,
  primary key (id_movimiento),
  index ndx_medicamento (id_medicamento),
  index ndx_usuario (id_usuario),
  index ndx_fecha (fecha_creacion),
  foreign key fk_movimiento_medicamento (id_medicamento) references medicamento(id_medicamento),
  foreign key fk_movimiento_usuario (id_usuario) references usuario(id_usuario))
  engine = InnoDB;

-- Tabla de alertas de stock mínimo
create table alerta (
  id_alerta         int not null auto_increment,
  id_medicamento    int not null,
  mensaje           varchar(255) not null,
  activo            boolean not null default true,
  fecha_creacion     timestamp default current_timestamp,
  fecha_modificacion timestamp default current_timestamp on update current_timestamp,
  primary key (id_alerta),
  index ndx_medicamento (id_medicamento),
  index ndx_activo (activo),
  foreign key fk_alerta_medicamento (id_medicamento) references medicamento(id_medicamento))
  engine = InnoDB;

-- Tabla de constantes de la aplicación
create table constante (
  id_constante  int auto_increment not null,
  atributo      varchar(50) not null unique,
  valor         varchar(200) not null,
  fecha_creacion     timestamp default current_timestamp,
  fecha_modificacion timestamp default current_timestamp on update current_timestamp,
  primary key (id_constante))
  engine = InnoDB;

-- ======================================================
-- SECCIÓN DE INSERCIÓN DE DATOS
-- ======================================================

-- Roles del sistema
insert into rol (nombre) values
  ('ADMIN'),
  ('FARMACEUTICO'),
  ('ENCARGADO_INVENTARIO');

-- Usuarios del sistema (contraseñas: Admin@1234 / Farma@1234 / Invent@1234)
-- Contraseñas cifradas con BCrypt
insert into usuario (username, password, nombre, apellidos, correo, telefono, activo) values
  ('admin',      '$2a$10$P1.w58XvnaYQUQgZUCk4aO/RTRl8EValluCqB3S2VMLTbRt.tlre.', 'Carlos',   'Mora Vargas',     'admin@pharmacr.cr',      '2222-0001', true),
  ('farma01',    '$2a$10$GkEj.ZzmQa/aEfDmtLIh3udIH5fMphx/35d0EYeqZL5uzgCJ0lQRi', 'Lucía',    'Rojas Jiménez',   'farma01@pharmacr.cr',    '2222-0002', true),
  ('inv01',      '$2a$10$koGR7eS22Pv5KdaVJKDcge04ZB53iMiw76.UjHPY.XyVYlYqXnPbO', 'Roberto',  'Solano Ulate',    'inv01@pharmacr.cr',      '2222-0003', true);

-- Asignación de roles
insert into usuario_rol (id_usuario, id_rol) values
  (1, 1), (1, 2), (1, 3),   -- admin tiene todos los roles
  (2, 2),                    -- farma01 solo FARMACEUTICO
  (3, 3);                    -- inv01 solo ENCARGADO_INVENTARIO

-- Rutas protegidas por rol (ADMIN)
insert into ruta (ruta, id_rol) values
  ('/usuario/**',               1),
  ('/rol/**',                   1),
  ('/usuario_rol/**',           1),
  ('/ruta/**',                  1),
  ('/constante/**',             1),
  ('/reportes/ventas',          1),
  ('/reportes/inventario',      1),
  ('/reportes/movimientos',     1),
  ('/dashboard',                1);

-- Rutas protegidas por rol (FARMACEUTICO)
insert into ruta (ruta, id_rol) values
  ('/venta/nueva',              2),
  ('/venta/guardar',            2),
  ('/venta/listado',            2),
  ('/medicamento/disponibilidad', 2),
  ('/medicamento/buscar',       2),
  ('/venta/comprobante/**',     2);

-- Rutas protegidas por rol (ENCARGADO_INVENTARIO)
insert into ruta (ruta, id_rol) values
  ('/medicamento/nuevo',        3),
  ('/medicamento/guardar',      3),
  ('/medicamento/modificar/**', 3),
  ('/medicamento/desactivar/**',3),
  ('/proveedor/**',             3),
  ('/inventario/entrada/**',    3),
  ('/inventario/salida/**',     3),
  ('/alerta/**',                3);

-- Rutas públicas (sin rol requerido)
insert into ruta (ruta, requiere_rol) values
  ('/',            false),
  ('/index',       false),
  ('/login',       false),
  ('/errores/**',  false),
  ('/403',         false),
  ('/css/**',      false),
  ('/js/**',       false),
  ('/img/**',      false),
  ('/webjars/**',  false);

-- Categorías de medicamentos
insert into categoria_medicamento (nombre, descripcion, activo) values
  ('Analgésicos',       'Medicamentos para el alivio del dolor',                      true),
  ('Antibióticos',      'Medicamentos para combatir infecciones bacterianas',          true),
  ('Antiinflamatorios', 'Medicamentos para reducir inflamación',                       true),
  ('Antihistamínicos',  'Medicamentos para tratar alergias',                           true),
  ('Vitaminas',         'Suplementos vitamínicos y minerales',                         true),
  ('Dermatológicos',    'Medicamentos de uso tópico para la piel',                     true),
  ('Cardiovasculares',  'Medicamentos para el sistema cardiovascular',                 false);

-- Proveedores
insert into proveedor (nombre_comercial, contacto, telefono, correo, activo) values
  ('Distribuidora CEFA',      'Ana Quirós',      '2256-4400', 'ventas@cefa.cr',         true),
  ('Farmacias Buen Precio',   'Mario Herrera',   '2283-1100', 'compras@buenprecio.cr',  true),
  ('MedGroup Costa Rica',     'Sofía Alfaro',    '2290-7700', 'pedidos@medgroup.cr',    true),
  ('BioFarma S.A.',           'Diego Solís',     '2222-9900', 'info@biofarma.cr',       true);

-- Medicamentos (con stock inicial)
insert into medicamento (id_categoria, codigo, nombre, presentacion, precio, stock_actual, stock_minimo, activo) values
  (1, 'MED-001', 'Acetaminofén 500mg',        'Tabletas x 100',   3500.00,  80, 20, true),
  (1, 'MED-002', 'Ibuprofeno 400mg',           'Tabletas x 50',    4200.00,  60, 15, true),
  (1, 'MED-003', 'Naproxeno 250mg',            'Tabletas x 30',    5800.00,  40, 10, true),
  (2, 'MED-004', 'Amoxicilina 500mg',          'Cápsulas x 21',    8500.00,  35, 10, true),
  (2, 'MED-005', 'Azitromicina 500mg',         'Tabletas x 3',    12000.00,  25,  8, true),
  (2, 'MED-006', 'Ciprofloxacino 500mg',       'Tabletas x 10',    9800.00,  30, 10, true),
  (3, 'MED-007', 'Diclofenaco 50mg',           'Tabletas x 20',    4500.00,  50, 15, true),
  (3, 'MED-008', 'Dexametasona 0.5mg',         'Tabletas x 30',    6200.00,  20,  8, true),
  (4, 'MED-009', 'Loratadina 10mg',            'Tabletas x 30',    5500.00,  45, 12, true),
  (4, 'MED-010', 'Cetirizina 10mg',            'Tabletas x 20',    4800.00,  38, 12, true),
  (5, 'MED-011', 'Vitamina C 1000mg',          'Tabletas x 60',    6900.00,  55, 15, true),
  (5, 'MED-012', 'Complejo B',                 'Tabletas x 100',   7500.00,  42, 15, true),
  (6, 'MED-013', 'Hidrocortisona crema 1%',    'Tubo 30g',         5200.00,  18,  5, true),
  (6, 'MED-014', 'Clotrimazol crema 1%',       'Tubo 20g',         4600.00,  22,  5, true),
  (1, 'MED-015', 'Tramadol 50mg',              'Cápsulas x 10',   15000.00,   3,  5, true);  -- stock bajo para generar alerta

-- Entradas de inventario (historial inicial de compras)
insert into entrada_inventario (id_proveedor, id_medicamento, id_usuario, lote, fecha_vencimiento, cantidad, observaciones) values
  (1, 1, 3, 'LOTE-2025-001', '2027-03-15', 100, 'Pedido mensual'),
  (1, 2, 3, 'LOTE-2025-002', '2027-01-20', 80,  'Pedido mensual'),
  (2, 4, 3, 'LOTE-2025-003', '2026-11-30', 50,  'Pedido quincenal antibióticos'),
  (2, 5, 3, 'LOTE-2025-004', '2026-10-15', 30,  'Pedido quincenal antibióticos'),
  (3, 9, 3, 'LOTE-2025-005', '2027-06-01', 60,  null),
  (3, 11,3, 'LOTE-2025-006', '2028-01-01', 70,  null),
  (4, 13,3, 'LOTE-2025-007', '2026-09-30', 25,  'Próximos a vencer — priorizar salida');

-- Ventas de ejemplo
insert into venta (id_usuario, fecha, total, estado) values
  (2, '2026-06-10 09:15:00', 17500.00, 'Completada'),
  (2, '2026-06-12 11:30:00', 25700.00, 'Completada'),
  (2, '2026-06-20 14:00:00', 12000.00, 'Completada'),
  (1, '2026-07-01 08:45:00',  9300.00, 'Completada'),
  (2, '2026-07-03 16:20:00', 22400.00, 'Completada');

-- Detalle de ventas
insert into detalle_venta (id_venta, id_medicamento, cantidad, precio_unitario, subtotal) values
  (1, 1, 2, 3500.00,  7000.00),
  (1, 2, 1, 4200.00,  4200.00),
  (1, 9, 1, 5500.00,  5500.00),
  (2, 4, 2, 8500.00, 17000.00),
  (2, 7, 1, 4500.00,  4500.00),
  (2, 11,1, 6900.00,  6900.00),   -- nota: sobrepasa el total de la venta intencionalmente para ejemplo
  (3, 5, 1,12000.00, 12000.00),
  (4, 10,1, 4800.00,  4800.00),
  (4, 3, 1, 5800.00,  5800.00),   -- subtotal no cierra con total de venta 2 (datos de ejemplo)
  (5, 1, 3, 3500.00, 10500.00),
  (5, 9, 1, 5500.00,  5500.00),
  (5, 14,1, 4600.00,  4600.00);

-- Salidas de inventario (ajustes)
insert into salida_inventario (id_medicamento, id_usuario, tipo, cantidad, motivo) values
  (7, 3, 'Perdida',    3, 'Caída del producto durante almacenamiento'),
  (15,3, 'Devolucion', 2, 'Devolución por error en pedido'),
  (8, 3, 'Ajuste',     2, 'Ajuste por diferencia en conteo físico');

-- Historial de movimientos (registro de auditoría)
insert into inventario_movimiento (id_medicamento, id_usuario, tipo_movimiento, cantidad, stock_resultante, motivo, id_referencia) values
  (1, 3, 'Entrada', 100, 100, 'Compra a CEFA',              1),
  (2, 3, 'Entrada',  80,  80, 'Compra a CEFA',              2),
  (4, 3, 'Entrada',  50,  50, 'Compra a Buen Precio',       3),
  (5, 3, 'Entrada',  30,  30, 'Compra a Buen Precio',       4),
  (1, 2, 'Venta',     2,  80, null,                         1),
  (2, 2, 'Venta',     1,  60, null,                         1),
  (9, 2, 'Venta',     1,  45, null,                         1),
  (4, 2, 'Venta',     2,  35, null,                         2),
  (5, 2, 'Venta',     1,  25, null,                         3),
  (7, 3, 'Salida',    3,  50, 'Pérdida por caída',          1),
  (15,3, 'Salida',    2,   3, 'Devolución por error',       2),
  (8, 3, 'Ajuste',    2,  20, 'Ajuste por conteo físico',   3);

-- Alertas de stock mínimo (generadas automáticamente por datos de ejemplo)
insert into alerta (id_medicamento, mensaje, activo) values
  (15, 'Tramadol 50mg está por debajo del stock mínimo (stock: 3, mínimo: 5)', true);

-- Constantes de la aplicación
insert into constante (atributo, valor) values
  ('app.nombre',              'PharmaCR'),
  ('app.version',             '1.0.0'),
  ('dominio',                 'localhost'),
  ('dias.alerta.vencimiento', '30'),
  ('moneda',                  'CRC'),
  ('smtp.host',               'smtp.gmail.com'),
  ('smtp.puerto',             '587');

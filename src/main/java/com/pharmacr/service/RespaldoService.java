package com.pharmacr.service;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

 //HU-19 arma el respaldo de la base de datos 

@Service
public class RespaldoService {

    private final DataSource dataSource;

    public RespaldoService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Transactional(readOnly = true)
    public String getNombreBaseDatos() {
        try (Connection conexion = dataSource.getConnection()) {
            return conexion.getCatalog();
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo conectar a la base de datos: " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public String generarRespaldo() {
        var script = new StringBuilder();

        try (Connection conexion = dataSource.getConnection()) {
            var esquema = conexion.getCatalog();

            script.append("-- Respaldo de la base de datos ").append(esquema).append('\n')
                    .append("-- Generado por PharmaCR el ").append(LocalDateTime.now()).append('\n')
                    .append("-- Restaurar con: mysql -u <usuario> -p ").append(esquema)
                    .append(" < este_archivo.sql\n\n")
                    .append("set foreign_key_checks = 0;\n\n");

            for (String tabla : listarTablas(conexion, esquema)) {
                script.append(exportarTabla(conexion, tabla));
            }

            script.append("set foreign_key_checks = 1;\n");

        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo generar el respaldo: " + e.getMessage());
        }

        return script.toString();
    }

    private List<String> listarTablas(Connection conexion, String esquema) throws SQLException {
        var tablas = new ArrayList<String>();
        try (ResultSet rs = conexion.getMetaData().getTables(esquema, null, "%", new String[]{"TABLE"})) {
            while (rs.next()) {
                tablas.add(rs.getString("TABLE_NAME"));
            }
        }
        return tablas;
    }

    private String exportarTabla(Connection conexion, String tabla) throws SQLException {
        var bloque = new StringBuilder("-- Datos de la tabla ").append(tabla).append('\n');
        var consulta = "select * from `" + tabla + "`";

        try (var sentencia = conexion.createStatement();
                ResultSet rs = sentencia.executeQuery(consulta)) {

            var meta = rs.getMetaData();
            int columnas = meta.getColumnCount();
            boolean hayDatos = false;

            while (rs.next()) {
                hayDatos = true;
                bloque.append("insert into `").append(tabla).append("` values (");
                for (int i = 1; i <= columnas; i++) {
                    if (i > 1) {
                        bloque.append(", ");
                    }
                    bloque.append(comoLiteral(rs.getObject(i)));
                }
                bloque.append(");\n");
            }

            if (!hayDatos) {
                bloque.append("-- (sin registros)\n");
            }
        }
        return bloque.append('\n').toString();
    }
    private String comoLiteral(Object valor) {
        if (valor == null) {
            return "null";
        }
        if (valor instanceof Number || valor instanceof Boolean) {
            return valor.toString();
        }
        return "'" + valor.toString().replace("\\", "\\\\").replace("'", "''") + "'";
    }
}

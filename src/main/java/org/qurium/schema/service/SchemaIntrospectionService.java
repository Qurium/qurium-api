/* Qurium - 2026 */
package org.qurium.schema.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.qurium.common.exception.QuriumException;
import org.qurium.common.exception.QuriumExceptionCode;

@ApplicationScoped
@RequiredArgsConstructor
public class SchemaIntrospectionService {

    private final ObjectMapper objectMapper;

    public String introspect(Connection connection) {
        try {
            DatabaseMetaData metaData = connection.getMetaData();
            List<Map<String, Object>> tables = new ArrayList<>();

            try (ResultSet rs = metaData.getTables(null, null, "%", new String[] {"TABLE"})) {
                while (rs.next()) {
                    String tableName = rs.getString("TABLE_NAME");
                    Map<String, Object> table = new LinkedHashMap<>();
                    table.put("table", tableName);
                    table.put("columns", getColumns(metaData, tableName));
                    tables.add(table);
                }
            }

            return objectMapper.writeValueAsString(tables);

        } catch (SQLException | JsonProcessingException e) {
            throw new QuriumException(QuriumExceptionCode.SCHEMA_IMPORT_FAILED);
        }
    }

    private List<Map<String, String>> getColumns(DatabaseMetaData metaData, String tableName)
            throws SQLException {
        List<Map<String, String>> columns = new ArrayList<>();

        try (ResultSet rs = metaData.getColumns(null, null, tableName, "%")) {
            while (rs.next()) {
                Map<String, String> column = new LinkedHashMap<>();
                column.put("name", rs.getString("COLUMN_NAME"));
                column.put("type", rs.getString("TYPE_NAME"));
                column.put("nullable", rs.getString("IS_NULLABLE"));
                columns.add(column);
            }
        }

        return columns;
    }
}

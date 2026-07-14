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
                    table.put("constraints", getConstraints(metaData, tableName));
                    table.put("indexes", getIndexes(metaData, tableName));
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

    private List<Map<String, Object>> getConstraints(DatabaseMetaData metaData, String tableName)
            throws SQLException {
        Map<String, Map<String, Object>> byName = new LinkedHashMap<>();

        try (ResultSet rs = metaData.getImportedKeys(null, null, tableName)) {
            while (rs.next()) {
                String fkName = rs.getString("FK_NAME");
                if (fkName == null) continue;

                String fkCol = rs.getString("FKCOLUMN_NAME");
                String pkCol = rs.getString("PKCOLUMN_NAME");

                if (!byName.containsKey(fkName)) {
                    Map<String, Object> c = new LinkedHashMap<>();
                    c.put("name", fkName);
                    c.put("type", "FOREIGN_KEY");
                    c.put("columns", fkCol);
                    c.put("referencesTable", rs.getString("PKTABLE_NAME"));
                    c.put("referencesColumns", pkCol);
                    byName.put(fkName, c);
                } else {
                    Map<String, Object> c = byName.get(fkName);
                    c.put("columns", c.get("columns") + ", " + fkCol);
                    c.put("referencesColumns", c.get("referencesColumns") + ", " + pkCol);
                }
            }
        }

        return new ArrayList<>(byName.values());
    }

    private List<Map<String, Object>> getIndexes(DatabaseMetaData metaData, String tableName)
            throws SQLException {
        Map<String, Map<String, Object>> byName = new LinkedHashMap<>();

        try (ResultSet rs = metaData.getIndexInfo(null, null, tableName, false, false)) {
            while (rs.next()) {
                String indexName = rs.getString("INDEX_NAME");
                if (indexName == null) continue; // skip table statistics row

                String colName = rs.getString("COLUMN_NAME");

                if (!byName.containsKey(indexName)) {
                    Map<String, Object> index = new LinkedHashMap<>();
                    index.put("name", indexName);
                    index.put("columns", colName);
                    index.put("unique", !rs.getBoolean("NON_UNIQUE"));
                    byName.put(indexName, index);
                } else {
                    Map<String, Object> index = byName.get(indexName);
                    index.put("columns", index.get("columns") + ", " + colName);
                }
            }
        }

        return new ArrayList<>(byName.values());
    }
}

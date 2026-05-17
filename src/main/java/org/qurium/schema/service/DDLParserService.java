/* Qurium - 2026 */
package org.qurium.schema.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.qurium.common.exception.QuriumException;
import org.qurium.common.exception.QuriumExceptionCode;

@ApplicationScoped
@RequiredArgsConstructor
public class DDLParserService {

    private static final Pattern CREATE_TABLE_PATTERN =
            Pattern.compile(
                    "CREATE\\s+TABLE\\s+(?:IF\\s+NOT\\s+EXISTS\\s+)?(?:\\w+\\.)?\"?(\\w+)\"?\\s*\\((.+?)\\);",
                    Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    private static final Pattern COLUMN_PATTERN =
            Pattern.compile(
                    "^\\s*\"?(\\w+)\"?\\s+(\\w+(?:\\s*\\([^)]*\\))?)", Pattern.CASE_INSENSITIVE);

    private final ObjectMapper objectMapper;

    public String parse(String ddl) {
        List<Map<String, Object>> tables = new ArrayList<>();
        Matcher tableMatcher = CREATE_TABLE_PATTERN.matcher(ddl);

        while (tableMatcher.find()) {
            String tableName = tableMatcher.group(1);
            String columnsBlock = tableMatcher.group(2);

            Map<String, Object> table = new LinkedHashMap<>();
            table.put("table", tableName);
            table.put("columns", parseColumns(columnsBlock));
            tables.add(table);
        }

        if (tables.isEmpty()) {
            throw new QuriumException(QuriumExceptionCode.SCHEMA_PARSE_FAILED);
        }

        try {
            return objectMapper.writeValueAsString(tables);
        } catch (JsonProcessingException e) {
            throw new QuriumException(QuriumExceptionCode.SCHEMA_PARSE_FAILED);
        }
    }

    private List<Map<String, String>> parseColumns(String columnsBlock) {
        List<Map<String, String>> columns = new ArrayList<>();

        for (String part : columnsBlock.split(",")) {
            String trimmed = part.trim();

            if (isConstraint(trimmed)) {
                continue;
            }

            Matcher colMatcher = COLUMN_PATTERN.matcher(trimmed);
            if (colMatcher.find()) {
                Map<String, String> column = new LinkedHashMap<>();
                column.put("name", colMatcher.group(1));
                column.put("type", colMatcher.group(2).toUpperCase());
                column.put("nullable", trimmed.toUpperCase().contains("NOT NULL") ? "NO" : "YES");
                columns.add(column);
            }
        }

        return columns;
    }

    private boolean isConstraint(String line) {
        String upper = line.toUpperCase().trim();
        return upper.startsWith("PRIMARY KEY")
                || upper.startsWith("FOREIGN KEY")
                || upper.startsWith("UNIQUE")
                || upper.startsWith("CHECK")
                || upper.startsWith("CONSTRAINT");
    }
}

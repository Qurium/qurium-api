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

    private static final Pattern ALTER_ADD_COLUMN_PATTERN =
            Pattern.compile(
                    "ALTER\\s+TABLE\\s+(?:\\w+\\.)?\"?(\\w+)\"?\\s+ADD\\s+(?:COLUMN\\s+)?\"?(\\w+)\"?\\s+(\\w+(?:\\s*\\([^)]*\\))?)(.*?);",
                    Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    private static final Pattern ALTER_ADD_CONSTRAINT_PATTERN =
            Pattern.compile(
                    "ALTER\\s+TABLE\\s+(?:\\w+\\.)?\"?(\\w+)\"?\\s+ADD\\s+CONSTRAINT\\s+\"?(\\w+)\"?\\s+(FOREIGN\\s+KEY|UNIQUE|CHECK)\\s*\\((.+?)\\)(.*?);",
                    Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    private static final Pattern CREATE_INDEX_PATTERN =
            Pattern.compile(
                    "CREATE\\s+(?:UNIQUE\\s+)?INDEX\\s+(?:IF\\s+NOT\\s+EXISTS\\s+)?\"?(\\w+)\"?\\s+ON\\s+(?:\\w+\\.)?\"?(\\w+)\"?\\s*\\((.+?)\\);",
                    Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    private static final Pattern INSERT_INTO_PATTERN =
            Pattern.compile(
                    "INSERT\\s+INTO\\s+(?:\\w+\\.)?\"?(\\w+)\"?\\s*\\((.+?)\\)\\s*VALUES\\s*(.+?);",
                    Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    private static final Pattern VALUES_GROUP_PATTERN =
            Pattern.compile("\\((.+?)\\)", Pattern.DOTALL);

    private static final Pattern COLUMN_PATTERN =
            Pattern.compile(
                    "^\\s*\"?(\\w+)\"?\\s+(\\w+(?:\\s*\\([^)]*\\))?)", Pattern.CASE_INSENSITIVE);

    private final ObjectMapper objectMapper;

    public String parse(String ddl) {
        Map<String, Map<String, Object>> tableMap = new LinkedHashMap<>();

        parseCreateTables(ddl, tableMap);
        parseAlterAddColumns(ddl, tableMap);
        parseAlterAddConstraints(ddl, tableMap);
        parseCreateIndexes(ddl, tableMap);
        parseInsertInto(ddl, tableMap);

        if (tableMap.isEmpty()) {
            throw new QuriumException(QuriumExceptionCode.SCHEMA_PARSE_FAILED);
        }

        try {
            return objectMapper.writeValueAsString(new ArrayList<>(tableMap.values()));
        } catch (JsonProcessingException e) {
            throw new QuriumException(QuriumExceptionCode.SCHEMA_PARSE_FAILED);
        }
    }

    private void parseCreateTables(String ddl, Map<String, Map<String, Object>> tableMap) {
        Matcher matcher = CREATE_TABLE_PATTERN.matcher(ddl);

        while (matcher.find()) {
            String tableName = matcher.group(1).toLowerCase();
            String columnsBlock = matcher.group(2);

            Map<String, Object> table = new LinkedHashMap<>();
            table.put("table", tableName);
            table.put("columns", parseColumns(columnsBlock));
            table.put("constraints", new ArrayList<Map<String, Object>>());
            table.put("indexes", new ArrayList<Map<String, Object>>());
            tableMap.put(tableName, table);
        }
    }

    private void parseAlterAddColumns(String ddl, Map<String, Map<String, Object>> tableMap) {
        Matcher matcher = ALTER_ADD_COLUMN_PATTERN.matcher(ddl);

        while (matcher.find()) {
            String tableName = matcher.group(1).toLowerCase();
            String columnName = matcher.group(2);
            String columnType = matcher.group(3);
            String rest = matcher.group(4);

            Map<String, Object> table = tableMap.get(tableName);
            if (table == null) {
                continue;
            }

            @SuppressWarnings("unchecked")
            List<Map<String, String>> columns = (List<Map<String, String>>) table.get("columns");

            Map<String, String> column = new LinkedHashMap<>();
            column.put("name", columnName);
            column.put("type", columnType.toUpperCase());
            column.put("nullable", rest.toUpperCase().contains("NOT NULL") ? "NO" : "YES");
            columns.add(column);
        }
    }

    private void parseAlterAddConstraints(String ddl, Map<String, Map<String, Object>> tableMap) {
        Matcher matcher = ALTER_ADD_CONSTRAINT_PATTERN.matcher(ddl);

        while (matcher.find()) {
            String tableName = matcher.group(1).toLowerCase();
            String constraintName = matcher.group(2);
            String constraintType = matcher.group(3).toUpperCase().replaceAll("\\s+", "_");
            String constraintColumns = matcher.group(4).trim();
            String rest = matcher.group(5).trim();

            Map<String, Object> table = tableMap.get(tableName);
            if (table == null) {
                continue;
            }

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> constraints =
                    (List<Map<String, Object>>) table.get("constraints");

            Map<String, Object> constraint = new LinkedHashMap<>();
            constraint.put("name", constraintName);
            constraint.put("type", constraintType);
            constraint.put("columns", constraintColumns);

            if (constraintType.equals("FOREIGN_KEY")) {
                Pattern refPattern =
                        Pattern.compile(
                                "REFERENCES\\s+\"?(\\w+)\"?\\s*\\((.+?)\\)",
                                Pattern.CASE_INSENSITIVE);
                Matcher refMatcher = refPattern.matcher(rest);
                if (refMatcher.find()) {
                    constraint.put("referencesTable", refMatcher.group(1));
                    constraint.put("referencesColumns", refMatcher.group(2).trim());
                }
            }

            constraints.add(constraint);
        }
    }

    private void parseCreateIndexes(String ddl, Map<String, Map<String, Object>> tableMap) {
        Matcher matcher = CREATE_INDEX_PATTERN.matcher(ddl);

        while (matcher.find()) {
            String indexName = matcher.group(1);
            String tableName = matcher.group(2).toLowerCase();
            String indexColumns = matcher.group(3).trim();

            Map<String, Object> table = tableMap.get(tableName);
            if (table == null) {
                continue;
            }

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> indexes = (List<Map<String, Object>>) table.get("indexes");

            Map<String, Object> index = new LinkedHashMap<>();
            index.put("name", indexName);
            index.put("columns", indexColumns);
            boolean isUnique =
                    ddl.substring(matcher.start()).toUpperCase().startsWith("CREATE UNIQUE");
            index.put("unique", isUnique);
            indexes.add(index);
        }
    }

    private void parseInsertInto(String ddl, Map<String, Map<String, Object>> tableMap) {
        Matcher matcher = INSERT_INTO_PATTERN.matcher(ddl);

        while (matcher.find()) {
            String tableName = matcher.group(1).toLowerCase();
            String columnsList = matcher.group(2).trim();
            String valuesBlock = matcher.group(3).trim();

            Map<String, Object> table = tableMap.get(tableName);
            if (table == null) {
                continue;
            }

            @SuppressWarnings("unchecked")
            List<Map<String, String>> sampleData =
                    (List<Map<String, String>>)
                            table.computeIfAbsent("sampleData", k -> new ArrayList<>());

            String[] columns = columnsList.split(",");
            for (int i = 0; i < columns.length; i++) {
                columns[i] = columns[i].trim().replaceAll("\"", "");
            }

            Matcher valuesMatcher = VALUES_GROUP_PATTERN.matcher(valuesBlock);
            while (valuesMatcher.find()) {
                String[] values = splitValues(valuesMatcher.group(1));
                Map<String, String> row = new LinkedHashMap<>();
                for (int i = 0; i < columns.length && i < values.length; i++) {
                    row.put(columns[i], values[i].trim().replaceAll("^'|'$", ""));
                }
                sampleData.add(row);
            }
        }
    }

    private String[] splitValues(String valuesStr) {
        List<String> values = new ArrayList<>();
        int depth = 0;
        boolean inQuote = false;
        StringBuilder current = new StringBuilder();

        for (char c : valuesStr.toCharArray()) {
            if (c == '\'' && depth == 0) {
                inQuote = !inQuote;
                current.append(c);
            } else if (c == '(' && !inQuote) {
                depth++;
                current.append(c);
            } else if (c == ')' && !inQuote) {
                depth--;
                current.append(c);
            } else if (c == ',' && !inQuote && depth == 0) {
                values.add(current.toString());
                current = new StringBuilder();
            } else {
                current.append(c);
            }
        }
        values.add(current.toString());

        return values.toArray(new String[0]);
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

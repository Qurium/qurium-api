/* Qurium - 2026 */
package org.qurium.nlquery.command.handler;

import static org.qurium.common.exception.QuriumExceptionCode.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.qurium.common.CommandHandler;
import org.qurium.common.exception.QuriumException;
import org.qurium.connection.DatabaseConnectionFactory;
import org.qurium.connection.domain.DatabaseConnection;
import org.qurium.nlquery.ai.AiResultSnapshot;
import org.qurium.nlquery.ai.AiSqlResponse;
import org.qurium.nlquery.ai.AiSqlService;
import org.qurium.nlquery.command.data.ExecuteNlQueryCommand;
import org.qurium.nlquery.domain.NlQueryStatus;
import org.qurium.nlquery.dto.ExecuteNlQueryResponseDTO;
import org.qurium.nlquery.repository.NlQueryRepository;
import org.qurium.schema.domain.Schema;
import org.qurium.schema.repository.SchemaRepository;
import org.qurium.uploadedfile.repository.UploadedFileRepository;

@ApplicationScoped
@RequiredArgsConstructor
public class ExecuteNlQueryHandler
        implements CommandHandler<ExecuteNlQueryCommand, ExecuteNlQueryResponseDTO> {

    private record SqlResult(String rawJson, long executionTimeMs, int rowsReturned) {}

    private final SchemaRepository schemaRepository;
    private final AiSqlService aiSqlService;
    private final NlQueryRepository nlQueryRepository;
    private final DatabaseConnectionFactory databaseConnectionFactory;
    private final UploadedFileRepository uploadedFileRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public ExecuteNlQueryResponseDTO handle(ExecuteNlQueryCommand command) {

        Schema schema =
                schemaRepository
                        .findByOwnerId(command.ownerId())
                        .orElseThrow(() -> new QuriumException(SCHEMA_NOT_FOUND));

        DatabaseConnection connection = schema.getConnection();
        boolean canExecute = connection != null;

        try {
            AiSqlResponse aiResponse =
                    aiSqlService.generateSql(schema.getSchemaJson(), command.question());

            String snapshot = null;
            Long executionTimeMs = null;
            Integer rowsReturned = null;

            if (canExecute) {
                SqlResult sqlResult = executeSql(connection, aiResponse.sql());
                executionTimeMs = sqlResult.executionTimeMs();
                rowsReturned = sqlResult.rowsReturned();
                AiResultSnapshot aiResultSnapshot =
                        aiSqlService.generateResultSnapshot(
                                command.question(), sqlResult.rawJson());
                if (aiResultSnapshot != null) {
                    snapshot = aiResultSnapshot.resultSnapshot();
                }
            }

            nlQueryRepository.store(
                    canExecute ? schema.getConnection() : null,
                    !canExecute ? schema.getUploadedFile() : null,
                    command.question(),
                    aiResponse.sql(),
                    snapshot,
                    executionTimeMs,
                    rowsReturned,
                    aiResponse.explanation(),
                    NlQueryStatus.SUCCESS,
                    null);

            return new ExecuteNlQueryResponseDTO(
                    aiResponse.sql(), aiResponse.explanation(), snapshot, canExecute);

        } catch (Exception e) {
            nlQueryRepository.store(
                    canExecute ? schema.getConnection() : null,
                    !canExecute ? schema.getUploadedFile() : null,
                    command.question(),
                    null,
                    null,
                    null,
                    null,
                    null,
                    NlQueryStatus.FAILED,
                    e.getMessage());

            throw new QuriumException(NL_QUERY_AI_FAILED);
        }
    }

    private SqlResult executeSql(DatabaseConnection connection, String sql) {
        try (Connection jdbc = databaseConnectionFactory.open(connection);
                Statement stmt = jdbc.createStatement()) {

            long start = System.currentTimeMillis();
            ResultSet rs = stmt.executeQuery(sql);
            long executionTimeMs = System.currentTimeMillis() - start;

            ResultSetMetaData meta = rs.getMetaData();
            int cols = meta.getColumnCount();
            List<Map<String, Object>> rows = new ArrayList<>();

            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                for (int i = 1; i <= cols; i++) {
                    row.put(meta.getColumnLabel(i), rs.getObject(i));
                }
                rows.add(row);
            }

            return new SqlResult(
                    objectMapper.writeValueAsString(rows), executionTimeMs, rows.size());

        } catch (QuriumException e) {
            throw e;
        } catch (Exception e) {
            throw new QuriumException(DATABASE_CONNECTION_UNREACHABLE);
        }
    }
}

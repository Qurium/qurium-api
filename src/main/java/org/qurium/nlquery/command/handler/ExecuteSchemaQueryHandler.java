/* Qurium - 2026 */
package org.qurium.nlquery.command.handler;

import static org.qurium.common.exception.QuriumExceptionCode.SCHEMA_NOT_FOUND;

import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import org.qurium.common.CommandHandler;
import org.qurium.common.exception.QuriumException;
import org.qurium.nlquery.ai.AiSqlResponse;
import org.qurium.nlquery.ai.AiSqlService;
import org.qurium.nlquery.command.data.ExecuteSchemaQueryCommand;
import org.qurium.nlquery.dto.ExecuteNlQueryResponseDTO;
import org.qurium.schema.domain.Schema;
import org.qurium.schema.repository.SchemaRepository;

@ApplicationScoped
@RequiredArgsConstructor
public class ExecuteSchemaQueryHandler
        implements CommandHandler<ExecuteSchemaQueryCommand, ExecuteNlQueryResponseDTO> {

    private final SchemaRepository schemaRepository;
    private final AiSqlService aiSqlService;

    @Override
    public ExecuteNlQueryResponseDTO handle(ExecuteSchemaQueryCommand command) {

        Schema schema =
                schemaRepository
                        .findByIdOptional(command.schemaId())
                        .orElseThrow(() -> new QuriumException(SCHEMA_NOT_FOUND));

        AiSqlResponse aiResponse =
                aiSqlService.generateSql(schema.getSchemaJson(), command.question());

        return new ExecuteNlQueryResponseDTO(
                aiResponse.sql(), aiResponse.explanation(), null, false);
    }
}

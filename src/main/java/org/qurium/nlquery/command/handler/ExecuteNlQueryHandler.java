/* Qurium - 2026 */
package org.qurium.nlquery.command.handler;

import static org.qurium.common.exception.QuriumExceptionCode.SCHEMA_NOT_FOUND;

import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import org.qurium.common.CommandHandler;
import org.qurium.common.exception.QuriumException;
import org.qurium.nlquery.ai.AiSqlResponse;
import org.qurium.nlquery.ai.AiSqlService;
import org.qurium.nlquery.command.data.ExecuteNlQueryCommand;
import org.qurium.nlquery.dto.ExecuteNlQueryResponseDTO;
import org.qurium.schema.domain.Schema;
import org.qurium.schema.repository.SchemaRepository;

@ApplicationScoped
@RequiredArgsConstructor
public class ExecuteNlQueryHandler
        implements CommandHandler<ExecuteNlQueryCommand, ExecuteNlQueryResponseDTO> {

    private final SchemaRepository schemaRepository;
    private final AiSqlService sqlService;

    @Override
    public ExecuteNlQueryResponseDTO handle(ExecuteNlQueryCommand command) {

        Schema schema =
                schemaRepository
                        .findByConnectionId(command.connectionId())
                        .orElseThrow(() -> new QuriumException(SCHEMA_NOT_FOUND));

        AiSqlResponse sqlResponse =
                sqlService.generateSql(schema.getSchemaJson(), command.question());

        return new ExecuteNlQueryResponseDTO(sqlResponse.sql(), sqlResponse.explanation());
    }
}

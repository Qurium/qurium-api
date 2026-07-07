/* Qurium - 2026 */
package org.qurium.nlquery.command.handler;

import static org.qurium.common.exception.QuriumExceptionCode.NL_QUERY_AI_FAILED;
import static org.qurium.common.exception.QuriumExceptionCode.SCHEMA_NOT_FOUND;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.qurium.common.CommandHandler;
import org.qurium.common.exception.QuriumException;
import org.qurium.nlquery.ai.AiSqlResponse;
import org.qurium.nlquery.ai.AiSqlService;
import org.qurium.nlquery.command.data.ExecuteSchemaQueryCommand;
import org.qurium.nlquery.domain.NlQueryStatus;
import org.qurium.nlquery.dto.ExecuteNlQueryResponseDTO;
import org.qurium.nlquery.repository.NlQueryRepository;
import org.qurium.schema.domain.Schema;
import org.qurium.schema.repository.SchemaRepository;

@ApplicationScoped
@RequiredArgsConstructor
public class ExecuteSchemaQueryHandler
        implements CommandHandler<ExecuteSchemaQueryCommand, ExecuteNlQueryResponseDTO> {

    private final SchemaRepository schemaRepository;
    private final AiSqlService aiSqlService;
    private final NlQueryRepository nlQueryRepository;

    @Override
    @Transactional
    public ExecuteNlQueryResponseDTO handle(ExecuteSchemaQueryCommand command) {

        Schema schema =
                schemaRepository
                        .findByIdOptional(command.schemaId())
                        .orElseThrow(() -> new QuriumException(SCHEMA_NOT_FOUND));

        try {
            AiSqlResponse aiResponse =
                    aiSqlService.generateSql(schema.getSchemaJson(), command.question());

            nlQueryRepository.store(
                    null,
                    command.question(),
                    aiResponse.sql(),
                    null,
                    aiResponse.explanation(),
                    NlQueryStatus.SUCCESS,
                    null);

            return new ExecuteNlQueryResponseDTO(
                    aiResponse.sql(), aiResponse.explanation(), null, false);

        } catch (Exception e) {
            nlQueryRepository.store(
                    null,
                    command.question(),
                    null,
                    null,
                    null,
                    NlQueryStatus.FAILED,
                    e.getMessage());

            throw new QuriumException(NL_QUERY_AI_FAILED);
        }
    }
}

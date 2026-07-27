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
import org.qurium.nlquery.command.data.ExecuteNlQueryCommand;
import org.qurium.nlquery.domain.NlQueryStatus;
import org.qurium.nlquery.dto.ExecuteNlQueryResponseDTO;
import org.qurium.nlquery.repository.NlQueryRepository;
import org.qurium.schema.domain.Schema;
import org.qurium.schema.repository.SchemaRepository;

@ApplicationScoped
@RequiredArgsConstructor
public class ExecuteNlQueryHandler
        implements CommandHandler<ExecuteNlQueryCommand, ExecuteNlQueryResponseDTO> {

    private final SchemaRepository schemaRepository;
    private final AiSqlService aiSqlService;
    private final NlQueryRepository nlQueryRepository;

    @Override
    @Transactional
    public ExecuteNlQueryResponseDTO handle(ExecuteNlQueryCommand command) {

        Schema schema =
                schemaRepository
                        .findByOwnerId(command.ownerId())
                        .orElseThrow(() -> new QuriumException(SCHEMA_NOT_FOUND));

        boolean canExecute = schema.getConnection() != null;

        try {
            AiSqlResponse aiResponse =
                    aiSqlService.generateSql(schema.getSchemaJson(), command.question());

            nlQueryRepository.store(
                    schema.getConnection(),
                    command.question(),
                    aiResponse.sql(),
                    canExecute ? aiResponse.resultSnapshot() : null,
                    aiResponse.explanation(),
                    NlQueryStatus.SUCCESS,
                    null);

            return new ExecuteNlQueryResponseDTO(
                    aiResponse.sql(),
                    aiResponse.explanation(),
                    canExecute ? aiResponse.resultSnapshot() : null,
                    canExecute);

        } catch (Exception e) {
            nlQueryRepository.store(
                    schema.getConnection(),
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

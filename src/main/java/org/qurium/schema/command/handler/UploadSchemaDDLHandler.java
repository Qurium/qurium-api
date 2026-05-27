/* Qurium - 2026 */
package org.qurium.schema.command.handler;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.qurium.common.CommandHandler;
import org.qurium.schema.command.data.UploadSchemaDDLCommand;
import org.qurium.schema.domain.SchemaSource;
import org.qurium.schema.repository.SchemaRepository;

@ApplicationScoped
@RequiredArgsConstructor
public class UploadSchemaDDLHandler implements CommandHandler<UploadSchemaDDLCommand, UUID> {

    private final SchemaRepository schemaRepository;

    @Override
    @Transactional
    public UUID handle(UploadSchemaDDLCommand command) {
        return schemaRepository.store(command.schemaJson(), SchemaSource.UPLOADED_DDL);
    }
}

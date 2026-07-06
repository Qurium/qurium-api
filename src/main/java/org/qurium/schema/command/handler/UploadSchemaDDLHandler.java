/* Qurium - 2026 */
package org.qurium.schema.command.handler;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.qurium.common.CommandHandler;
import org.qurium.schema.command.data.UploadSchemaDDLCommand;
import org.qurium.schema.repository.SchemaRepository;
import org.qurium.uploadedfile.domain.UploadedFile;
import org.qurium.uploadedfile.repository.UploadedFileRepository;

@ApplicationScoped
@RequiredArgsConstructor
public class UploadSchemaDDLHandler implements CommandHandler<UploadSchemaDDLCommand, UUID> {

    private final UploadedFileRepository uploadedFileRepository;
    private final SchemaRepository schemaRepository;

    @Override
    @Transactional
    public UUID handle(UploadSchemaDDLCommand command) {
        UploadedFile uploadedFile = uploadedFileRepository.store(command.fileName());
        schemaRepository.store(uploadedFile, command.schemaJson());
        return uploadedFile.getId();
    }
}

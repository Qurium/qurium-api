/* Qurium - 2026 */
package org.qurium.schema.command.handler;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.util.Optional;
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

        String fileName = command.fileName().trim().toLowerCase().replace(" ", "_");

        Optional<UploadedFile> optionalUploadedFile =
                uploadedFileRepository.findByFileNameAndName(fileName);

        if (optionalUploadedFile.isPresent()) {

            UUID id = optionalUploadedFile.get().getId();
            schemaRepository
                    .findByUploadedFileId(id)
                    .ifPresent(
                            s -> {
                                s.setSchemaJson(command.schemaJson());
                                s.persist();
                            });
            return optionalUploadedFile.get().getId();
        }

        UploadedFile uploadedFile =
                uploadedFileRepository.store(command.fileName(), command.name());
        schemaRepository.store(uploadedFile, command.schemaJson());
        return uploadedFile.getId();
    }
}

/* Qurium - 2026 */
package org.qurium.uploadedfile.command.handler;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.qurium.common.CommandHandler;
import org.qurium.common.exception.QuriumException;
import org.qurium.common.exception.QuriumExceptionCode;
import org.qurium.schema.domain.Schema;
import org.qurium.schema.domain.SchemaSource;
import org.qurium.schema.repository.SchemaRepository;
import org.qurium.uploadedfile.command.data.ReuploadFileCommand;
import org.qurium.uploadedfile.domain.UploadedFile;
import org.qurium.uploadedfile.repository.UploadedFileRepository;

@ApplicationScoped
@RequiredArgsConstructor
public class ReuploadFileHandler implements CommandHandler<ReuploadFileCommand, UUID> {

    private final SchemaRepository schemaRepository;
    private final UploadedFileRepository fileRepository;

    @Override
    @Transactional
    public UUID handle(ReuploadFileCommand command) {

        UploadedFile uploadedFile =
                fileRepository
                        .findByIdOptional(command.id())
                        .orElseThrow(
                                () ->
                                        new QuriumException(
                                                QuriumExceptionCode.UPLOADED_FILE_NOT_FOUND));

        Schema schema =
                schemaRepository
                        .findByUploadedFileId(command.id())
                        .orElseGet(
                                () -> {
                                    Schema s = new Schema();
                                    s.setUploadedFile(uploadedFile);
                                    s.setSource(SchemaSource.UPLOADED_DDL);
                                    return s;
                                });

        schema.setSchemaJson(command.schema());
        if (schema.getId() == null) schemaRepository.persist(schema);

        String normalizedFileName = command.fileName().trim().toLowerCase().replace(" ", "_");
        fileRepository
                .findByFileName(normalizedFileName)
                .filter(f -> !f.getId().equals(command.id()))
                .ifPresent(
                        _ -> {
                            throw new QuriumException(
                                    QuriumExceptionCode.UPLOADED_FILE_ALREADY_EXISTS);
                        });
        uploadedFile.setFileName(command.fileName());

        if (command.name() != null && !command.name().isBlank()) {
            fileRepository
                    .findByName(command.name())
                    .filter(f -> !f.getId().equals(command.id()))
                    .ifPresent(
                            _ -> {
                                throw new QuriumException(
                                        QuriumExceptionCode.UPLOADED_FILE_ALREADY_EXISTS);
                            });
            uploadedFile.setName(command.name());
        }

        return uploadedFile.getId();
    }
}

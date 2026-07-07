/* Qurium - 2026 */
package org.qurium.uploadedfile.command.handler;

import static org.qurium.common.exception.QuriumExceptionCode.UPLOADED_FILE_NOT_FOUND;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.qurium.common.CommandHandler;
import org.qurium.common.exception.QuriumException;
import org.qurium.schema.domain.Schema;
import org.qurium.schema.repository.SchemaRepository;
import org.qurium.uploadedfile.command.data.DeleteUploadedFileCommand;
import org.qurium.uploadedfile.domain.UploadedFile;
import org.qurium.uploadedfile.repository.UploadedFileRepository;

@ApplicationScoped
@RequiredArgsConstructor
public class DeleteUploadedFileHandler implements CommandHandler<DeleteUploadedFileCommand, Void> {

    private final UploadedFileRepository uploadedFileRepository;
    private final SchemaRepository schemaRepository;

    @Override
    @Transactional
    public Void handle(DeleteUploadedFileCommand command) {
        UploadedFile uploadedFile =
                uploadedFileRepository
                        .findByIdOptional(command.id())
                        .orElseThrow(() -> new QuriumException(UPLOADED_FILE_NOT_FOUND));

        schemaRepository.findByUploadedFileId(command.id()).ifPresent(Schema::delete);
        uploadedFile.delete();

        return null;
    }
}

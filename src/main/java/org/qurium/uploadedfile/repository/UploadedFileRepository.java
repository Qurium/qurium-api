/* Qurium - 2026 */
package org.qurium.uploadedfile.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.util.Optional;
import java.util.UUID;
import org.qurium.uploadedfile.domain.UploadedFile;

@ApplicationScoped
public class UploadedFileRepository implements PanacheRepositoryBase<UploadedFile, UUID> {

    @Transactional(Transactional.TxType.MANDATORY)
    public UploadedFile store(String fileName, String name) {
        UploadedFile uploadedFile = new UploadedFile();
        uploadedFile.setName(name);
        uploadedFile.setFileName(fileName);
        persist(uploadedFile);
        return uploadedFile;
    }

    public Optional<UploadedFile> findByFileNameAndName(String fileName) {

        return find("lower(replace(trim(fileName), ' ', '_')) = ?1", fileName)
                .firstResultOptional();
    }
}

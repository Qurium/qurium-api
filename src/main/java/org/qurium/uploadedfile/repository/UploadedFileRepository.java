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
    public UploadedFile store(String name) {
        UploadedFile uploadedFile = new UploadedFile();
        uploadedFile.setName(name);
        persist(uploadedFile);
        return uploadedFile;
    }

    public Optional<UploadedFile> findByFileName(String name) {

        return find("lower(replace(trim(name), ' ', '_')) = ?1", name).firstResultOptional();
    }
}

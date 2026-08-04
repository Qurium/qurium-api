/* Qurium - 2026 */
package org.qurium.nlquery.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.UUID;
import org.qurium.connection.domain.DatabaseConnection;
import org.qurium.nlquery.domain.NlQuery;
import org.qurium.nlquery.domain.NlQueryStatus;
import org.qurium.uploadedfile.domain.UploadedFile;

@ApplicationScoped
public class NlQueryRepository implements PanacheRepositoryBase<NlQuery, UUID> {

    public UUID store(
            DatabaseConnection connection,
            UploadedFile uploadedFile,
            String question,
            String generatedSql,
            String resultSnapshot,
            Long executionTimeMs,
            Integer rowsReturned,
            String explanation,
            NlQueryStatus status,
            String errorMessage) {

        NlQuery nlQuery = new NlQuery();
        nlQuery.setConnection(connection);
        nlQuery.setUploadedFile(uploadedFile);
        nlQuery.setQuestion(question);
        nlQuery.setGeneratedSql(generatedSql);
        nlQuery.setResultSnapshot(resultSnapshot);
        nlQuery.setExecutionTimeMs(executionTimeMs);
        nlQuery.setRowsReturned(rowsReturned);
        nlQuery.setExplanation(explanation);
        nlQuery.setStatus(status);
        nlQuery.setErrorMessage(errorMessage);

        persist(nlQuery);

        return nlQuery.getId();
    }

    public List<NlQuery> findAllByOwnerId(UUID ownerId, int page, int size) {
        return find(
                        "connection.id = ?1 or uploadedFile.id = ?1",
                        Sort.by("createdAt").descending(),
                        ownerId)
                .page(page, size)
                .list();
    }

    public long countByOwnerId(UUID ownerId) {
        return count("connection.id = ?1 or uploadedFile.id = ?1", ownerId);
    }
}

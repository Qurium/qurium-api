/* Qurium - 2026 */
package org.qurium.nlquery.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.UUID;
import org.qurium.connection.domain.DatabaseConnection;
import org.qurium.nlquery.domain.NlQuery;
import org.qurium.nlquery.domain.NlQueryStatus;

@ApplicationScoped
public class NlQueryRepository implements PanacheRepositoryBase<NlQuery, UUID> {

    public UUID store(
            DatabaseConnection connection,
            String question,
            String generatedSql,
            String resultSnapshot,
            String explanation,
            NlQueryStatus status,
            String errorMessage) {

        NlQuery nlQuery = new NlQuery();
        nlQuery.setConnection(connection);
        nlQuery.setQuestion(question);
        nlQuery.setGeneratedSql(generatedSql);
        nlQuery.setResultSnapshot(resultSnapshot);
        nlQuery.setExplanation(explanation);
        nlQuery.setStatus(status);
        nlQuery.setErrorMessage(errorMessage);

        persist(nlQuery);

        return nlQuery.getId();
    }
}

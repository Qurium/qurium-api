/* Qurium - 2026 */
package org.qurium.nlquery.domain;

import jakarta.persistence.*;
import lombok.*;
import org.qurium.common.BaseEntity;
import org.qurium.connection.domain.DatabaseConnection;
import org.qurium.uploadedfile.domain.UploadedFile;

@Table(name = "nl_query")
@Entity
@Getter
@Setter
@NoArgsConstructor
public class NlQuery extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "connection_id")
    private DatabaseConnection connection;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_file_id")
    private UploadedFile uploadedFile;

    @Column(name = "question")
    private String question;

    @Column(name = "generated_sql")
    private String generatedSql;

    @Column(name = "result_snapshot", columnDefinition = "TEXT")
    private String resultSnapshot;

    @Column(name = "execution_time_ms")
    private Long executionTimeMs;

    @Column(name = "rows_returned")
    private Integer rowsReturned;

    @Column(name = "explanation", columnDefinition = "TEXT")
    private String explanation;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private NlQueryStatus status;

    @Column(name = "error_message")
    private String errorMessage;
}

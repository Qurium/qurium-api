package org.qurium.nlquery.domain;

import jakarta.persistence.*;
import lombok.*;
import org.qurium.common.BaseEntity;
import org.qurium.connection.domain.DatabaseConnection;

@Table(name = "nl_query")
@Entity
@Getter
@Setter
@NoArgsConstructor
public class NlQuery extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "connection_id", nullable = false)
    private DatabaseConnection connection;

    @Column(name = "question")
    private String question;

    @Column(name = "generated_sql")
    private String generatedSql;

    @Column(name = "result_snapshot", columnDefinition = "TEXT")
    private String resultSnapshot;

    @Column(name = "explanation", columnDefinition = "TEXT")
    private String explanation;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private NlQueryStatus status;

    @Column(name = "error_message")
    private String errorMessage;
}

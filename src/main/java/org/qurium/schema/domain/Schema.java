/* Qurium - 2026 */
package org.qurium.schema.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.qurium.common.BaseEntity;
import org.qurium.connection.domain.DatabaseConnection;
import org.qurium.uploadedfile.domain.UploadedFile;

@Entity
@Table(name = "schema")
@Getter
@Setter
@NoArgsConstructor
public class Schema extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "connection_id")
    private DatabaseConnection connection;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_file_id")
    private UploadedFile uploadedFile;

    @Column(name = "schema_json", columnDefinition = "TEXT", nullable = false)
    private String schemaJson;

    @Enumerated(EnumType.STRING)
    @Column(name = "source", nullable = false)
    private SchemaSource source;
}

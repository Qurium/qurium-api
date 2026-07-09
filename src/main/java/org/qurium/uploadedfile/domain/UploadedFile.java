/* Qurium - 2026 */
package org.qurium.uploadedfile.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.qurium.common.BaseEntity;

@Entity
@Table(name = "uploaded_file")
@Getter
@Setter
@NoArgsConstructor
public class UploadedFile extends BaseEntity {

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "file_name", nullable = false)
    private String fileName;
}

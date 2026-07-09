package org.qurium.uploadedfile.dto.request;

import jakarta.validation.constraints.NotEmpty;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.multipart.FileUpload;

public record UploadFileRequest(@RestForm("file") FileUpload file, @NotEmpty @RestForm("name") String name) {
}

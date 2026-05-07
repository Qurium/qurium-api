/* Qurium - 2026 */
package org.qurium.common;

import java.util.List;
import lombok.Getter;

@Getter
public class PaginatedResponse<T> {

    private final List<T> content;
    private final int page;
    private final int size;
    private final int numberOfElements;
    private final long totalElements;
    private final int totalPages;
    private final boolean first;
    private final boolean last;
    private final boolean empty;

    private PaginatedResponse(
            List<T> content, int page, int size, long totalElements, int totalPages) {
        this.content = content;
        this.page = page;
        this.size = size;
        this.numberOfElements = content.size();
        this.totalElements = totalElements;
        this.totalPages = totalPages;
        this.first = page == 0;
        this.last = page >= totalPages - 1;
        this.empty = totalElements == 0;
    }

    public static <T> PaginatedResponse<T> of(List<T> query, int page, int size) {

        long totalElements = query.size();
        int totalPages = Math.max(1, (int) Math.ceil((double) totalElements / size));
        return new PaginatedResponse<>(query, page, size, totalElements, totalPages);
    }
}

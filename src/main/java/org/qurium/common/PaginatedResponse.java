package org.qurium.common;

import io.quarkus.hibernate.orm.panache.PanacheQuery;
import lombok.Getter;
import org.qurium.common.exception.QuriumException;

import java.util.List;

import static org.qurium.common.exception.QuriumExceptionCode.*;

@Getter
public class PaginatedResponse<T> {

    private static final int MAX_PAGE_SIZE = 100;

    private final List<T> content;
    private final int page;
    private final int size;
    private final int numberOfElements;
    private final long totalElements;
    private final int totalPages;
    private final boolean first;
    private final boolean last;
    private final boolean empty;

    private PaginatedResponse(List<T> content, int page, int size, long totalElements, int totalPages) {
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

    public static <T> PaginatedResponse<T> of(PanacheQuery<T> query, int page, int size) {
        if (page < 0) throw new QuriumException(PAGE_INDEX_NEGATIVE);
        if (size < 1) throw new QuriumException(PAGE_SIZE_NEGATIVE);
        if (size > MAX_PAGE_SIZE) throw new QuriumException(PAGE_SIZE_TOO_BIG);

        query.page(page, size);
        List<T> content = query.list();
        long totalElements = query.count();
        int totalPages = Math.max(1, (int) Math.ceil((double) totalElements / size));
        return new PaginatedResponse<>(content, page, size, totalElements, totalPages);
    }
}

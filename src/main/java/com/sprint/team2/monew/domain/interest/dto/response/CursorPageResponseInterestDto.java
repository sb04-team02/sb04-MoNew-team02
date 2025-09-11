package com.sprint.team2.monew.domain.interest.dto.response;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Slice;

import java.time.LocalDateTime;
import java.util.List;

public record CursorPageResponseInterestDto<T> (
        List<T> content,
        String nextCursor,
        LocalDateTime nextAfter,
        int size,
        Long totalElements,
        boolean hasNext
) {
    public static <T> CursorPageResponseInterestDto from(Page<T> page, LocalDateTime after, String cursor) {
        return new CursorPageResponseInterestDto<>(
                page.getContent(),
                cursor,
                after,
                page.getSize(),
                page.getTotalElements(),
                page.hasNext()
        );
    }

    public static <T> CursorPageResponseInterestDto<T> from(Slice<T> slice, String cursor, LocalDateTime after, Long totalElements) {
        return new CursorPageResponseInterestDto<>(
                slice.getContent(),
                cursor,
                after,
                slice.getSize(),
                totalElements,
                slice.hasNext()
        );
    }
}

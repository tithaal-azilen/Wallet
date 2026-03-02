package com.Tithaal.Wallet.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PaginationRequest {
    private int page;
    private int size;
    private String sortBy;
    private String sortDir;

    public static PaginationRequest of(int page, int size, String sortBy, String sortDir) {
        return PaginationRequest.builder()
                .page(page)
                .size(size)
                .sortBy(sortBy)
                .sortDir(sortDir)
                .build();
    }
}

package com.easytrax.easytraxbackend.packinglist.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

@Schema(description = "포장명세서 생성 요청")
public record PackingListCreateRequest(
        @Schema(description = "프로젝트 ID", example = "1")
        @NotNull(message = "프로젝트 ID는 필수입니다")
        Long projectId,

        @Schema(description = "발송인 이름", example = "ABC Export Company")
        @NotBlank(message = "발송인 이름은 필수입니다")
        String shipperName,

        @Schema(description = "발송인 주소", example = "123 Export St, Seoul, Korea")
        @NotBlank(message = "발송인 주소는 필수입니다")
        String shipperAddress,

        @Schema(description = "수취인 이름", example = "XYZ Import Company")
        @NotBlank(message = "수취인 이름은 필수입니다")
        String consigneeName,

        @Schema(description = "수취인 주소", example = "456 Import Ave, Beijing, China")
        @NotBlank(message = "수취인 주소는 필수입니다")
        String consigneeAddress,

        @Schema(description = "출발일", example = "2025-01-15")
        LocalDate departureDate,

        @Schema(description = "목적지", example = "중국")
        String destination,

        @Schema(description = "선박명", example = "Ocean Star")
        String vesselName,

        @Schema(description = "항해번호", example = "OS-2025-001")
        String voyageNumber,

        @Schema(description = "마크 및 번호", example = "No Marks")
        String marksAndNumbers,

        @Schema(description = "포장명세서 항목 목록")
        @Valid
        @NotNull(message = "포장명세서 항목은 필수입니다")
        List<PackingListItemRequest> items
) {
}
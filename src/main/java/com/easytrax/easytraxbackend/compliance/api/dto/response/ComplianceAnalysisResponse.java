package com.easytrax.easytraxbackend.compliance.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "수출 준수성 분석 결과")
public record ComplianceAnalysisResponse(
        
        @Schema(description = "분석 ID")
        Long analysisId,
        
        @Schema(description = "영양성분표 ID")
        Long nutritionLabelId,
        
        @Schema(description = "목적지 국가")
        String destinationCountry,
        
        @Schema(description = "제품 카테고리")
        String productCategory,
        
        @Schema(description = "전체 준수율 (%)")
        Integer overallCompliancePercentage,
        
        @Schema(description = "준수 상태")
        ComplianceStatus complianceStatus,
        
        @Schema(description = "예상 완료 시간 (분)")
        Integer estimatedCompletionMinutes,
        
        @Schema(description = "분석 완료 시각")
        LocalDateTime analysisCompletedAt,
        
        @Schema(description = "카테고리별 준수 점수")
        List<CategoryComplianceScore> categoryScores,
        
        @Schema(description = "발견된 문제점")
        List<ComplianceIssue> issues,
        
        @Schema(description = "개선 권장사항")
        List<ImprovementSuggestion> improvements
) {
    
    @Schema(description = "준수 상태")
    public enum ComplianceStatus {
        @Schema(description = "위험도 높음")
        HIGH_RISK,
        
        @Schema(description = "경고")
        WARNING,
        
        @Schema(description = "양호")
        GOOD
    }
    
    @Schema(description = "카테고리별 준수 점수")
    public record CategoryComplianceScore(
            @Schema(description = "카테고리명")
            String categoryName,
            
            @Schema(description = "점수 (%)")
            Integer score,
            
            @Schema(description = "상태")
            String status
    ) {}
    
    @Schema(description = "준수성 문제")
    public record ComplianceIssue(
            @Schema(description = "문제 유형")
            String issueType,
            
            @Schema(description = "심각도")
            String severity,
            
            @Schema(description = "문제 설명")
            String description,
            
            @Schema(description = "관련 규정")
            String regulation
    ) {}
    
    @Schema(description = "개선 권장사항")
    public record ImprovementSuggestion(
            @Schema(description = "개선 항목")
            String category,
            
            @Schema(description = "권장사항")
            String suggestion,
            
            @Schema(description = "우선순위")
            String priority
    ) {}
}
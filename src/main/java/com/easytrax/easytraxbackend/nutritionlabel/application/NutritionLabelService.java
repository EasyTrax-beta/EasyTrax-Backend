package com.easytrax.easytraxbackend.nutritionlabel.application;

import com.easytrax.easytraxbackend.global.code.status.ErrorStatus;
import com.easytrax.easytraxbackend.global.exception.GeneralException;
import com.easytrax.easytraxbackend.nutritionlabel.api.dto.request.NutritionLabelCreateRequest;
import com.easytrax.easytraxbackend.nutritionlabel.api.dto.response.NutritionLabelListResponse;
import com.easytrax.easytraxbackend.nutritionlabel.api.dto.response.NutritionLabelResponse;
import com.easytrax.easytraxbackend.nutritionlabel.domain.NutritionLabel;
import com.easytrax.easytraxbackend.nutritionlabel.domain.repository.NutritionLabelRepository;
import com.easytrax.easytraxbackend.project.domain.Project;
import com.easytrax.easytraxbackend.project.domain.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NutritionLabelService {

    private final NutritionLabelRepository nutritionLabelRepository;
    private final ProjectRepository projectRepository;

    @Transactional
    public NutritionLabelResponse createNutritionLabel(NutritionLabelCreateRequest request, Long userId) {
        Project project = findProjectByIdAndUserId(request.projectId(), userId);

        NutritionLabel nutritionLabel = NutritionLabel.builder()
                .productName(request.productName())
                .servingSize(request.servingSize())
                .servingsPerContainer(request.servingsPerContainer())
                .calories(request.calories())
                .caloriesFromFat(request.caloriesFromFat())
                .totalFat(request.totalFat())
                .saturatedFat(request.saturatedFat())
                .transFat(request.transFat())
                .cholesterol(request.cholesterol())
                .sodium(request.sodium())
                .totalCarbohydrate(request.totalCarbohydrate())
                .dietaryFiber(request.dietaryFiber())
                .totalSugars(request.totalSugars())
                .addedSugars(request.addedSugars())
                .protein(request.protein())
                .vitaminD(request.vitaminD())
                .calcium(request.calcium())
                .iron(request.iron())
                .potassium(request.potassium())
                .vitaminA(request.vitaminA())
                .vitaminC(request.vitaminC())
                .labelFormat(request.labelFormat())
                .project(project)
                .build();

        NutritionLabel savedNutritionLabel = nutritionLabelRepository.save(nutritionLabel);
        return NutritionLabelResponse.of(savedNutritionLabel);
    }

    public Page<NutritionLabelListResponse> findNutritionLabelsByProject(Long projectId, Long userId, Pageable pageable) {
        return nutritionLabelRepository.findByProjectIdAndUserId(projectId, userId, pageable)
                .map(NutritionLabelListResponse::of);
    }

    public Page<NutritionLabelListResponse> findNutritionLabelsByUser(Long userId, Pageable pageable) {
        return nutritionLabelRepository.findByUserId(userId, pageable)
                .map(NutritionLabelListResponse::of);
    }

    public NutritionLabelResponse findNutritionLabelById(Long id, Long userId) {
        NutritionLabel nutritionLabel = findNutritionLabelByIdAndUserId(id, userId);
        return NutritionLabelResponse.of(nutritionLabel);
    }

    @Transactional
    public NutritionLabelResponse updateNutritionLabel(Long id, NutritionLabelCreateRequest request, Long userId) {
        NutritionLabel nutritionLabel = findNutritionLabelByIdAndUserId(id, userId);

        nutritionLabel.updateNutritionLabel(
                request.productName(),
                request.servingSize(),
                request.servingsPerContainer(),
                request.calories(),
                request.caloriesFromFat(),
                request.totalFat(),
                request.saturatedFat(),
                request.transFat(),
                request.cholesterol(),
                request.sodium(),
                request.totalCarbohydrate(),
                request.dietaryFiber(),
                request.totalSugars(),
                request.addedSugars(),
                request.protein(),
                request.vitaminD(),
                request.calcium(),
                request.iron(),
                request.potassium(),
                request.vitaminA(),
                request.vitaminC(),
                request.labelFormat()
        );

        return NutritionLabelResponse.of(nutritionLabel);
    }

    @Transactional
    public void deleteNutritionLabel(Long id, Long userId) {
        NutritionLabel nutritionLabel = findNutritionLabelByIdAndUserId(id, userId);
        nutritionLabelRepository.delete(nutritionLabel);
    }

    private Project findProjectByIdAndUserId(Long projectId, Long userId) {
        return projectRepository.findByIdAndUserId(projectId, userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.PROJECT_NOT_FOUND));
    }

    public NutritionLabel findNutritionLabelByIdAndUserId(Long id, Long userId) {
        return nutritionLabelRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.NUTRITION_LABEL_NOT_FOUND));
    }
}
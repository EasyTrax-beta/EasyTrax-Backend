package com.easytrax.easytraxbackend.hscode.application;

import com.easytrax.easytraxbackend.global.code.status.ErrorStatus;
import com.easytrax.easytraxbackend.global.exception.GeneralException;
import com.easytrax.easytraxbackend.global.s3.S3UploadService;
import com.easytrax.easytraxbackend.hscode.api.dto.request.HSCodeClassifyByInfoRequest;
import com.easytrax.easytraxbackend.hscode.api.dto.response.HSCodeClassificationResult;
import com.easytrax.easytraxbackend.hscode.api.dto.response.HSCodeClassifyResponse;
import com.easytrax.easytraxbackend.hscode.api.dto.response.ProductInfoExtractionResponse;
import com.easytrax.easytraxbackend.hscode.api.dto.response.ProductOcrResult;
import com.easytrax.easytraxbackend.hscode.domain.ProductInfo;
import com.easytrax.easytraxbackend.hscode.domain.repository.ProductInfoRepository;
import com.easytrax.easytraxbackend.project.domain.Country;
import com.easytrax.easytraxbackend.project.domain.Project;
import com.easytrax.easytraxbackend.project.domain.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HSCodeService {

    private final ProjectRepository projectRepository;
    private final ProductInfoRepository productInfoRepository;
    private final GeminiOcrService geminiOcrService;
    private final HSCodeClassificationService classificationService;
    private final S3UploadService s3UploadService;

    @Transactional
    public ProductInfoExtractionResponse extractProductInfoFromImage(Long userId, Long projectId, MultipartFile imageFile) {
        
        findProjectByIdAndUserId(projectId, userId);
        
        String imageUrl = s3UploadService.uploadFile(imageFile);
        
        ProductOcrResult ocrResult = geminiOcrService.extractProductInfo(imageFile);
        
        return ProductInfoExtractionResponse.from(ocrResult, imageUrl);
    }

    @Transactional
    public HSCodeClassifyResponse classifyByImage(Long userId, Long projectId, MultipartFile imageFile, 
                                                Country originCountry, Country targetCountry) {
        
        Project project = findProjectByIdAndUserId(projectId, userId);
        
        String imageUrl = s3UploadService.uploadFile(imageFile);
        
        ProductOcrResult ocrResult = geminiOcrService.extractProductInfo(imageFile);
        
        HSCodeClassificationResult classificationResult = classificationService.classifyHSCode(
                ocrResult.productName(), 
                ocrResult.purpose(), 
                ocrResult.description(), 
                ocrResult.material()
        );
        
        ProductInfo productInfo = ProductInfo.builder()
                .productName(ocrResult.productName())
                .purpose(ocrResult.purpose())
                .description(ocrResult.description())
                .material(ocrResult.material())
                .originCountry(originCountry)
                .targetCountry(targetCountry)
                .extractedFromImage(true)
                .imageUrl(imageUrl)
                .confidenceScore(ocrResult.confidenceScore())
                .classifiedHsCode(classificationResult.hsCode())
                .classificationConfidence(classificationResult.confidence())
                .project(project)
                .build();
        
        ProductInfo savedProductInfo = productInfoRepository.save(productInfo);
        
        return HSCodeClassifyResponse.from(savedProductInfo);
    }

    @Transactional
    public HSCodeClassifyResponse classifyByInfo(Long userId, HSCodeClassifyByInfoRequest request) {
        
        Project project = findProjectByIdAndUserId(request.projectId(), userId);
        
        HSCodeClassificationResult classificationResult = classificationService.classifyHSCode(
                request.productName(), 
                request.purpose(), 
                request.description(), 
                request.material()
        );
        
        ProductInfo productInfo = request.toEntity(project);
        productInfo.updateClassificationResult(
                classificationResult.hsCode(), 
                classificationResult.confidence()
        );
        
        ProductInfo savedProductInfo = productInfoRepository.save(productInfo);
        
        return HSCodeClassifyResponse.from(savedProductInfo);
    }

    public HSCodeClassifyResponse findProductInfo(Long userId, Long productInfoId) {
        ProductInfo productInfo = productInfoRepository.findByIdAndUserId(productInfoId, userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.RESOURCE_NOT_FOUND));
        
        return HSCodeClassifyResponse.from(productInfo);
    }

    private Project findProjectByIdAndUserId(Long projectId, Long userId) {
        return projectRepository.findByIdAndUserId(projectId, userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.PROJECT_NOT_FOUND));
    }
}
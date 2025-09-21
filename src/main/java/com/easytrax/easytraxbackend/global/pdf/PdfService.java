package com.easytrax.easytraxbackend.global.pdf;

import com.itextpdf.html2pdf.ConverterProperties;
import com.itextpdf.html2pdf.HtmlConverter;
import com.itextpdf.html2pdf.resolver.font.DefaultFontProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Slf4j
@Service
public class PdfService {

    public byte[] generatePdfFromHtml(String htmlContent) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            ConverterProperties converterProperties = new ConverterProperties();
            DefaultFontProvider fontProvider = new DefaultFontProvider(true, true, true);
            converterProperties.setFontProvider(fontProvider);
            
            HtmlConverter.convertToPdf(htmlContent, outputStream, converterProperties);
            return outputStream.toByteArray();
        } catch (IOException e) {
            log.error("PDF 생성 중 오류 발생", e);
            throw new RuntimeException("PDF 생성에 실패했습니다.", e);
        }
    }
}
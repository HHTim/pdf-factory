package com.pdffactory.service;

import com.pdffactory.dto.PdfSecuritySettingsDTO;
import com.pdffactory.dto.PdfApplySecurityRequest;
import com.pdffactory.dto.PdfApplySecurityResponse;
import com.pdffactory.dto.PdfCreateSecuredRequest;
import com.pdffactory.dto.PdfCreateSecuredResponse;
import com.pdffactory.exception.PdfProcessingException;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfStamper;
import com.lowagie.text.pdf.PdfWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;

/**
 * PDF 安全性服務
 * 根據傳入的 DTO 參數產生新的 PDF 安全性設定
 */
@Slf4j
@Service
public class PdfSecurityService {

    /**
     * 套用安全性設定到現有 PDF
     *
     * @param inputPdf 原始 PDF 輸入流
     * @param settings 安全性設定 DTO
     * @return 加密後的 PDF byte 陣列
     */
    public byte[] applySecuritySettings(InputStream inputPdf,
                                        PdfSecuritySettingsDTO settings)
            throws IOException, DocumentException {

        log.info("開始套用安全性設定到現有 PDF");

        PdfReader reader = new PdfReader(inputPdf);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PdfStamper stamper = new PdfStamper(reader, outputStream);

        // 計算權限
        int permissions = calculatePermissions(settings);

        // 取得加密類型
        int encryptionType = getEncryptionType(settings.getEncryptionType());

        // 設定加密
        byte[] userPwd = settings.getUserPassword() != null
                ? settings.getUserPassword().getBytes()
                : null;
        byte[] ownerPwd = settings.getOwnerPassword() != null
                ? settings.getOwnerPassword().getBytes()
                : null;

        if (ownerPwd == null || ownerPwd.length == 0) {
            throw new PdfProcessingException("擁有者密碼不可為空");
        }

        stamper.setEncryption(
            userPwd,           // 使用者密碼
            ownerPwd,          // 擁有者密碼
            permissions,       // 權限設定
            encryptionType     // 加密類型
        );

        log.info("安全性設定已套用 - 加密類型: {}, 權限: {}",
                settings.getEncryptionType(), Integer.toBinaryString(permissions));

        stamper.close();
        reader.close();

        return outputStream.toByteArray();
    }

    /**
     * 建立新 PDF 時套用安全性設定
     *
     * @param writer PdfWriter 實例
     * @param settings 安全性設定 DTO
     */
    public void applySecurityToWriter(PdfWriter writer,
                                      PdfSecuritySettingsDTO settings)
            throws DocumentException {

        log.info("建立新 PDF 並套用安全性設定");

        int permissions = calculatePermissions(settings);
        int encryptionType = getEncryptionType(settings.getEncryptionType());

        byte[] userPwd = settings.getUserPassword() != null
                ? settings.getUserPassword().getBytes()
                : null;
        byte[] ownerPwd = settings.getOwnerPassword() != null
                ? settings.getOwnerPassword().getBytes()
                : null;

        if (ownerPwd == null || ownerPwd.length == 0) {
            throw new PdfProcessingException("擁有者密碼不可為空");
        }

        writer.setEncryption(userPwd, ownerPwd, permissions, encryptionType);

        log.info("安全性設定已套用 - 加密類型: {}, 權限: {}",
                settings.getEncryptionType(), Integer.toBinaryString(permissions));
    }

    /**
     * 建立範例 PDF 並套用安全性設定
     *
     * @param settings 安全性設定 DTO
     * @param title PDF 標題
     * @param content PDF 內容
     * @return 加密後的 PDF byte 陣列
     */
    public byte[] createSecuredPdf(PdfSecuritySettingsDTO settings,
                                   String title,
                                   String content)
            throws DocumentException, IOException {

        log.info("建立新的安全 PDF - 標題: {}", title);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document();
        PdfWriter writer = PdfWriter.getInstance(document, baos);

        // 套用安全性設定
        applySecurityToWriter(writer, settings);

        // 設定 metadata
        document.addTitle(title != null ? title : "Secured PDF Document");
        document.addCreator("PDF Security Service");
        document.addProducer("OpenPDF Library");

        // 開啟文件並加入內容
        document.open();
        document.add(new Paragraph(content != null ? content : "This is a secured PDF document."));
        document.close();

        log.info("安全 PDF 建立完成,大小: {} bytes", baos.size());

        return baos.toByteArray();
    }

    /**
     * 套用安全性設定到 PDF（處理 Base64 編碼）
     *
     * @param request 套用安全性請求
     * @return 套用安全性響應
     */
    public PdfApplySecurityResponse applySecurityToBase64Pdf(PdfApplySecurityRequest request) {
        try {
            log.info("收到套用安全性設定請求: {}", request.getFileName());

            // 1. 解碼 Base64
            byte[] pdfBytes = Base64.getDecoder().decode(request.getPdfFileBase64());

            // 2. 建立安全性設定 DTO
            PdfSecuritySettingsDTO settings = PdfSecuritySettingsDTO.builder()
                    .allowPrinting(request.getAllowPrinting())
                    .allowHighQualityPrinting(request.getAllowHighQualityPrinting())
                    .allowAssembly(request.getAllowAssembly())
                    .allowCopy(request.getAllowCopy())
                    .allowScreenReaders(request.getAllowScreenReaders())
                    .allowModifyContents(request.getAllowModifyContents())
                    .allowModifyAnnotations(request.getAllowModifyAnnotations())
                    .allowFillIn(request.getAllowFillIn())
                    .userPassword(request.getUserPassword())
                    .ownerPassword(request.getOwnerPassword())
                    .encryptionType(request.getEncryptionType())
                    .build();

            // 3. 套用安全性設定
            byte[] result = applySecuritySettings(new ByteArrayInputStream(pdfBytes), settings);

            // 4. 編碼為 Base64
            String base64Result = Base64.getEncoder().encodeToString(result);

            return PdfApplySecurityResponse.builder()
                    .success(true)
                    .message("安全性設定套用成功")
                    .pdfFileBase64(base64Result)
                    .fileSize((long) result.length)
                    .build();

        } catch (Exception e) {
            log.error("套用安全性設定失敗", e);
            return PdfApplySecurityResponse.builder()
                    .success(false)
                    .message("套用安全性設定失敗: " + e.getMessage())
                    .build();
        }
    }

    /**
     * 建立新的安全 PDF（處理 Base64 編碼）
     *
     * @param request 建立安全 PDF 請求
     * @return 建立安全 PDF 響應
     */
    public PdfCreateSecuredResponse createSecuredPdfBase64(PdfCreateSecuredRequest request) {
        try {
            log.info("建立新的安全 PDF - 標題: {}, 加密類型: {}",
                    request.getTitle(), request.getEncryptionType());

            // 1. 建立安全性設定 DTO
            PdfSecuritySettingsDTO settings = PdfSecuritySettingsDTO.builder()
                    .allowPrinting(request.getAllowPrinting())
                    .allowHighQualityPrinting(request.getAllowHighQualityPrinting())
                    .allowAssembly(request.getAllowAssembly())
                    .allowCopy(request.getAllowCopy())
                    .allowScreenReaders(request.getAllowScreenReaders())
                    .allowModifyContents(request.getAllowModifyContents())
                    .allowModifyAnnotations(request.getAllowModifyAnnotations())
                    .allowFillIn(request.getAllowFillIn())
                    .userPassword(request.getUserPassword())
                    .ownerPassword(request.getOwnerPassword())
                    .encryptionType(request.getEncryptionType())
                    .build();

            // 2. 建立安全 PDF
            byte[] result = createSecuredPdf(settings, request.getTitle(), request.getContent());

            // 3. 編碼為 Base64
            String base64Result = Base64.getEncoder().encodeToString(result);

            return PdfCreateSecuredResponse.builder()
                    .success(true)
                    .message("安全 PDF 建立成功")
                    .pdfFileBase64(base64Result)
                    .fileSize((long) result.length)
                    .build();

        } catch (Exception e) {
            log.error("建立安全 PDF 失敗", e);
            return PdfCreateSecuredResponse.builder()
                    .success(false)
                    .message("建立安全 PDF 失敗: " + e.getMessage())
                    .build();
        }
    }

    /**
     * 根據 DTO 設定計算權限值
     */
    private int calculatePermissions(PdfSecuritySettingsDTO settings) {
        int permissions = 0;

        // 列印權限
        if (PdfSecuritySettingsDTO.isAllowed(settings.getAllowPrinting())) {
            permissions |= PdfWriter.ALLOW_PRINTING;
        }

        // 高品質列印 (降級列印的反向邏輯)
        if (PdfSecuritySettingsDTO.isAllowed(settings.getAllowHighQualityPrinting())) {
            permissions |= PdfWriter.ALLOW_DEGRADED_PRINTING;
        }

        // 文件組合
        if (PdfSecuritySettingsDTO.isAllowed(settings.getAllowAssembly())) {
            permissions |= PdfWriter.ALLOW_ASSEMBLY;
        }

        // 內容複製
        if (PdfSecuritySettingsDTO.isAllowed(settings.getAllowCopy())) {
            permissions |= PdfWriter.ALLOW_COPY;
        }

        // 協助工具存取
        if (PdfSecuritySettingsDTO.isAllowed(settings.getAllowScreenReaders())) {
            permissions |= PdfWriter.ALLOW_SCREENREADERS;
        }

        // 修改內容
        if (PdfSecuritySettingsDTO.isAllowed(settings.getAllowModifyContents())) {
            permissions |= PdfWriter.ALLOW_MODIFY_CONTENTS;
        }

        // 修改注釋
        if (PdfSecuritySettingsDTO.isAllowed(settings.getAllowModifyAnnotations())) {
            permissions |= PdfWriter.ALLOW_MODIFY_ANNOTATIONS;
        }

        // 填寫表格
        if (PdfSecuritySettingsDTO.isAllowed(settings.getAllowFillIn())) {
            permissions |= PdfWriter.ALLOW_FILL_IN;
        }

        return permissions;
    }

    /**
     * 解析加密類型字串
     */
    private int getEncryptionType(String type) {
        if (type == null) {
            return PdfWriter.STANDARD_ENCRYPTION_128;
        }

        return switch (type.toUpperCase()) {
            case "RC4_40" -> PdfWriter.STANDARD_ENCRYPTION_40;
            case "RC4_128" -> PdfWriter.STANDARD_ENCRYPTION_128;
            case "AES_128" -> PdfWriter.ENCRYPTION_AES_128;
            case "AES_256" -> {
                // OpenPDF 1.3.34 不支援 AES_256，降級至 AES_128
                log.warn("OpenPDF 1.3.34 不支援 AES_256，降級至 AES_128");
                yield PdfWriter.ENCRYPTION_AES_128;
            }
            default -> PdfWriter.STANDARD_ENCRYPTION_128;
        };
    }
}

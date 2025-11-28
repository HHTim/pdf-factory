package com.pdffactory.service;

import com.pdffactory.dto.PdfRewriteRequest;
import com.pdffactory.dto.PdfRewriteResponse;
import com.pdffactory.dto.PdfUploadAndRewriteRequest;
import com.pdffactory.dto.PdfUploadAndRewriteResponse;
import com.pdffactory.dto.PdfSecurityInfo;
import com.pdffactory.exception.PdfProcessingException;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

/**
 * PDF 重寫服務
 * 核心功能：讀取 PDF 並使用 OpenPDF 重新生成，移除 iText 字樣並保留安全性設定
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PdfRewriterService {

    private final PdfReaderService pdfReaderService;

    /**
     * 重寫 PDF 檔案
     *
     * @param request 重寫請求
     * @return 重寫響應
     */
    public PdfRewriteResponse rewritePdf(PdfRewriteRequest request) {
        try {
            log.info("開始重寫 PDF: {}", request.getInputPath());

            // 1. 讀取原 PDF 安全性資訊
            String password = request.getOwnerPassword() != null ?
                    request.getOwnerPassword() : request.getUserPassword();
            PdfSecurityInfo originalSecurityInfo = pdfReaderService.extractSecurityInfo(
                    request.getInputPath(), password);

            // 2. 讀取原 PDF
            PdfReader reader = pdfReaderService.getPdfReader(request.getInputPath(), password);

            // 3. 重寫 PDF
            byte[] pdfBytes;
            if (request.getOutputPath() != null) {
                rewritePdfToFile(reader, request, originalSecurityInfo);
                pdfBytes = null;
            } else {
                pdfBytes = rewritePdfToBytes(reader, request, originalSecurityInfo);
            }

            reader.close();

            // 4. 讀取新 PDF 的安全性資訊（驗證）
            PdfSecurityInfo newSecurityInfo = null;
            if (request.getOutputPath() != null) {
                String newPassword = request.getNewOwnerPassword() != null ?
                        request.getNewOwnerPassword() : request.getOwnerPassword();
                newSecurityInfo = pdfReaderService.extractSecurityInfo(
                        request.getOutputPath(), newPassword);
            }

            log.info("PDF 重寫完成: {}", request.getOutputPath());

            return PdfRewriteResponse.builder()
                    .success(true)
                    .message("PDF 重寫成功")
                    .outputPath(request.getOutputPath())
                    .originalSecurityInfo(originalSecurityInfo)
                    .newSecurityInfo(newSecurityInfo)
                    .pdfBytes(pdfBytes)
                    .fileSize(pdfBytes != null ? (long) pdfBytes.length : null)
                    .build();

        } catch (Exception e) {
            log.error("PDF 重寫失敗", e);
            return PdfRewriteResponse.builder()
                    .success(false)
                    .message("PDF 重寫失敗: " + e.getMessage())
                    .build();
        }
    }

    /**
     * 上傳並重寫 PDF（處理 Base64 編碼）
     *
     * @param request 上傳並重寫請求
     * @return 上傳並重寫響應
     */
    public PdfUploadAndRewriteResponse uploadAndRewritePdf(PdfUploadAndRewriteRequest request) {
        Path tempDir = null;
        Path inputPath = null;

        try {
            log.info("收到 PDF 上傳並重寫請求: {}", request.getFileName());

            // 1. 解碼 Base64
            byte[] pdfBytes = Base64.getDecoder().decode(request.getPdfFileBase64());

            // 2. 儲存到暫存檔案
            tempDir = Files.createTempDirectory("pdf-factory");
            inputPath = tempDir.resolve(request.getFileName());
            Files.write(inputPath, pdfBytes);

            // 3. 建立重寫請求
            PdfRewriteRequest rewriteRequest = PdfRewriteRequest.builder()
                    .inputPath(inputPath.toString())
                    .ownerPassword(request.getOwnerPassword())
                    .userPassword(request.getUserPassword())
                    .preserveSecurity(request.isPreserveSecurity())
                    .build();

            // 4. 執行重寫
            PdfRewriteResponse rewriteResponse = rewritePdf(rewriteRequest);

            if (rewriteResponse.isSuccess() && rewriteResponse.getPdfBytes() != null) {
                // 5. 編碼為 Base64
                String base64Result = Base64.getEncoder().encodeToString(rewriteResponse.getPdfBytes());

                return PdfUploadAndRewriteResponse.builder()
                        .success(true)
                        .message("PDF 重寫成功")
                        .pdfFileBase64(base64Result)
                        .fileSize((long) rewriteResponse.getPdfBytes().length)
                        .originalSecurityInfo(rewriteResponse.getOriginalSecurityInfo())
                        .newSecurityInfo(rewriteResponse.getNewSecurityInfo())
                        .build();
            } else {
                return PdfUploadAndRewriteResponse.builder()
                        .success(false)
                        .message(rewriteResponse.getMessage())
                        .build();
            }

        } catch (IOException e) {
            log.error("PDF 上傳並重寫失敗", e);
            return PdfUploadAndRewriteResponse.builder()
                    .success(false)
                    .message("PDF 上傳並重寫失敗: " + e.getMessage())
                    .build();
        } finally {
            // 6. 清理暫存檔案
            try {
                if (inputPath != null) {
                    Files.deleteIfExists(inputPath);
                }
                if (tempDir != null) {
                    Files.deleteIfExists(tempDir);
                }
            } catch (IOException e) {
                log.warn("清理暫存檔案失敗", e);
            }
        }
    }

    /**
     * 重寫 PDF 到檔案
     */
    private void rewritePdfToFile(PdfReader reader, PdfRewriteRequest request,
                                   PdfSecurityInfo securityInfo) throws IOException, DocumentException {

        try (FileOutputStream fos = new FileOutputStream(request.getOutputPath())) {
            processPdfRewrite(reader, fos, request, securityInfo);
        }
    }

    /**
     * 重寫 PDF 到位元組陣列
     */
    private byte[] rewritePdfToBytes(PdfReader reader, PdfRewriteRequest request,
                                      PdfSecurityInfo securityInfo) throws IOException, DocumentException {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        processPdfRewrite(reader, baos, request, securityInfo);
        return baos.toByteArray();
    }

    /**
     * 處理 PDF 重寫核心邏輯
     */
    private void processPdfRewrite(PdfReader reader, java.io.OutputStream outputStream,
                                   PdfRewriteRequest request, PdfSecurityInfo securityInfo)
            throws IOException, DocumentException {

        int totalPages = reader.getNumberOfPages();
        log.info("原 PDF 共有 {} 頁", totalPages);

        // 建立新文件（使用第一頁的尺寸）
        Rectangle pageSize = reader.getPageSize(1);
        Document document = new Document(pageSize);

        // 建立 PdfCopy 以複製內容
        PdfCopy copy = new PdfCopy(document, outputStream);

        // 設定 PDF metadata（移除 iText 字樣）
        document.addCreator("PDF Rewriter Application");
        document.addProducer("OpenPDF Library");
        document.addTitle(getOriginalTitle(reader));
        document.addAuthor(getOriginalAuthor(reader));
        document.addSubject(getOriginalSubject(reader));

        // 套用安全性設定
        if (request.isPreserveSecurity() && !request.isRemoveSecurity() && securityInfo.isEncrypted()) {
            applySecuritySettings(copy, request, securityInfo);
        } else if (!request.isRemoveSecurity()) {
            log.info("未保留安全性設定或原 PDF 未加密");
        } else {
            log.info("已移除所有安全性設定");
        }

        // 開啟文件
        document.open();

        // 複製所有頁面
        for (int i = 1; i <= totalPages; i++) {
            PdfImportedPage page = copy.getImportedPage(reader, i);
            copy.addPage(page);
            log.debug("複製第 {}/{} 頁", i, totalPages);
        }

        document.close();
        log.info("PDF 文件重寫完成，共 {} 頁", totalPages);
    }

    /**
     * 套用安全性設定到新 PDF
     */
    private void applySecuritySettings(PdfCopy copy, PdfRewriteRequest request,
                                       PdfSecurityInfo securityInfo) {

        try {
            // 決定使用的密碼
            String userPassword = request.getNewUserPassword() != null ?
                    request.getNewUserPassword() : request.getUserPassword();
            String ownerPassword = request.getNewOwnerPassword() != null ?
                    request.getNewOwnerPassword() : request.getOwnerPassword();

            if (ownerPassword == null || ownerPassword.isEmpty()) {
                log.warn("未提供擁有者密碼，使用預設密碼");
                ownerPassword = "default-owner-password";
            }

            // 使用原始權限
            int permissions = securityInfo.getPermissions();

            // 決定加密類型
            int encryptionType = determineEncryptionType(securityInfo);

            // 設定加密
            copy.setEncryption(
                    userPassword != null ? userPassword.getBytes() : null,
                    ownerPassword.getBytes(),
                    permissions,
                    encryptionType
            );

            log.info("已套用安全性設定 - 加密: {}, 權限: {}",
                    securityInfo.getEncryptionAlgorithm(),
                    Integer.toBinaryString(permissions));

        } catch (DocumentException e) {
            log.error("套用安全性設定失敗", e);
            throw new PdfProcessingException("套用安全性設定失敗", e);
        }
    }

    /**
     * 根據原始加密等級決定新的加密類型
     */
    private int determineEncryptionType(PdfSecurityInfo securityInfo) {
        Integer level = securityInfo.getEncryptionLevel();
        String algorithm = securityInfo.getEncryptionAlgorithm();

        if (level == null) {
            return PdfWriter.ENCRYPTION_AES_128; // 預設
        }

        if (level == 256 || (algorithm != null && algorithm.contains("256"))) {
            // OpenPDF 1.3.34 可能不支援 256-bit，降級到 128-bit AES
            log.warn("256-bit 加密可能不支援，降級至 128-bit AES");
            return PdfWriter.ENCRYPTION_AES_128;
        } else if (level == 128 || (algorithm != null && algorithm.contains("128"))) {
            // 優先使用 AES 而非 RC4
            if (algorithm != null && algorithm.contains("AES")) {
                return PdfWriter.ENCRYPTION_AES_128;
            } else {
                return PdfWriter.STANDARD_ENCRYPTION_128;
            }
        } else if (level == 40) {
            return PdfWriter.STANDARD_ENCRYPTION_40;
        }

        return PdfWriter.ENCRYPTION_AES_128; // 預設
    }

    /**
     * 取得原始 PDF 標題
     */
    private String getOriginalTitle(PdfReader reader) {
        return reader.getInfo().getOrDefault("Title", "Rewritten PDF Document");
    }

    /**
     * 取得原始 PDF 作者
     */
    private String getOriginalAuthor(PdfReader reader) {
        return reader.getInfo().getOrDefault("Author", "");
    }

    /**
     * 取得原始 PDF 主題
     */
    private String getOriginalSubject(PdfReader reader) {
        return reader.getInfo().getOrDefault("Subject", "");
    }
}

package com.pdffactory.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * PDF 上傳並重寫請求 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PdfUploadAndRewriteRequest {

    /**
     * PDF 文件內容 (Base64 編碼)
     */
    private String pdfFileBase64;

    /**
     * 原始檔案名稱
     */
    private String fileName;

    /**
     * 擁有者密碼（若原 PDF 有加密需提供）
     */
    private String ownerPassword;

    /**
     * 使用者密碼（若原 PDF 有加密需提供）
     */
    private String userPassword;

    /**
     * 是否保留原始安全性設定（預設 true）
     */
    @Builder.Default
    private boolean preserveSecurity = true;
}

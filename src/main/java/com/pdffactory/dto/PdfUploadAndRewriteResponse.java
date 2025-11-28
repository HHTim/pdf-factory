package com.pdffactory.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * PDF 上傳並重寫響應 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PdfUploadAndRewriteResponse {

    /**
     * 是否成功
     */
    private boolean success;

    /**
     * 訊息
     */
    private String message;

    /**
     * 重寫後的 PDF 文件內容 (Base64 編碼)
     */
    private String pdfFileBase64;

    /**
     * 檔案大小 (bytes)
     */
    private Long fileSize;

    /**
     * 原始安全性資訊
     */
    private PdfSecurityInfo originalSecurityInfo;

    /**
     * 新的安全性資訊
     */
    private PdfSecurityInfo newSecurityInfo;
}

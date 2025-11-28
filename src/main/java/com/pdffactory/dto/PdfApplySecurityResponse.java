package com.pdffactory.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * PDF 套用安全性設定響應 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PdfApplySecurityResponse {

    /**
     * 是否成功
     */
    private boolean success;

    /**
     * 訊息
     */
    private String message;

    /**
     * 加密後的 PDF 文件內容 (Base64 編碼)
     */
    private String pdfFileBase64;

    /**
     * 檔案大小 (bytes)
     */
    private Long fileSize;
}

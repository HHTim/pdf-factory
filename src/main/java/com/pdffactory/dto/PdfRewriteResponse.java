package com.pdffactory.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * PDF 重寫響應 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PdfRewriteResponse {

    /**
     * 是否成功
     */
    private boolean success;

    /**
     * 訊息
     */
    private String message;

    /**
     * 輸出檔案路徑
     */
    private String outputPath;

    /**
     * 原始 PDF 安全性資訊
     */
    private PdfSecurityInfo originalSecurityInfo;

    /**
     * 新 PDF 安全性資訊
     */
    private PdfSecurityInfo newSecurityInfo;

    /**
     * PDF 位元組陣列（若未提供輸出路徑）
     */
    private byte[] pdfBytes;

    /**
     * 檔案大小（bytes）
     */
    private Long fileSize;
}

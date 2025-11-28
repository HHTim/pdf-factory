package com.pdffactory.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * PDF 重寫請求 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PdfRewriteRequest {

    /**
     * 輸入 PDF 檔案路徑
     */
    private String inputPath;

    /**
     * 輸出 PDF 檔案路徑（可選，若未提供則返回 byte[]）
     */
    private String outputPath;

    /**
     * 擁有者密碼（若原 PDF 有加密需提供）
     */
    private String ownerPassword;

    /**
     * 使用者密碼（若原 PDF 有加密需提供）
     */
    private String userPassword;

    /**
     * 新的擁有者密碼（可選，若未提供則使用原密碼）
     */
    private String newOwnerPassword;

    /**
     * 新的使用者密碼（可選，若未提供則使用原密碼）
     */
    private String newUserPassword;

    /**
     * 是否保留原始安全性設定（預設 true）
     */
    @Builder.Default
    private boolean preserveSecurity = true;

    /**
     * 是否移除所有安全性設定（預設 false）
     */
    @Builder.Default
    private boolean removeSecurity = false;
}

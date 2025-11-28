package com.pdffactory.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * PDF 安全性設定 DTO
 * 用於根據傳入參數產生新的 PDF 安全性設定
 * 每個參數接受 "Y"/"N" 或 true/false
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PdfSecuritySettingsDTO {

    /**
     * 是否允許列印 (Y/N 或 true/false)
     */
    private String allowPrinting;

    /**
     * 是否允許高品質列印
     */
    private String allowHighQualityPrinting;

    /**
     * 是否允許文件組合
     */
    private String allowAssembly;

    /**
     * 是否允許內容複製
     */
    private String allowCopy;

    /**
     * 是否允許複製內容用於協助工具
     */
    private String allowScreenReaders;

    /**
     * 是否允許修改內容
     */
    private String allowModifyContents;

    /**
     * 是否允許修改注釋
     */
    private String allowModifyAnnotations;

    /**
     * 是否允許填寫表格欄位
     */
    private String allowFillIn;

    /**
     * 使用者密碼 (開啟文件) - 可為 null
     */
    private String userPassword;

    /**
     * 擁有者密碼 (權限控制) - 必填
     */
    private String ownerPassword;

    /**
     * 加密類型: RC4_40, RC4_128, AES_128, AES_256
     */
    @Builder.Default
    private String encryptionType = "RC4_128";

    /**
     * 判斷字串是否為允許 (Y/true)
     */
    public static boolean isAllowed(String value) {
        if (value == null) return false;
        String v = value.trim().toUpperCase();
        return "Y".equals(v) || "TRUE".equals(v) || "1".equals(v);
    }
}

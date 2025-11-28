package com.pdffactory.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 建立新的安全 PDF 請求 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PdfCreateSecuredRequest {

    /**
     * PDF 標題
     */
    @Builder.Default
    private String title = "Secured PDF Document";

    /**
     * PDF 內容
     */
    @Builder.Default
    private String content = "This is a secured PDF document created by OpenPDF.";

    /**
     * 是否允許列印 (Y/N 或 true/false)
     */
    @Builder.Default
    private String allowPrinting = "N";

    /**
     * 是否允許高品質列印
     */
    @Builder.Default
    private String allowHighQualityPrinting = "N";

    /**
     * 是否允許文件組合
     */
    @Builder.Default
    private String allowAssembly = "N";

    /**
     * 是否允許內容複製
     */
    @Builder.Default
    private String allowCopy = "N";

    /**
     * 是否允許複製內容用於協助工具
     */
    @Builder.Default
    private String allowScreenReaders = "N";

    /**
     * 是否允許修改內容
     */
    @Builder.Default
    private String allowModifyContents = "N";

    /**
     * 是否允許修改注釋
     */
    @Builder.Default
    private String allowModifyAnnotations = "N";

    /**
     * 是否允許填寫表格欄位
     */
    @Builder.Default
    private String allowFillIn = "N";

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
}

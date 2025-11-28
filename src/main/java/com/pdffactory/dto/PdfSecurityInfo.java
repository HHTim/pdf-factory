package com.pdffactory.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * PDF 安全性資訊 DTO
 * 用於儲存從原 PDF 讀取的安全性設定
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PdfSecurityInfo {

    /**
     * 是否加密
     */
    private boolean encrypted;

    /**
     * 加密等級 (40/128/256)
     */
    private Integer encryptionLevel;

    /**
     * 加密演算法名稱
     */
    private String encryptionAlgorithm;

    /**
     * 權限碼 (permissions bitmask)
     */
    private int permissions;

    /**
     * 是否允許列印
     */
    private boolean allowPrinting;

    /**
     * 是否允許高解析度列印
     */
    private boolean allowDegradedPrinting;

    /**
     * 是否允許修改內容
     */
    private boolean allowModifyContents;

    /**
     * 是否允許複製內容
     */
    private boolean allowCopy;

    /**
     * 是否允許修改註解
     */
    private boolean allowModifyAnnotations;

    /**
     * 是否允許填寫表單
     */
    private boolean allowFillIn;

    /**
     * 是否允許螢幕閱讀器存取
     */
    private boolean allowScreenReaders;

    /**
     * 是否允許文件組合
     */
    private boolean allowAssembly;

    /**
     * PDF 版本
     */
    private String pdfVersion;

    /**
     * PDF Creator (原始創建者)
     */
    private String creator;

    /**
     * PDF Producer (原始產生器)
     */
    private String producer;

    /**
     * 是否有使用者密碼
     */
    private boolean hasUserPassword;

    /**
     * 是否有擁有者密碼
     */
    private boolean hasOwnerPassword;
}

package com.pdffactory.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * PDF 安全性資訊查詢請求 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PdfSecurityInfoRequest {

    /**
     * PDF 檔案路徑
     */
    private String filePath;

    /**
     * 密碼 (若 PDF 有加密)
     */
    private String password;
}

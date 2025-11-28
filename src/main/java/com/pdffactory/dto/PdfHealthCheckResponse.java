package com.pdffactory.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 健康檢查響應 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PdfHealthCheckResponse {

    /**
     * 服務狀態
     */
    private String status;

    /**
     * 服務名稱
     */
    private String serviceName;

    /**
     * 版本號
     */
    private String version;

    /**
     * 時間戳記
     */
    private Long timestamp;
}

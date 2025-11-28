package com.pdffactory.controller;

import com.pdffactory.dto.*;
import com.pdffactory.service.PdfReaderService;
import com.pdffactory.service.PdfRewriterService;
import com.pdffactory.service.PdfSecurityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * PDF Factory REST Controller
 * 所有 API 均使用 POST mapping 和 @RequestBody
 */
@Slf4j
@RestController
@RequestMapping("/api/pdf")
@RequiredArgsConstructor
@Tag(name = "PDF Factory API", description = "PDF 處理服務 - 提供 PDF 重寫和安全性管理功能")
public class PdfController {

    private final PdfRewriterService pdfRewriterService;
    private final PdfReaderService pdfReaderService;
    private final PdfSecurityService pdfSecurityService;

    /**
     * 重寫 PDF（從檔案路徑）
     * <p>
     * POST /api/pdf/rewrite
     * <p>
     * 請求體範例：
     * {
     *   "inputPath": "/path/to/input.pdf",
     *   "outputPath": "/path/to/output.pdf",
     *   "ownerPassword": "password123",
     *   "preserveSecurity": true
     * }
     */
    @Operation(
        summary = "重寫 PDF（檔案路徑）",
        description = "讀取本地 PDF 文件並使用 OpenPDF 重新生成，移除 iText 字樣。可選擇保留或修改安全性設定。"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "PDF 重寫成功",
            content = @Content(schema = @Schema(implementation = PdfRewriteResponse.class))
        ),
        @ApiResponse(
            responseCode = "500",
            description = "PDF 重寫失敗",
            content = @Content(schema = @Schema(implementation = PdfRewriteResponse.class))
        )
    })
    @PostMapping("/rewrite")
    public ResponseEntity<PdfRewriteResponse> rewritePdf(@RequestBody PdfRewriteRequest request) {
        try {
            log.info("收到 PDF 重寫請求: {}", request.getInputPath());

            PdfRewriteResponse response = pdfRewriterService.rewritePdf(request);

            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }

        } catch (Exception e) {
            log.error("PDF 重寫失敗", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(PdfRewriteResponse.builder()
                            .success(false)
                            .message("PDF 重寫失敗: " + e.getMessage())
                            .build());
        }
    }

    /**
     * 上傳並重寫 PDF
     * <p>
     * POST /api/pdf/upload-and-rewrite
     * <p>
     * 請求體範例：
     * {
     *   "pdfFileBase64": "JVBERi0xLjQKJeLjz9MK...",
     *   "fileName": "document.pdf",
     *   "ownerPassword": "password123",
     *   "userPassword": null,
     *   "preserveSecurity": true
     * }
     */
    @Operation(
        summary = "上傳並重寫 PDF",
        description = "接收 Base64 編碼的 PDF 文件，重寫後返回 Base64 結果。在伺服器建立臨時檔案處理，完成後自動清理。"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "PDF 上傳並重寫成功",
            content = @Content(schema = @Schema(implementation = PdfUploadAndRewriteResponse.class))
        ),
        @ApiResponse(
            responseCode = "500",
            description = "PDF 上傳並重寫失敗",
            content = @Content(schema = @Schema(implementation = PdfUploadAndRewriteResponse.class))
        )
    })
    @PostMapping("/upload-and-rewrite")
    public ResponseEntity<PdfUploadAndRewriteResponse> uploadAndRewritePdf(
            @RequestBody PdfUploadAndRewriteRequest request) {

        try {
            PdfUploadAndRewriteResponse response = pdfRewriterService.uploadAndRewritePdf(request);

            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }

        } catch (Exception e) {
            log.error("PDF 上傳並重寫失敗", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(PdfUploadAndRewriteResponse.builder()
                            .success(false)
                            .message("PDF 上傳並重寫失敗: " + e.getMessage())
                            .build());
        }
    }

    /**
     * 查詢 PDF 安全性資訊
     * <p>
     * POST /api/pdf/security-info
     * <p>
     * 請求體範例：
     * {
     *   "filePath": "/path/to/file.pdf",
     *   "password": "your_password"
     * }
     */
    @Operation(
        summary = "查詢 PDF 安全性資訊",
        description = "讀取 PDF 文件的完整安全性資訊，包含加密等級、演算法、權限設定和 metadata。"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "成功讀取安全性資訊",
            content = @Content(schema = @Schema(implementation = PdfSecurityInfo.class))
        ),
        @ApiResponse(
            responseCode = "500",
            description = "讀取安全性資訊失敗"
        )
    })
    @PostMapping("/security-info")
    public ResponseEntity<PdfSecurityInfo> getSecurityInfo(@RequestBody PdfSecurityInfoRequest request) {

        try {
            log.info("查詢 PDF 安全性資訊: {}", request.getFilePath());

            PdfSecurityInfo securityInfo = pdfReaderService.extractSecurityInfo(
                    request.getFilePath(), request.getPassword());

            return ResponseEntity.ok(securityInfo);

        } catch (Exception e) {
            log.error("讀取 PDF 安全性資訊失敗", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 套用安全性設定到 PDF
     * <p>
     * POST /api/pdf/apply-security
     * <p>
     * 請求體範例：
     * {
     *   "pdfFileBase64": "JVBERi0xLjQKJeLjz9MK...",
     *   "fileName": "document.pdf",
     *   "allowPrinting": "Y",
     *   "allowHighQualityPrinting": "Y",
     *   "allowAssembly": "N",
     *   "allowCopy": "N",
     *   "allowScreenReaders": "N",
     *   "allowModifyContents": "N",
     *   "allowModifyAnnotations": "N",
     *   "allowFillIn": "N",
     *   "userPassword": null,
     *   "ownerPassword": "your_password",
     *   "encryptionType": "RC4_128"
     * }
     */
    @Operation(
        summary = "套用安全性設定到 PDF",
        description = "接收 Base64 編碼的現有 PDF，套用自訂的安全性設定（權限控制、加密演算法、密碼），返回 Base64 結果。"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "安全性設定套用成功",
            content = @Content(schema = @Schema(implementation = PdfApplySecurityResponse.class))
        ),
        @ApiResponse(
            responseCode = "500",
            description = "安全性設定套用失敗",
            content = @Content(schema = @Schema(implementation = PdfApplySecurityResponse.class))
        )
    })
    @PostMapping("/apply-security")
    public ResponseEntity<PdfApplySecurityResponse> applySecurityToPdf(
            @RequestBody PdfApplySecurityRequest request) {

        try {
            PdfApplySecurityResponse response = pdfSecurityService.applySecurityToBase64Pdf(request);

            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }

        } catch (Exception e) {
            log.error("套用安全性設定失敗", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(PdfApplySecurityResponse.builder()
                            .success(false)
                            .message("套用安全性設定失敗: " + e.getMessage())
                            .build());
        }
    }

    /**
     * 建立新的安全 PDF
     * <p>
     * POST /api/pdf/create-secured
     * <p>
     * 請求體範例：
     * {
     *   "title": "測試安全PDF文件",
     *   "content": "這是一份使用 OpenPDF 產生的加密 PDF 文件。",
     *   "allowPrinting": "Y",
     *   "allowHighQualityPrinting": "Y",
     *   "allowAssembly": "N",
     *   "allowCopy": "N",
     *   "allowScreenReaders": "N",
     *   "allowModifyContents": "N",
     *   "allowModifyAnnotations": "N",
     *   "allowFillIn": "N",
     *   "userPassword": null,
     *   "ownerPassword": "your_password",
     *   "encryptionType": "RC4_128"
     * }
     */
    @Operation(
        summary = "建立新的安全 PDF",
        description = "從零建立一份全新的 PDF 文件，設定標題、內容和完整的安全性設定，返回 Base64 編碼結果。"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "安全 PDF 建立成功",
            content = @Content(schema = @Schema(implementation = PdfCreateSecuredResponse.class))
        ),
        @ApiResponse(
            responseCode = "500",
            description = "安全 PDF 建立失敗",
            content = @Content(schema = @Schema(implementation = PdfCreateSecuredResponse.class))
        )
    })
    @PostMapping("/create-secured")
    public ResponseEntity<PdfCreateSecuredResponse> createSecuredPdf(
            @RequestBody PdfCreateSecuredRequest request) {

        try {
            PdfCreateSecuredResponse response = pdfSecurityService.createSecuredPdfBase64(request);

            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }

        } catch (Exception e) {
            log.error("建立安全 PDF 失敗", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(PdfCreateSecuredResponse.builder()
                            .success(false)
                            .message("建立安全 PDF 失敗: " + e.getMessage())
                            .build());
        }
    }

    /**
     * 健康檢查
     * <p>
     * POST /api/pdf/health
     * <p>
     * 請求體：{} (空的 JSON 物件即可)
     */
    @Operation(
        summary = "服務健康檢查",
        description = "檢查服務是否正常運作，返回服務名稱、版本和時間戳記。可用於監控系統或負載均衡器健康檢查。"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "服務正常運作",
            content = @Content(schema = @Schema(implementation = PdfHealthCheckResponse.class))
        )
    })
    @PostMapping("/health")
    public ResponseEntity<PdfHealthCheckResponse> health() {
        PdfHealthCheckResponse response = PdfHealthCheckResponse.builder()
                .status("UP")
                .serviceName("PDF Factory")
                .version("1.0.0")
                .timestamp(System.currentTimeMillis())
                .build();

        return ResponseEntity.ok(response);
    }
}

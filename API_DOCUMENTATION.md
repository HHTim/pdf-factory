# PDF Factory API 文檔

## API 設計原則

✅ **所有 API 均使用 POST mapping**
✅ **所有傳入參數均使用 @RequestBody**
✅ **檔案傳輸使用 Base64 編碼**
✅ **每個 API 都有專屬的 Request 和 Response DTO**

## 互動式 API 文檔

### Swagger UI
啟動應用程式後，訪問以下網址使用互動式 API 文檔：

- **Swagger UI**: http://localhost:8080/swagger-ui/index.html
- **OpenAPI JSON**: http://localhost:8080/v3/api-docs
- **OpenAPI YAML**: http://localhost:8080/v3/api-docs.yaml

Swagger UI 提供：
- 完整的 API 說明和範例
- 線上測試所有端點
- 自動生成的請求/響應格式
- 互動式參數輸入

---

## API 端點總覽

### PDF Rewriter Service（重寫服務）

#### 1. 重寫 PDF（檔案路徑）
- **端點**: `POST /api/pdf/rewrite`
- **Request DTO**: `PdfRewriteRequest`
- **Response DTO**: `PdfRewriteResponse`
- **功能說明**:
  - 讀取本地 PDF 文件並使用 OpenPDF 重新生成
  - 移除原始 PDF 中的 iText 字樣和 metadata
  - 可選擇保留原始安全性設定（加密等級、權限）
  - 支援設定新的 owner/user 密碼
  - 返回重寫前後的安全性資訊供比對

**Request 範例:**
```json
{
  "inputPath": "/path/to/input.pdf",
  "outputPath": "/path/to/output.pdf",
  "ownerPassword": "original_password",
  "userPassword": null,
  "newOwnerPassword": "new_password",
  "newUserPassword": null,
  "preserveSecurity": true,
  "removeSecurity": false
}
```

**Response 範例:**
```json
{
  "success": true,
  "message": "PDF 重寫成功",
  "outputPath": "/path/to/output.pdf",
  "fileSize": 245678,
  "originalSecurityInfo": {
    "encrypted": true,
    "encryptionLevel": 128,
    "encryptionAlgorithm": "128-bit RC4",
    "allowPrinting": true,
    "allowCopy": false
  },
  "newSecurityInfo": {
    "encrypted": true,
    "encryptionLevel": 128,
    "encryptionAlgorithm": "128-bit AES"
  }
}
```

#### 2. 上傳並重寫 PDF
- **端點**: `POST /api/pdf/upload-and-rewrite`
- **Request DTO**: `PdfUploadAndRewriteRequest`
- **Response DTO**: `PdfUploadAndRewriteResponse`
- **功能說明**:
  - 接收 Base64 編碼的 PDF 文件
  - 在伺服器建立臨時檔案處理
  - 使用 OpenPDF 重新生成 PDF，移除 iText 字樣
  - 可選擇保留原始安全性設定
  - 返回 Base64 編碼的重寫結果
  - 自動清理臨時檔案

**Request 範例:**
```json
{
  "pdfFileBase64": "JVBERi0xLjQKJeLjz9MK...",
  "fileName": "document.pdf",
  "ownerPassword": "your_password",
  "userPassword": null,
  "preserveSecurity": true
}
```

**Response 範例:**
```json
{
  "success": true,
  "message": "PDF 重寫成功",
  "pdfFileBase64": "JVBERi0xLjQK...",
  "fileSize": 245678,
  "originalSecurityInfo": {
    "encrypted": true,
    "allowPrinting": true,
    "allowCopy": false
  },
  "newSecurityInfo": {
    "encrypted": true,
    "allowPrinting": true,
    "allowCopy": false
  }
}
```

#### 3. 查詢 PDF 安全性資訊
- **端點**: `POST /api/pdf/security-info`
- **Request DTO**: `PdfSecurityInfoRequest`
- **Response DTO**: `PdfSecurityInfo`
- **功能說明**:
  - 讀取 PDF 文件的完整安全性資訊
  - 解析加密等級和演算法（RC4/AES 40/128/256-bit）
  - 提取所有權限設定（列印、複製、修改等）
  - 讀取 PDF metadata（Creator、Producer、版本）
  - 判斷是否設定了 owner/user 密碼

**Request 範例:**
```json
{
  "filePath": "/path/to/file.pdf",
  "password": "your_password"
}
```

**Response 範例:**
```json
{
  "encrypted": true,
  "encryptionLevel": 128,
  "encryptionAlgorithm": "128-bit AES",
  "permissions": 2100,
  "allowPrinting": true,
  "allowDegradedPrinting": true,
  "allowModifyContents": false,
  "allowCopy": false,
  "allowModifyAnnotations": false,
  "allowFillIn": false,
  "allowScreenReaders": false,
  "allowAssembly": false,
  "pdfVersion": "1.4",
  "creator": "Adobe Acrobat",
  "producer": "iText",
  "hasUserPassword": true,
  "hasOwnerPassword": true
}
```

---

### PDF Security Service（安全性服務）

#### 4. 套用安全性到 PDF
- **端點**: `POST /api/pdf/apply-security`
- **Request DTO**: `PdfApplySecurityRequest`
- **Response DTO**: `PdfApplySecurityResponse`
- **功能說明**:
  - 接收 Base64 編碼的現有 PDF 文件
  - 套用自訂的安全性設定（權限控制）
  - 設定加密演算法（RC4_40/RC4_128/AES_128）
  - 設定 owner/user 密碼
  - 返回 Base64 編碼的加密 PDF

**Request 範例:**
```json
{
  "pdfFileBase64": "JVBERi0xLjQKJeLjz9MK...",
  "fileName": "document.pdf",
  "allowPrinting": "Y",
  "allowHighQualityPrinting": "Y",
  "allowAssembly": "N",
  "allowCopy": "N",
  "allowScreenReaders": "N",
  "allowModifyContents": "N",
  "allowModifyAnnotations": "N",
  "allowFillIn": "N",
  "userPassword": null,
  "ownerPassword": "test123456",
  "encryptionType": "RC4_128"
}
```

**Response 範例:**
```json
{
  "success": true,
  "message": "安全性設定套用成功",
  "pdfFileBase64": "JVBERi0xLjQK...",
  "fileSize": 267890
}
```

#### 5. 建立新的安全 PDF
- **端點**: `POST /api/pdf/create-secured`
- **Request DTO**: `PdfCreateSecuredRequest`
- **Response DTO**: `PdfCreateSecuredResponse`
- **功能說明**:
  - 從零建立一份全新的 PDF 文件
  - 設定文件標題和內容
  - 套用完整的安全性設定
  - 設定加密演算法和密碼
  - 返回 Base64 編碼的加密 PDF

**Request 範例:**
```json
{
  "title": "測試安全PDF文件",
  "content": "這是一份使用 OpenPDF 產生的加密 PDF 文件。",
  "allowPrinting": "Y",
  "allowHighQualityPrinting": "Y",
  "allowAssembly": "N",
  "allowCopy": "N",
  "allowScreenReaders": "N",
  "allowModifyContents": "N",
  "allowModifyAnnotations": "N",
  "allowFillIn": "N",
  "userPassword": null,
  "ownerPassword": "test123456",
  "encryptionType": "RC4_128"
}
```

**Response 範例:**
```json
{
  "success": true,
  "message": "安全 PDF 建立成功",
  "pdfFileBase64": "JVBERi0xLjQK...",
  "fileSize": 15678
}
```

---

### Health Check

#### 6. 健康檢查
- **端點**: `POST /api/pdf/health`
- **Request DTO**: `{}` (空 JSON)
- **Response DTO**: `PdfHealthCheckResponse`
- **功能說明**:
  - 檢查服務是否正常運作
  - 返回服務名稱、版本和時間戳記
  - 可用於監控系統或負載均衡器健康檢查

**Request 範例:**
```json
{}
```

**Response 範例:**
```json
{
  "status": "UP",
  "serviceName": "PDF Factory",
  "version": "1.0.0",
  "timestamp": 1701234567890
}
```

---

## DTO 結構總覽

### Request DTOs
1. `PdfRewriteRequest` - 重寫 PDF 請求
2. `PdfUploadAndRewriteRequest` - 上傳並重寫請求（包含 Base64）
3. `PdfSecurityInfoRequest` - 查詢安全性資訊請求
4. `PdfApplySecurityRequest` - 套用安全性請求（包含 Base64）
5. `PdfCreateSecuredRequest` - 建立安全 PDF 請求

### Response DTOs
1. `PdfRewriteResponse` - 重寫 PDF 響應
2. `PdfUploadAndRewriteResponse` - 上傳並重寫響應（包含 Base64）
3. `PdfSecurityInfo` - 安全性資訊
4. `PdfApplySecurityResponse` - 套用安全性響應（包含 Base64）
5. `PdfCreateSecuredResponse` - 建立安全 PDF 響應（包含 Base64）
6. `PdfHealthCheckResponse` - 健康檢查響應

### 共用 DTOs
- `PdfSecuritySettingsDTO` - 安全性設定（內部使用）

---

## 權限控制參數說明

所有權限參數均接受 `"Y"` / `"N"` 字串：

| 參數 | 說明 |
|------|------|
| `allowPrinting` | 是否允許列印 |
| `allowHighQualityPrinting` | 是否允許高品質列印 |
| `allowAssembly` | 是否允許文件組合 |
| `allowCopy` | 是否允許內容複製 |
| `allowScreenReaders` | 是否允許螢幕閱讀器存取 |
| `allowModifyContents` | 是否允許修改內容 |
| `allowModifyAnnotations` | 是否允許修改注釋 |
| `allowFillIn` | 是否允許填寫表格 |

## 加密類型

| 類型 | 說明 |
|------|------|
| `RC4_40` | 40-bit RC4 加密 |
| `RC4_128` | 128-bit RC4 加密（預設） |
| `AES_128` | 128-bit AES 加密 |
| `AES_256` | 256-bit AES 加密（會降級至 AES_128） |

---

## 測試

使用 Postman 導入 `postman-collection.json` 進行測試。

所有 API 範例已配置完成，包含根據圖片設定的安全性參數。

# PDF Factory

PDF Factory 是一個使用 OpenPDF 的專業 PDF 處理服務，提供兩大核心功能：

## 功能特色

### 1. PDF 重寫服務 (PDF Rewriter Service)
- 讀取現有的 PDF 文件
- 移除 iText 相關元數據
- 使用 OpenPDF 重新生成 PDF
- **保留原始安全性設定**（加密等級、權限控制）

### 2. PDF 安全性服務 (PDF Security Service)
- 根據傳入參數生成新的安全 PDF
- 靈活的權限控制（列印、複製、修改等）
- 支援多種加密方式：RC4 40/128-bit、AES 128-bit
- 可對現有 PDF 套用自訂安全性設定

## 技術架構

- **框架**: Spring Boot 3.2.0
- **Java 版本**: 17
- **PDF 函式庫**: OpenPDF 1.3.34
- **授權**: LGPL/MPL（商業友善）
- **Package**: `com.pdffactory`

## 專案結構

```
src/main/java/com/pdffactory/
├── PdfFactoryApplication.java          # 主程式
├── controller/
│   └── PdfController.java              # REST API 端點
├── dto/
│   ├── PdfRewriteRequest.java          # 重寫請求 DTO
│   ├── PdfRewriteResponse.java         # 重寫響應 DTO
│   ├── PdfSecurityInfo.java            # 安全性資訊 DTO
│   └── PdfSecuritySettingsDTO.java     # 安全性設定 DTO
├── service/
│   ├── PdfReaderService.java           # PDF 讀取服務
│   ├── PdfRewriterService.java         # PDF 重寫服務
│   └── PdfSecurityService.java         # PDF 安全性服務
└── exception/
    └── PdfProcessingException.java     # 自訂異常
```

## API 端點

**重要**: 所有 API 均使用 POST method，所有參數均透過 @RequestBody 傳遞

### PDF Rewriter Service（PDF 重寫服務）
- `POST /api/pdf/rewrite` - 重寫 PDF（檔案路徑）
  - 讀取本地 PDF 並使用 OpenPDF 重新生成
  - 移除 iText 字樣和 metadata
  - 可保留原始安全性設定或套用新的安全性設定

- `POST /api/pdf/upload-and-rewrite` - 上傳並重寫 PDF
  - 接收 Base64 編碼的 PDF
  - 重寫後返回 Base64 結果
  - 自動清理臨時檔案

- `POST /api/pdf/security-info` - 查詢 PDF 安全性資訊
  - 讀取 PDF 完整安全性資訊
  - 解析加密等級、演算法和權限設定
  - 提取 metadata（Creator、Producer、版本）

### PDF Security Service（PDF 安全性服務）
- `POST /api/pdf/apply-security` - 套用安全性到上傳的 PDF
  - 接收 Base64 PDF 並套用自訂安全性設定
  - 支援多種加密演算法（RC4_40/RC4_128/AES_128）
  - 設定細緻的權限控制

- `POST /api/pdf/create-secured` - 建立新的安全 PDF
  - 從零建立加密的 PDF 文件
  - 設定標題、內容和完整安全性設定
  - 返回 Base64 編碼結果

### Health Check
- `POST /api/pdf/health` - 服務健康檢查
  - 檢查服務運作狀態
  - 返回服務名稱、版本和時間戳記

## 快速開始

### 編譯專案
```bash
mvn clean install
```

### 運行專案
```bash
mvn spring-boot:run
```

或使用 IDE（IntelliJ IDEA / Eclipse）直接運行 `PdfFactoryApplication`

### 測試 API

#### 方法 1: 使用 Swagger UI（推薦）
應用程式啟動後，開啟瀏覽器訪問：

```
http://localhost:8080/swagger-ui/index.html
```

Swagger UI 提供：
- 互動式 API 文檔
- 線上測試所有 API
- 自動生成的請求/響應範例
- 完整的參數說明

#### 方法 2: 使用 Postman
導入 `postman-collection.json` 進行測試。

#### 方法 3: 使用 curl
參考 `example-usage.sh` 腳本範例。

## Swagger UI 使用說明

### 訪問 Swagger UI
啟動應用程式後，訪問以下網址：
- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`
- OpenAPI YAML: `http://localhost:8080/v3/api-docs.yaml`

### 使用 Swagger UI 測試 API

1. **選擇 API 端點**
   - 點擊任一 API 端點展開詳細資訊
   - 查看請求參數、響應格式和範例

2. **執行測試**
   - 點擊 "Try it out" 按鈕
   - 填寫請求參數（Swagger 會提供範例）
   - 點擊 "Execute" 執行請求
   - 查看響應結果

3. **測試範例：建立安全 PDF**
   ```json
   {
     "title": "測試文件",
     "content": "這是測試內容",
     "allowPrinting": "Y",
     "allowCopy": "N",
     "ownerPassword": "test123",
     "encryptionType": "AES_128"
   }
   ```

4. **下載生成的 PDF**
   - 複製響應中的 `pdfFileBase64` 欄位
   - 使用線上工具或指令將 Base64 解碼為 PDF 檔案
   ```bash
   echo "base64字串" | base64 -d > output.pdf
   ```

## IDE 設定

### IntelliJ IDEA
1. 開啟專案
2. 啟用 Annotation Processing：
   - Settings → Build, Execution, Deployment → Compiler → Annotation Processors
   - 勾選 "Enable annotation processing"
3. 安裝 Lombok Plugin（如未安裝）

### Eclipse
1. 執行 `java -jar lombok.jar` 安裝 Lombok
2. 重啟 Eclipse
3. 開啟專案

## 安全性設定範例

根據參數產生 PDF（禁止所有操作除了列印）:
```json
{
  "allowPrinting": "Y",
  "allowHighQualityPrinting": "Y",
  "allowAssembly": "N",
  "allowCopy": "N",
  "allowScreenReaders": "N",
  "allowModifyContents": "N",
  "allowModifyAnnotations": "N",
  "allowFillIn": "N",
  "userPassword": null,
  "ownerPassword": "your_password",
  "encryptionType": "RC4_128"
}
```

## 授權

OpenPDF 採用 LGPL 2.1 / MPL 2.0 雙授權，商業閉源專案可免費使用。

## 聯絡資訊

- 專案名稱: PDF Factory
- Version: 1.0.0
- Port: 8080

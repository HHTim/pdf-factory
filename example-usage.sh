#!/bin/bash

# PDF Factory - 使用範例腳本

echo "==================================="
echo "PDF Factory - 使用範例"
echo "==================================="
echo ""

# 設定變數
BASE_URL="http://localhost:8080/api/pdf"
INPUT_PDF="/path/to/your/input.pdf"
OUTPUT_PDF="/path/to/your/output.pdf"
PASSWORD="your-password"

echo "請確保應用程式已啟動在 http://localhost:8080"
echo ""

# 1. 健康檢查
echo "1. 健康檢查..."
curl -s -X POST "${BASE_URL}/health" \
  -H "Content-Type: application/json" \
  -d '{}' | jq .
echo ""
echo ""

# 2. 查詢 PDF 安全性資訊
echo "2. 查詢 PDF 安全性資訊..."
curl -s -X POST "${BASE_URL}/security-info" \
  -H "Content-Type: application/json" \
  -d "{
    \"filePath\": \"${INPUT_PDF}\",
    \"password\": \"${PASSWORD}\"
  }" | jq .
echo ""
echo ""

# 3. 重寫 PDF
echo "3. 重寫 PDF..."
curl -s -X POST "${BASE_URL}/rewrite" \
  -H "Content-Type: application/json" \
  -d "{
    \"inputPath\": \"${INPUT_PDF}\",
    \"outputPath\": \"${OUTPUT_PDF}\",
    \"ownerPassword\": \"${PASSWORD}\",
    \"preserveSecurity\": true
  }" | jq .
echo ""
echo ""

# 4. 上傳並重寫 PDF (使用 Base64 編碼)
echo "4. 上傳並重寫 PDF..."
# 將 PDF 檔案編碼為 Base64
PDF_BASE64=$(base64 -i "${INPUT_PDF}")
curl -s -X POST "${BASE_URL}/upload-and-rewrite" \
  -H "Content-Type: application/json" \
  -d "{
    \"pdfFileBase64\": \"${PDF_BASE64}\",
    \"fileName\": \"$(basename ${INPUT_PDF})\",
    \"ownerPassword\": \"${PASSWORD}\",
    \"userPassword\": null,
    \"preserveSecurity\": true
  }" | jq -r '.pdfFileBase64' | base64 -d > "rewritten_$(basename ${INPUT_PDF})"
echo "已下載重寫後的 PDF: rewritten_$(basename ${INPUT_PDF})"
echo ""

echo "==================================="
echo "完成！"
echo "==================================="

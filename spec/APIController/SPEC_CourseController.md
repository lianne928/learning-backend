# 規格文件 - CourseController

來源檔案: `src/main/java/com/learning/api/controller/CourseController.java`

Base URL: `http://localhost:8080`

---

## 概述

處理課程（Course）完整 CRUD 的 REST API，包含課程查詢、建立、更新及刪除。

---

## API 互動邏輯

### 1. 取得所有課程

* **請求資訊（HTTP Request）**
- Method: `GET`
- URL: `/api/courses`
- Payload: 無

* **回應內容 (Response)**
- HTTP Status: `200 OK`
- Body: `List<CourseResp>`（課程陣列）

---

### 2. 取得單一課程

* **請求資訊（HTTP Request）**
- Method: `GET`
- URL: `/api/courses/{id}`
- Payload: 無

* **回應內容 (Response)**
- HTTP Status: `200 OK`（成功）
- Body: `CourseResp` 物件
- HTTP Status: `404 Not Found`（課程不存在）
- Body:
```json
{
  "msg": "課程不存在"
}
```

---

### 3. 取得老師所有課程（不分上下架）

* **請求資訊（HTTP Request）**
- Method: `GET`
- URL: `/api/courses/tutor/{tutorId}`
- Payload: 無

* **回應內容 (Response)**
- HTTP Status: `200 OK`
- Body: `List<Course>`（該老師所有課程陣列）

---

### 4. 取得老師已上架課程

* **請求資訊（HTTP Request）**
- Method: `GET`
- URL: `/api/courses/tutor/{tutorId}/active`
- Payload: 無

* **回應內容 (Response)**
- HTTP Status: `200 OK`
- Body: `List<Course>`（`active=true` 的課程陣列）

---

### 5. 建立課程

* **請求資訊（HTTP Request）**
- Method: `POST`
- URL: `/api/courses`
- Headers: `Content-Type: application/json`
- Payload (Request Body):
```json
{
  "tutorId": 2,
  "name": "初級兒童美語",
  "subject": 11,
  "price": 700,
  "description": "本課程教學設計有趣，激發孩子對口說的信心",
  "active": true
}
```

| 欄位 | 型別 | 必填 | 說明 |
|---|---|---|---|
| `tutorId` | Long | 是 | 老師 ID（必須為 role=2 的使用者） |
| `name` | String | 是 | 課程名稱（不可為空白） |
| `subject` | Integer | 是 | 科目代碼（需為有效代碼） |
| `price` | Integer | 是 | 課程單價（須 > 0） |
| `description` | String | 否 | 課程描述 |
| `active` | Boolean | 否 | 是否上架 |

* **回應內容 (Response)**
- HTTP Status: `200 OK`（成功）
- Body:
```json
{
  "msg": "ok"
}
```
- HTTP Status: `400 Bad Request`（tutorId 缺失、名稱空白、科目無效、價格為 0 或非老師用戶）
- Body:
```json
{
  "msg": "建立失敗"
}
```

---

### 6. 更新課程

* **請求資訊（HTTP Request）**
- Method: `PUT`
- URL: `/api/courses/{id}`
- Headers: `Content-Type: application/json`
- Payload (Request Body):
```json
{
  "name": "進階英語口說",
  "subject": 12,
  "price": 900,
  "active": false
}
```

| 欄位 | 型別 | 必填 | 說明 |
|---|---|---|---|
| `name` | String | 否 | 課程名稱（不可為空白） |
| `subject` | Integer | 否 | 科目代碼（需為有效代碼） |
| `price` | Integer | 否 | 課程單價 |
| `active` | Boolean | 否 | 是否上架 |

* **回應內容 (Response)**
- HTTP Status: `200 OK`（成功）
- Body: 更新後的 `CourseResp` 物件
- HTTP Status: `404 Not Found`（課程不存在）
- HTTP Status: `400 Bad Request`（名稱空白或科目代碼無效）
- Body:
```json
{
  "message": "驗證失敗: （錯誤原因）"
}
```

---

### 7. 刪除課程

* **請求資訊（HTTP Request）**
- Method: `DELETE`
- URL: `/api/courses/{id}`
- Payload: 無

* **回應內容 (Response)**
- HTTP Status: `204 No Content`（成功）
- HTTP Status: `404 Not Found`（課程不存在）

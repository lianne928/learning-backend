# 規格文件 - Teacher API（TeacherController / TutorProfileController / TutorScheduleController / TutorFeedbackController）

來源檔案:
- `src/main/java/com/learning/api/controller/TeacherController.java`
- `src/main/java/com/learning/api/controller/TutorProfileController.java`
- `src/main/java/com/learning/api/controller/TutorScheduleController.java`
- `src/main/java/com/learning/api/controller/TutorFeedbackController.java`

Base URL: `http://localhost:8080`

---

## 概述

老師（Tutor）相關的 REST API，涵蓋課程管理、個人檔案 CRUD、排班切換及課後回饋提交。

---

## API 互動邏輯

### 1. 新增課程（TeacherController）

* **請求資訊（HTTP Request）**
- Method: `POST`
- URL: `/api/teacher/courses`
- Headers: `Content-Type: application/json`
- Payload (Request Body):
```json
{
  "tutorId": 2,
  "name": "初級兒童美語",
  "subject": 1,
  "level": 5,
  "price": 700,
  "description": "本課程教學設計有趣，激發孩子對口說的信心",
  "active": true
}
```

| 欄位 | 型別 | 必填 | 說明 |
|---|---|---|---|
| `tutorId` | Long | 是 | 老師 ID（開發測試用，正式版改由登入資訊取得） |
| `name` | String | 是 | 課程名稱 |
| `subject` | Integer | 否 | 科目代碼 |
| `level` | Integer | 否 | 難度等級 |
| `price` | Integer | 否 | 課程單價（元） |
| `description` | String | 否 | 課程描述 |
| `active` | Boolean | 否 | 是否上架 |

* **回應內容 (Response)**
- HTTP Status: `200 OK`（成功）
- Body:
```json
{
  "message": "課程新增成功！學生現在可以購買了！"
}
```
- HTTP Status: `400 Bad Request`（資料格式錯誤或價格異常）
- Body:
```json
{
  "message": "新增課程失敗，請檢查資料格式或價格"
}
```

---

## TutorProfileController — 老師個人檔案 CRUD

Base URL: `/api/teacher/profile`

### TutorProfileDTO 欄位說明

| 欄位 | 型別 | 必填 | 說明 |
|---|---|---|---|
| `tutorId` | Long | 是（POST/PUT） | 老師 ID |
| `name` | String | 否 | 更新 users 表的姓名 |
| `title` | String | 否 | 老師頭銜（例：資深英文老師） |
| `avatar` | String | 否 | 頭像圖片 URL |
| `intro` | String | 否 | 自我介紹 |
| `education` | String | 否 | 學歷（例：國立台灣大學 碩士） |
| `certificate1` | String | 否 | 第一張證照圖片 URL |
| `certificateName1` | String | 否 | 第一張證照名稱 |
| `certificate2` | String | 否 | 第二張證照圖片 URL |
| `certificateName2` | String | 否 | 第二張證照名稱 |
| `videoUrl1` | String | 否 | 第一支自我介紹影片 URL |
| `videoUrl2` | String | 否 | 第二支影片 URL |
| `bankCode` | String | 否 | 銀行代碼 |
| `bankAccount` | String | 否 | 銀行帳號 |

---

### 2. 取得老師個人檔案（TutorProfileController）

* **請求資訊（HTTP Request）**
- Method: `GET`
- URL: `/api/teacher/profile/{tutorId}`
- Payload: 無

* **回應內容 (Response)**
- HTTP Status: `200 OK`（成功）
- Body: `Tutor` 實體（包含所有個人檔案欄位）
- HTTP Status: `404 Not Found`（找不到）
- Body:
```json
{
  "msg": "找不到該名老師的個人檔案"
}
```

---

### 3. 建立老師個人檔案（TutorProfileController）

* **請求資訊（HTTP Request）**
- Method: `POST`
- URL: `/api/teacher/profile`
- Headers: `Content-Type: application/json`
- Payload (Request Body): `TutorProfileDTO`（見欄位說明）

* **回應內容 (Response)**
- HTTP Status: `201 Created`（成功）
- Body:
```json
{
  "msg": "個人檔案建立成功！"
}
```
- HTTP Status: `400 Bad Request`（未提供 tutorId）
- Body:
```json
{
  "message": "必須提供老師 ID"
}
```
- HTTP Status: `404 Not Found`（找不到該名老師）
- Body:
```json
{
  "msg": "（錯誤原因）"
}
```
- HTTP Status: `409 Conflict`（個人檔案已存在）
- Body:
```json
{
  "msg": "（已存在的說明）"
}
```

* **限制**：每位老師只能建立一筆個人檔案，若已存在請使用 PUT 更新。

---

### 4. 更新老師個人檔案（TutorProfileController）

* **請求資訊（HTTP Request）**
- Method: `PUT`
- URL: `/api/teacher/profile`
- Headers: `Content-Type: application/json`
- Payload (Request Body): `TutorProfileDTO`（見欄位說明）

* **回應內容 (Response)**
- HTTP Status: `200 OK`（成功）
- Body:
```json
{
  "msg": "個人檔案更新成功！"
}
```
- HTTP Status: `400 Bad Request`（未提供 tutorId）
- Body:
```json
{
  "msg": "必須提供老師 ID"
}
```
- HTTP Status: `404 Not Found`（找不到該名老師）
- Body:
```json
{
  "msg": "更新失敗，找不到該名老師"
}
```

---

### 5. 刪除老師個人檔案（TutorProfileController）

* **請求資訊（HTTP Request）**
- Method: `DELETE`
- URL: `/api/teacher/profile/{tutorId}`
- Payload: 無

* **回應內容 (Response)**
- HTTP Status: `200 OK`（成功）
- Body:
```json
{
  "msg": "個人檔案已成功刪除！"
}
```
- HTTP Status: `404 Not Found`（找不到）
- Body:
```json
{
  "msg": "（錯誤原因）"
}
```

---

## TutorScheduleController — 排班管理

Base URL: `/api/teacher/schedules`

### 6. 切換時段狀態（開放 / 關閉）（TutorScheduleController）

* **請求資訊（HTTP Request）**
- Method: `POST`
- URL: `/api/teacher/schedules/toggle`
- Headers: `Content-Type: application/json`
- Payload (Request Body): `ScheduleDTO.ToggleReq`
```json
{
  "tutorId": 2,
  "dayOfWeek": 1,
  "startTime": "10:00",
  "endTime": "11:00"
}
```

* **回應內容 (Response)**
- HTTP Status: `200 OK`（成功）
- Body:
```json
{
  "msg": "時段狀態已更新"
}
```
- HTTP Status: `400 Bad Request`（時間格式錯誤或其他失敗）
- Body:
```json
{
  "msg": "（錯誤原因）"
}
```

---

### 7. 取得老師所有排班（TutorScheduleController）

* **請求資訊（HTTP Request）**
- Method: `GET`
- URL: `/api/teacher/schedules/{tutorId}`
- Payload: 無

* **回應內容 (Response)**
- HTTP Status: `200 OK`
- Body: `List<ScheduleDTO.Res>`（直接回傳陣列）
```json
[
  {
    "id": 1,
    "tutorId": 2,
    "dayOfWeek": 1,
    "startTime": "10:00",
    "endTime": "11:00",
    "available": true
  }
]
```

---

### 8. 老師送出課後回饋（TutorFeedbackController）

* **請求資訊（HTTP Request）**
- Method: `POST`
- URL: `/api/teacher/feedbacks`
- Headers: `Content-Type: application/json`
- Payload (Request Body): `LessonFeedback` 實體
```json
{
  "bookingId": 10,
  "rating": 4,
  "comment": "學生今天表現不錯，已達學習目標"
}
```

| 欄位 | 型別 | 必填 | 說明 |
|---|---|---|---|
| `bookingId` | Long | 是 | 所屬 Booking 的 ID |
| `rating` | Integer | 是 | 評分（1-5） |
| `comment` | String | 否 | 課後回饋內容 |

* **回應內容 (Response)**
- HTTP Status: `200 OK`（成功）
- Body:
```json
{
  "message": "課後回饋送出成功！家長將會收到通知。"
}
```
- HTTP Status: `400 Bad Request`（評分不在範圍 / 已重複填寫）
- Body:
```json
{
  "message": "評分必須介於 1 到 5 之間"
}
```
```json
{
  "message": "這堂課已經填寫過回饋囉！"
}
```

* **限制**：每個 `bookingId` 只能提交一次課後回饋。

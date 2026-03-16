# API Reference — Learning Backend

Base URL: `http://localhost:8080`

認證方式：`Authorization: Bearer <JWT token>`（無標示 🔓 的端點皆需要）

---

## 認證 Auth

### POST /api/auth/register 🔓

註冊新使用者。

**Request Body**
```json
{
  "name": "王小明",
  "email": "wang@example.com",
  "password": "secret123",
  "birthday": "2000-01-15",
  "role": 1
}
```

| 欄位     | 類型        | 說明                         |
|----------|------------|------------------------------|
| name     | string     | 顯示名稱，最長 100 字           |
| email    | string     | 唯一，作為登入帳號              |
| password | string     | 明文密碼，後端以 BCrypt 雜湊儲存 |
| birthday | date       | `yyyy-MM-dd`，可為 null        |
| role     | int        | 1=學生, 2=老師, 3=管理員        |

**Response 200**
```json
{ "message": "register success" }
```

**Response 400** — email 已存在或資料驗證失敗

---

### POST /api/auth/login 🔓

登入並取得 JWT token。

**Request Body**
```json
{
  "email": "wang@example.com",
  "password": "secret123"
}
```

**Response 200**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "user": {
    "id": 1,
    "name": "王小明",
    "email": "wang@example.com",
    "role": 1,
    "wallet": 5000
  }
}
```

**Response 400** — 帳號或密碼錯誤

---

## 課程 Courses

### GET /api/courses 🔓

取得所有課程（含平均評分）。以批次查詢防止 N+1。

**Response 200**
```json
[
  {
    "id": 1,
    "tutorId": 10,
    "name": "英文會話入門",
    "subject": 11,
    "description": "適合初學者...",
    "price": 600,
    "active": true,
    "averageRating": 4.5
  }
]
```

`subject` 對應：11-13=各年級, 21-23=考試/認證, 31=其他

---

### GET /api/courses/{id} 🔓

取得單一課程。

**Response 200** — CourseResp（同上）
**Response 404** — 課程不存在

---

### GET /api/courses/tutor/{tutorId} 🔓

取得指定老師的所有課程。

---

### GET /api/courses/tutor/{tutorId}/active 🔓

取得指定老師的上架中課程。

---

### POST /api/courses

建立課程。

**Request Body**
```json
{
  "tutorId": 10,
  "name": "英文會話入門",
  "subject": 11,
  "description": "課程描述...",
  "price": 600,
  "active": true
}
```

**Response 200** — `{ "message": "success" }`

---

### PUT /api/courses/{id}

更新課程資訊。

**Request Body** — 同 POST，全部欄位更新
**Response 200** — 更新後的 Course 物件

---

### DELETE /api/courses/{id}

刪除課程。

**Response 204** — 成功
**Response 404** — 課程不存在

---

## 結帳 Checkout

### POST /api/shop/purchase

購買課程時段。這是系統中最關鍵的交易端點，以 `@Transactional` 確保原子性。

**Request Body**
```json
{
  "studentId": 1,
  "courseId": 5,
  "selectedSlots": [
    { "date": "2026-03-20", "hour": 14 },
    { "date": "2026-03-27", "hour": 14 }
  ]
}
```

**驗證邏輯（依序）：**
1. 課程必須存在且為 active
2. 學生錢包餘額 ≥ 總金額
3. 每個時段的 TutorSchedule 必須為 `available`
4. 每個時段不得有衝突的 Bookings

**Response 200** — `{ "message": "purchase success" }`

**Response 400**
```json
{
  "message": "insufficient balance",
  "actionCode": "TOP_UP"
}
```
或時段已被預訂的錯誤訊息。

---

## 訂單 Orders

### POST /api/orders

建立訂單（不含時段預約，純訂單）。

**Request Body**
```json
{
  "userId": 1,
  "courseId": 5,
  "lessonCount": 10
}
```

> 10 堂以上自動套用 95 折優惠，`discountPrice` 由系統計算。

**Response 200** — `{ "message": "success" }`

---

### GET /api/orders/{id}

取得單一訂單。

**Response 200**
```json
{
  "id": 1,
  "userId": 1,
  "courseId": 5,
  "unitPrice": 600,
  "discountPrice": 5700,
  "lessonCount": 10,
  "lessonUsed": 2,
  "status": 2
}
```

`status`: 1=待付款, 2=已成立, 3=已完成

---

### GET /api/orders/user/{userId}

取得指定使用者的所有訂單。

---

### PUT /api/orders/{id}

更新訂單堂數。

**Request Body**
```json
{
  "lessonCount": 12,
  "lessonUsed": 3
}
```

---

### PATCH /api/orders/{id}/status

更新訂單狀態。

**Request Body**
```json
{ "status": 2 }
```

---

### POST /api/orders/{id}/pay

付款處理。

**Response 200** — `{ "message": "payment success" }`

---

### DELETE /api/orders/{id}

取消訂單（只有狀態為 1=待付款 可取消）。

---

## 老師管理 Tutor

### GET /api/tutor/{id} 🔓

取得老師資料。

**Response 200** — Tutor 物件（含 intro, education, certificate URLs 等）

---

### POST /api/tutor

建立老師申請資料。

**Request Body**
```json
{
  "id": 2,
  "title": "英語教學碩士",
  "avatarUrl": "https://...",
  "intro": "我有 5 年教學經驗...",
  "education": "台灣師範大學英語教育所",
  "certificate1": "https://...",
  "certificateName1": "TESOL 認證",
  "bankCode": "013",
  "bankAccount": "1234567890"
}
```

`status` 預設為 1（申請中），由管理員審核後改為 2（合格）。

---

### PUT /api/tutor/{id}

更新老師資料。

---

### DELETE /api/tutor/{id}

刪除老師資料。

---

## 老師個人檔案 Teacher Profile

> 注意：`/api/teacher/profile` 與 `/api/tutor` 為兩個不同的端點，功能有部分重疊，前者用於老師前台自行管理，後者用於後台。

### GET /api/teacher/profile/{tutorId} 🔓

### POST /api/teacher/profile 🔒 ROLE_TEACHER

**Request Body** — TutorProfileDTO（欄位同 TutorReq）
**Response 201 Created**
**Response 409 Conflict** — 該老師個人檔案已存在

### PUT /api/teacher/profile 🔒 ROLE_TEACHER

### DELETE /api/teacher/profile/{tutorId} 🔒 ROLE_TEACHER

---

## 老師排班 Schedules

### POST /api/teacher/schedules/toggle 🔒 ROLE_TEACHER

切換單一時段的可預約狀態。

**Request Body**
```json
{
  "tutorId": 10,
  "weekday": 2,
  "hour": 14,
  "targetStatus": "available"
}
```

| 欄位         | 說明                          |
|--------------|-------------------------------|
| weekday      | 1=週一 … 7=週日               |
| hour         | 9–21                         |
| targetStatus | `"available"` 或 `"unavailable"` |

**Response 200** — `{ "message": "success" }`
**Response 400** — 時段已被預約，無法切換

---

### GET /api/teacher/schedules/{tutorId} 🔓

取得老師整週排班模板。

**Response 200**
```json
[
  { "weekday": 1, "hour": 9,  "status": "available" },
  { "weekday": 1, "hour": 10, "status": "unavailable" },
  ...
]
```

---

## 課程評論 Reviews

### GET /api/reviews 🔓

取得所有評論。

### GET /api/reviews/{id} 🔓

### GET /api/reviews/user/{userId} 🔓

### GET /api/reviews/course/{courseId} 🔓

### GET /api/reviews/course/{courseId}/average-rating 🔓

**Response 200**
```json
{ "courseId": 5, "averageRating": 4.33 }
```

平均分由 `(focusScore + comprehensionScore + confidenceScore) / 3` 計算。

---

### POST /api/reviews

**Request Body**
```json
{
  "userId": 1,
  "courseId": 5,
  "focusScore": 5,
  "comprehensionScore": 4,
  "confidenceScore": 4,
  "comment": "老師講解清楚"
}
```

所有 score 欄位為必填整數（1–5）。`comment` 最長 1000 字，可為 null。

### PUT /api/reviews/{id}

### DELETE /api/reviews/{id}
**Response 204 No Content**

---

## 課後回饋 Lesson Feedbacks

### GET /api/feedbacks 🔓

### GET /api/feedbacks/{id} 🔓

### GET /api/feedbacks/lesson/{bookingId} 🔓

### GET /api/feedbacks/lesson/{bookingId}/average-rating 🔓

**Response 200** — `{ "bookingId": 42, "averageRating": 4.5 }`

---

### POST /api/feedbacks

**Request Body**
```json
{
  "bookingId": 42,
  "focusScore": 5,
  "comprehensionScore": 4,
  "confidenceScore": 4,
  "comment": "今天學到很多",
  "rating": 5
}
```

`rating` 為 1–5 整數（必填），其他 score 同 Reviews。

### PUT /api/feedbacks/{id}

### DELETE /api/feedbacks/{id}
**Response 204 No Content**

---

### POST /api/teacher/feedbacks 🔒 ROLE_TEACHER

教師提交課後回饋（`TutorFeedbackController`，直接寫入 `LessonFeedbackRepository`）。
與 `/api/feedbacks` 為不同端點：前者由教師端提交，後者提供查詢 / 管理。

**Request Body**
```json
{
  "bookingId": 42,
  "rating": 5,
  "focusScore": 5,
  "comprehensionScore": 4,
  "confidenceScore": 4,
  "comment": "今天進步很多！"
}
```

**Response 200** — `{ "message": "課後回饋送出成功！家長將會收到通知。" }`
**Response 400** — rating 超出 1–5 範圍，或該 booking 已填寫過回饋

---

## 聊天訊息 Chat Messages

### GET /api/chatMessage/booking/{bookingId} 🔓

取得指定 booking 的所有聊天記錄（按時間排序）。

**Response 200**
```json
[
  {
    "id": 1,
    "bookingId": 10,
    "role": 1,
    "messageType": 1,
    "message": "老師好，我今天想練習...",
    "mediaUrl": null,
    "createdAt": "2026-03-14T09:00:00Z"
  }
]
```

`messageType`: 1=文字, 2=貼圖, 3=語音, 4=圖片, 5=影片, 6=檔案
`role`: 1=學生, 2=老師

---

### POST /api/chatMessage

**Request Body**
```json
{
  "bookingId": 10,
  "role": 1,
  "messageType": 1,
  "message": "老師好",
  "mediaUrl": null
}
```

媒體類型（type 2–6）需提供 `mediaUrl`；文字類型（1）需提供 `message`。

**Response 201 Created** — 建立的 ChatMessage 物件

---

### POST /api/chatMessage/upload

上傳多媒體檔案並建立聊天訊息。

**Content-Type:** `multipart/form-data`

| 參數 | 類型 | 說明 |
|------|------|------|
| `file` | MultipartFile | 上傳的檔案（必填） |
| `bookingId` | Long | 所屬 booking（必填） |
| `role` | Integer | 發送角色：1=學生, 2=老師（必填） |

依 MIME type 自動偵測 `messageType`：
- `image/*` → 4 (IMAGE)
- `video/*` → 5 (VIDEO)
- `audio/*` → 3 (VOICE)
- 其他 → 6 (FILE)

**Response 201 Created** — 儲存後的 ChatMessage（`mediaUrl` 設為檔案存取 URL）
**Response 400** — file 為空
**Response 500** — 檔案儲存失敗

---

### PUT /api/chatMessage/{id}

**Request Body**
```json
{ "message": "已修改的訊息" }
```

**Response 200** — 更新後的 ChatMessage
**Response 404** — 訊息不存在

---

### DELETE /api/chatMessage/{id}

**Response 204 No Content**

---

## WebSocket 即時通訊

連線端點：`ws://localhost:8080/ws`（支援 SockJS fallback）

使用 STOMP 協議。

### 訂閱頻道

| Topic                                  | 說明                |
|----------------------------------------|---------------------|
| `/topic/room/{bookingId}/signal`       | WebRTC 訊號（ICE/SDP） |
| `/topic/room/{bookingId}/chat`         | 即時聊天訊息         |
| `/topic/room/{bookingId}/events`       | 加入/離開事件        |

### 發送訊息

| Destination                    | 說明                |
|-------------------------------|---------------------|
| `/app/signal/{bookingId}`     | 傳送 WebRTC 訊號    |
| `/app/chat/{bookingId}`       | 傳送聊天訊息        |
| `/app/event/{bookingId}`      | 傳送房間事件        |

### 訊息格式

**SignalingMessage**（傳送至 `/app/signal/{bookingId}`，轉發至 `/topic/room/{id}/signal`，不持久化）
```json
{
  "type": "offer | answer | candidate",
  "senderRole": 1,
  "sdp": "v=0...",
  "candidate": "candidate:...",
  "sdpMid": "0",
  "sdpMLineIndex": 0
}
```
`sdp` 用於 offer/answer；`candidate`/`sdpMid`/`sdpMLineIndex` 用於 ICE candidate。

**RoomEvent**（傳送至 `/app/event/{bookingId}`，轉發至 `/topic/room/{id}/events`，不持久化）
```json
{
  "type": "joined | left",
  "role": 1,
  "timestamp": "2026-03-15T08:00:00Z"
}
```
`role`: 1=學生, 2=老師。`timestamp` 預設為伺服器端 `Instant.now()`。

---

## 錯誤回應格式

各例外類型與對應 HTTP 狀態碼：

| 例外類型 | HTTP 狀態碼 | 說明 |
|---------|------------|------|
| `NoSuchElementException` | 404 | 資源不存在 |
| `NoResourceFoundException` | 404 | Spring MVC 找不到靜態資源 |
| `IllegalArgumentException` | 400 | 業務邏輯驗證失敗 |
| `MethodArgumentNotValidException` | 400 | Bean Validation 驗證失敗 |
| `Exception`（其他） | 500 | 伺服器錯誤 |

大多數錯誤回傳 `ErrorResponse` 格式：

```json
{
  "message": "具體錯誤說明",
  "timestamp": "2026-03-14T09:00:00Z"
}
```

`MethodArgumentNotValidException`（Bean Validation 失敗）回傳扁平 Map：

```json
{
  "email": "must not be blank",
  "password": "size must be between 6 and 100"
}
```

# 重複功能分析報告

> 分析日期：2026-03-16
> 專案：d:\lianne928

---

## 重複功能總覽

### 1. Tutor 管理重複

兩組 Controller + Service 對同一個 `Tutor` entity 執行相同的 CRUD 操作。

| | TutorService + TutorController | TutorProfileService + TutorProfileController |
|---|---|---|
| 路徑 | `/api/tutor` | `/api/teacher/profile` |
| 操作 | CRUD on Tutor | CRUD on Tutor |
| 欄位映射 | `applyFields()` | `applyDtoToTutor()` — 映射完全相同的 12 個欄位 |

**差異：**
- `TutorService.createTutor()` 有角色驗證 `user.getRole() != 2`
- `TutorProfileService` 會同步更新 `User.name`，並使用 `@Transactional`

**相關檔案：**
- `src/main/java/com/learning/api/service/TutorService.java`
- `src/main/java/com/learning/api/service/TutorProfileService.java`
- `src/main/java/com/learning/api/controller/TutorController.java`
- `src/main/java/com/learning/api/controller/TutorProfileController.java`

---

### 2. 課程建立重複

兩個 Service 都做課程驗證 + 儲存，邏輯幾乎相同。

| | CourseService.sendCourses() | TeacherCourseService.addCourse() |
|---|---|---|
| 路徑 | `POST /api/courses` | `POST /api/teacher/courses` |
| 驗證 tutorId + role==2 | ✅ | ✅ |
| 驗證 price > 0 | ✅ | ✅ |
| 驗證 description 長度 | ✅ | ✅ |
| 驗證 subject codes | ✅ | ✅ |

**相關檔案：**
- `src/main/java/com/learning/api/service/CourseService.java`
- `src/main/java/com/learning/api/service/TeacherCourseService.java`
- `src/main/java/com/learning/api/controller/CourseController.java`
- `src/main/java/com/learning/api/controller/TeacherController.java`

---

### 3. 評分/回饋系統重複

兩個 Entity 儲存幾乎一樣的欄位，且對應的 Service 有相同方法。

| | LessonFeedback | Reviews |
|---|---|---|
| focusScore | ✅ | ✅ |
| comprehensionScore | ✅ | ✅ |
| confidenceScore | ✅ | ✅ |
| comment | ✅ | ✅ |
| 計算平均分 | `getAverageRating()` | `getAverageRating()` — 邏輯相同 |
| 分數驗證 | `validateScore()` | `validateReview()` — 邏輯相同 |

**差異：** 關聯對象不同 — LessonFeedback 綁 `bookingId`，Reviews 綁 `userId + courseId`

**相關檔案：**
- `src/main/java/com/learning/api/entity/LessonFeedback.java`
- `src/main/java/com/learning/api/entity/Reviews.java`
- `src/main/java/com/learning/api/service/LessonFeedbackService.java`
- `src/main/java/com/learning/api/service/ReviewService.java`

---

### 4. 回饋提交重複

兩個 Controller 都提交 `LessonFeedback`，但一個繞過了 Service 層。

| | FeedbackController | TutorFeedbackController |
|---|---|---|
| 路徑 | `POST /api/feedbacks` | `POST /api/teacher/feedbacks` |
| 操作 | 呼叫 LessonFeedbackService | **直接存取 Repository**，繞過 Service 層 |
| rating 1-5 驗證 | Service 內做 | inline 做 |

**相關檔案：**
- `src/main/java/com/learning/api/controller/FeedbackController.java`
- `src/main/java/com/learning/api/controller/TutorFeedbackController.java`

---

### 5. 登入邏輯重複

| | AuthService.loginReq() | MemberService.login() |
|---|---|---|
| 查 email | ✅ | ✅ |
| BCrypt 驗證密碼 | ✅ | ✅ |
| 產生 JWT | ✅ | ✅ |
| 狀態 | AuthController 使用中 | 有實作但**無對應 Controller 暴露（未被使用）** |

**相關檔案：**
- `src/main/java/com/learning/api/service/AuthService.java`
- `src/main/java/com/learning/api/service/MemberService.java`

---

### 6. 訂單建立重複

三個地方都能產生 Order，CheckoutService 未複用 OrderService。

| 路徑 | 方式 |
|---|---|
| `BookingService` | 轉換後呼叫 OrderService |
| `OrderController` | 直接呼叫 OrderService |
| `CheckoutService` | **自己重新實作** Order + Booking 建立邏輯，未複用 OrderService |

**相關檔案：**
- `src/main/java/com/learning/api/service/BookingService.java`
- `src/main/java/com/learning/api/service/OrderService.java`
- `src/main/java/com/learning/api/service/CheckoutService.java`

---

### 7. 分數驗證邏輯散落各處

相同的「分數需介於 1-5」驗證出現在三個不同位置：

| 位置 | 方式 |
|---|---|
| `LessonFeedbackService.validateScore()` | 方法封裝 |
| `ReviewService.validateReview()` | 方法封裝（邏輯相同） |
| `TutorFeedbackController` | inline 寫死 |

---

## 優先修復建議

| 優先級 | 問題 | 建議 |
|---|---|---|
| 🔴 高 | CourseService vs TeacherCourseService | 合併，以角色決定存取權限 |
| 🔴 高 | CheckoutService 自己建 Order | 改為呼叫 OrderService |
| 🟡 中 | TutorService vs TutorProfileService | 合併為一個 Service |
| 🟡 中 | TutorFeedbackController 直接用 Repo | 改為呼叫 LessonFeedbackService |
| 🟡 中 | MemberService.login() 未使用 | 確認是否可刪除 |
| 🟢 低 | 分數驗證邏輯重複 | 抽出共用 validator |
| 🟢 低 | LessonFeedback vs Reviews 欄位相似 | 評估業務需求後考慮合併 |

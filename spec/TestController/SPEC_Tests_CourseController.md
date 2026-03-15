# 測試規格文件 - CourseControllerTest

來源檔案: `src/test/java/com/learning/api/controller/CourseControllerTest.java`

---

## 測試架構

| 項目 | 說明 |
|---|---|
| 測試類型 | 整合測試（`@SpringBootTest` + MockMvc） |
| 交易管理 | `@Transactional`（每個測試後自動 rollback） |
| 前置資料 | `@BeforeEach` 建立 User（老師 role=2）、User（學生 role=1）、一筆基礎 Course |
| 測試數量 | 18 個測試方法 |

---

## 前置資料建立順序

```
User（role=2，老師）→ User（role=1，學生）→ Course（tutorId=老師Id，active=true）
```

---

## 測試案例

### GET /api/courses — 取得所有課程

| 測試方法 | 說明 | 驗證重點 |
|---|---|---|
| `getAll_shouldReturnNonEmptyList` | 前置建立一筆課程後查詢全部 | 回傳 `200`，陣列長度 >= 1，`$[0].id` 存在 |

---

### GET /api/courses/{id} — 取得單一課程

| 測試方法 | 說明 | 驗證重點 |
|---|---|---|
| `getById_existingId_shouldReturn200WithCourse` | 查詢前置建立的課程 | 回傳 `200`，id/name/tutorId/subject/price/active 值符合前置資料 |
| `getById_nonExistingId_shouldReturn404` | id=999999 | 回傳 `404` |

---

### GET /api/courses/tutor/{tutorId} — 取得老師所有課程

| 測試方法 | 說明 | 驗證重點 |
|---|---|---|
| `getByTutorId_shouldReturnAllCoursesForTutor` | 查詢前置老師的課程 | 回傳 `200`，陣列長度 >= 1，`$[0].tutorId` 等於 savedTutorId |

---

### GET /api/courses/tutor/{tutorId}/active — 取得老師上架課程

| 測試方法 | 說明 | 驗證重點 |
|---|---|---|
| `getByTutorIdActive_shouldReturnOnlyActiveCourses` | 額外建立一筆 active=false 的課程，查詢上架課程 | 回傳 `200`，所有元素的 `$.active` 均為 `true` |

---

### POST /api/courses — 建立課程

| 測試方法 | 說明 | 驗證重點 |
|---|---|---|
| `post_validRequest_shouldReturn200WithOkMsg` | 合法 tutorId（role=2）、name、subject=21、price=800 | 回傳 `200`，`$.msg` 為「ok」 |
| `post_missingTutorId_shouldReturn400` | 未提供 tutorId | 回傳 `400`，`$.msg` 為「建立失敗」 |
| `post_emptyName_shouldReturn400` | name 為空白字串（`"   "`） | 回傳 `400`，`$.msg` 為「建立失敗」 |
| `post_invalidSubject_shouldReturn400` | subject=99（無效代碼） | 回傳 `400`，`$.msg` 為「建立失敗」 |
| `post_zeroPrice_shouldReturn400` | price=0 | 回傳 `400`，`$.msg` 為「建立失敗」 |
| `post_nonTutorUser_shouldReturn400` | tutorId 指向 role=1 的學生 | 回傳 `400`，`$.msg` 為「建立失敗」 |

---

### PUT /api/courses/{id} — 更新課程

| 測試方法 | 說明 | 驗證重點 |
|---|---|---|
| `put_existingId_validRequest_shouldReturn200WithUpdatedCourse` | 更新 name/subject/price/active | 回傳 `200`，回應 body 中各欄位值符合更新後資料 |
| `put_nonExistingId_shouldReturn404` | id=999999 | 回傳 `404` |
| `put_emptyName_shouldReturn400WithMessage` | name=`"  "`（空白） | 回傳 `400`，`$.message` 包含「驗證失敗」 |
| `put_invalidSubject_shouldReturn400WithMessage` | subject=99（無效代碼） | 回傳 `400`，`$.message` 包含「驗證失敗」 |

---

### DELETE /api/courses/{id} — 刪除課程

| 測試方法 | 說明 | 驗證重點 |
|---|---|---|
| `delete_existingId_shouldReturn204` | 刪除前置建立的課程 | 回傳 `204 No Content` |
| `delete_nonExistingId_shouldReturn404` | id=999999 | 回傳 `404` |
| `delete_thenGetById_shouldReturn404` | 先 DELETE，再 GET | DELETE 回傳 `204`，後續 GET 回傳 `404` |

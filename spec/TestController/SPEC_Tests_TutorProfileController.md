# 測試規格文件 - TutorProfileControllerTest

來源檔案: `src/test/java/com/learning/api/controller/TutorProfileControllerTest.java`

---

## 測試架構

| 項目 | 說明 |
|---|---|
| 測試類型 | 整合測試（`@SpringBootTest` + MockMvc） |
| 交易管理 | `@Transactional`（每個測試後自動 rollback） |
| 前置資料 | `@BeforeEach` 建立 User（role=2，老師） |
| 測試數量 | 17 個測試方法 |

---

## 前置資料建立順序

```
User（role=2，老師）
```

---

## 測試案例

### GET /api/teacher/profile/{tutorId} — 取得個人檔案

| 測試方法 | 說明 | 驗證重點 |
|---|---|---|
| `get_noProfileExists_shouldReturn404` | 尚未建立個人檔案時查詢 | 回傳 `404`，`$.msg` 包含「找不到」 |
| `get_afterCreate_shouldReturn200WithData` | 先 POST 建立，再 GET 查詢 | 回傳 `200`，title/intro/education/certificate1/certificateName1/bankCode/bankAccount 等欄位符合建立時的值 |

---

### POST /api/teacher/profile — 建立個人檔案

| 測試方法 | 說明 | 驗證重點 |
|---|---|---|
| `post_missingTutorId_shouldReturn400` | Request body 未提供 tutorId | 回傳 `400`，`$.msg` 存在 |
| `post_nonExistentUser_shouldReturn404` | tutorId=999999（不存在的老師） | 回傳 `404`，`$.msg` 包含「找不到」 |
| `post_validData_shouldReturn201` | 合法 tutorId，完整欄位 | 回傳 `201`，`$.msg` 包含「成功」 |
| `post_profileAlreadyExists_shouldReturn409` | 同一 tutorId 第二次 POST | 回傳 `409`，`$.msg` 包含「已存在」 |

---

### PUT /api/teacher/profile — 更新個人檔案

| 測試方法 | 說明 | 驗證重點 |
|---|---|---|
| `put_validRequest_withAllFields_shouldReturn200` | 傳入 name/intro/certificate/video 等欄位 | 回傳 `200`，`$.msg` 包含「個人檔案儲存成功」 |
| `put_missingTutorId_shouldReturn400` | 未傳 tutorId | 回傳 `400`，`$.msg` 為「必須提供老師 ID」 |
| `put_nonExistingTutorId_shouldReturn404` | tutorId=999999 | 回傳 `404`，`$.msg` 為「更新失敗，找不到該名老師」 |
| `put_withNameUpdate_shouldUpdateUserName` | 傳入 name 欄位 | 回傳 `200`，`$.msg` 包含「成功」 |
| `put_updatesAllFields_shouldReturn200AndPersist` | 先 POST 建立，再 PUT 更新 title/education/bankCode，最後 GET 驗證 | 回傳 `200`，GET 確認更新欄位值已生效 |
| `put_withoutName_shouldNotOverwriteUserName` | 僅傳 intro，不傳 name | PUT 成功，User.name 不被覆蓋 |
| `put_upsertsTutorRow_shouldCreateTutorRecord` | 老師尚未有 Tutor 紀錄時執行 PUT | 回傳 `200`，自動建立 Tutor 列，intro 值正確 |
| `put_updatesExistingTutorRow_shouldOverwriteIntro` | 預先建立 Tutor 紀錄（intro="Old intro"），再 PUT | 回傳 `200`，Tutor.intro 更新為新值 |

---

### DELETE /api/teacher/profile/{tutorId} — 刪除個人檔案

| 測試方法 | 說明 | 驗證重點 |
|---|---|---|
| `delete_nonExistentProfile_shouldReturn404` | 尚未建立個人檔案即 DELETE | 回傳 `404`，`$.msg` 包含「找不到」 |
| `delete_existingProfile_shouldReturn200` | 先 POST 建立，再 DELETE | 回傳 `200`，`$.msg` 包含「成功」 |
| `delete_thenGet_shouldReturn404` | 先 POST 建立，DELETE 後再 GET | DELETE 回傳 `200`，後續 GET 回傳 `404` |

# learning-backend 程式現況分析報告

**分析日期：** 2026-03-14
**分析範圍：** `src/main/java/com/learning/api/**`（85 個 Java 檔案）、`pom.xml`、`application.properties`、測試程式碼
**報告版本：** 1.0

---

## 執行摘要

### 整體健康度評分

| 面向 | 評分 | 說明 |
|------|------|------|
| 架構健康度 | ⭐⭐⭐⭐☆ | 分層清晰，Controller→Service→Repository 依賴方向一致 |
| 程式碼品質 | ⭐⭐⭐☆☆ | 主體整潔，但魔法數字氾濫、部分注入方式不一致 |
| 測試覆蓋 | ⭐⭐☆☆☆ | 僅 Controller 整合測試，無 Service 單元測試，依賴本地 DB |
| 安全風險 | ⭐⭐☆☆☆ | JWT secret 佔位符已提交、弱 DB 憑證在 repo 中、CORS 全開 |
| 依賴健康 | ⭐⭐⭐☆☆ | Spring Boot 4.0.2 極新，jjwt 0.11.5 有較新版本可升 |
| 效能基準 | ⭐⭐⭐☆☆ | 無監控數據；Checkout 迴圈儲存、SQL logging 在 prod config |

**整體評級：C+（67/100）**

### 三大最緊急問題

1. 🔴 **機密憑證已提交 Git**：`application.properties` 含明文 DB 密碼與 JWT secret 佔位符，一旦 repo 公開即造成資安事故
2. 🔴 **JWT Secret 為弱佔位符**：`jwt.secret=your_jwt_secret_key_here` 長度不足且可預測，可偽造任意 token
3. 🟡 **無環境配置分離**：單一 `application.properties` 涵蓋 dev/prod，`show-sql=true` 及 `ddl-auto=update` 會影響正式環境

### 建議立即行動

> 本週可以做的三件事：
> 1. 將 `application.properties` 加入 `.gitignore`，改用環境變數或 `application-local.properties`（不提交）
> 2. 替換 JWT secret 為 ≥ 256-bit 隨機字串，並透過環境變數注入
> 3. 建立 `application-prod.properties`，關閉 `show-sql`，將 `ddl-auto` 設為 `validate`

---

## Phase 1｜架構健康度分析

### 1.1 架構模式識別

**整體模式：** Modular Monolith（單一部署，按功能模組化）
**分層結構：** Layered（Controller → Service → Repository → Entity）
**通訊方式：** 同步 HTTP REST + 非同步 WebSocket (STOMP)

### 架構符合度評分

| 面向 | 評分 (1-5) | 說明 |
|------|-----------|------|
| 關注點分離 | 4 | Controller 不含業務邏輯；DTO 轉換在 Service 中執行 |
| 模組邊界清晰度 | 3 | 部分功能分散於 `TeacherController` 與 `CourseController` 重疊 |
| 依賴方向一致性 | 4 | 整體向下依賴，無明顯循環依賴 |
| 可測試性 | 2 | Service 層無法獨立單元測試；測試直接依賴真實 MySQL |
| 可觀測性 | 2 | 無 logging 配置；Actuator 已引入但未加保護 |

**架構整體健康度：3 / 5**

### 1.2 關鍵架構問題清單

**問題 1：課程建立端點重複**
- **位置：** `TeacherController.java` `POST /api/teacher/courses` vs `CourseController.java` `POST /api/courses`
- **影響等級：** 🟡 中
- **影響範圍：** 前端整合、API 文件混淆
- **改善方向：** 確認語意差異後統一為單一端點，或明確以路由前綴區分「老師操作」vs「公開課程」

**問題 2：`/api/teacher/**` 全開公開包含寫入操作**
- **位置：** `SecurityConfig.java:40`
- **影響等級：** 🔴 高
- **影響範圍：** TutorProfileController（POST/PUT/DELETE）、TeacherController、TutorScheduleController 等所有 `/api/teacher/**`
- **改善方向：** GET 端點保持公開，POST/PUT/DELETE 加上 `hasRole('TEACHER')` 保護

**問題 3：`CheckoutService` 迴圈內逐筆 `save()`**
- **位置：** `CheckoutService.java:65-74`
- **影響等級：** 🟡 中
- **影響範圍：** 高峰購課效能
- **改善方向：** 收集所有 `Bookings` 物件後呼叫 `bookingRepo.saveAll(list)`

---

## Phase 2｜技術債盤點

### 🔴 優先處理（影響安全或開發速度）

| # | 技術債項目 | 位置 | 預估影響 | 修復成本 |
|---|-----------|------|---------|---------|
| 1 | `application.properties` 含明文憑證並已提交 | `src/main/resources/application.properties` | 資安事故風險 | 小 |
| 2 | JWT secret 為弱佔位符字串 | `application.properties:13` | Token 可偽造 | 小 |
| 3 | `show-sql=true` 洩漏 SQL 到 prod log | `application.properties:9` | 效能 + 資料洩漏 | 小 |
| 4 | `ddl-auto=update` 在 prod 有 schema 異動風險 | `application.properties:8` | 資料遺失風險 | 小 |
| 5 | `/api/teacher/**` 寫入端點無授權保護 | `SecurityConfig.java:40` | 任意人可修改老師資料 | 小 |
| 6 | `test-email/**` 公開端點觸發真實寄信 | `SecurityConfig.java:51` | 濫發郵件 | 小 |

### 🟡 中期改善（影響可維護性）

| # | 技術債項目 | 位置 | 預估影響 | 修復成本 |
|---|-----------|------|---------|---------|
| 1 | 角色、狀態、交易類型全用魔法數字（1/2/3） | `entity/*.java`, `service/*.java` | 維護困難，新人易誤用 | 中 |
| 2 | `CheckoutService` 使用 `@Autowired` 欄位注入 | `CheckoutService.java:14-18` | 不可測試、隱性依賴 | 小 |
| 3 | `pom.xml` 重複宣告 WebSocket 依賴 | `pom.xml:116-126` | 建置噪音 | 小 |
| 4 | `.gitignore` 含未解決的 merge conflict 標記 | `.gitignore:42-47` | 部分忽略規則失效 | 小 |
| 5 | `SecurityConfig` 在兩個 package 各有一份 | `config/SecurityConfig.java` vs `security/SecurityConfig.java` | 混淆生效配置 | 小 |
| 6 | `pom.xml` 描述仍為 "Demo project for Spring Boot" | `pom.xml:15` | 專業度 | 小 |
| 7 | 無 env 分離（dev/prod properties） | `src/main/resources/` | 生產配置管理 | 小 |

### 🟢 長期優化（提升品質但不緊急）

| # | 技術債項目 | 位置 | 預估影響 | 修復成本 |
|---|-----------|------|---------|---------|
| 1 | Flyway 被註解掉，Schema 靠 `ddl-auto` 管理 | `pom.xml:128-134` | 無法追蹤 Schema 版本 | 中 |
| 2 | 無 logging 框架配置（logback.xml 缺失） | `src/main/resources/` | 無法區分 log level per package | 小 |
| 3 | Swagger/Actuator 在任何環境都公開 | `SecurityConfig.java:54` | 資訊洩漏 | 小 |
| 4 | 無 API 版本控制（缺 `/v1/` prefix） | `controller/*.java` | 日後 breaking change 難管理 | 大 |
| 5 | 無 Docker/容器化支援 | repo root | 環境一致性 | 中 |
| 6 | 無 CI/CD pipeline | `.github/` | 手動部署風險 | 中 |

**技術債總覽：**
- 高優先技術債：6 項
- 預估修復工時：高優先 ≈ 3 人天 / 全部 ≈ 10 人天
- 技術債比率（估計）：~15%

---

## Phase 3｜依賴風險分析

### 執行環境

| 項目 | 目前版本 | 說明 | 風險 |
|------|---------|------|------|
| Java | 21 (LTS) | Active LTS，2029 EOL | 低 |
| Spring Boot | 4.0.2 | 2025 年新版本系列，API 異動較大 | 中（升級文件需確認） |
| MySQL | 8+ (連接器預設) | 使用 runtime driver | 低 |

### 核心依賴

| 套件名稱 | 目前版本 | 備註 | 升級難度 |
|---------|---------|------|---------|
| `jjwt-api/impl/jackson` | 0.11.5 | 0.12.x 有 API 改動（`Keys.hmacShaKeyFor` 替換） | 中 |
| `springdoc-openapi-starter-webmvc-ui` | 2.5.0 | 與 Spring Boot 4.x 相容性需確認 | 中 |
| `spring-boot-starter-*` | 4.0.2 | 由 parent 統一管理，版本一致 | 低 |
| `mysql-connector-j` | 由 Spring Boot parent 管理 | 無自定版本 | 低 |

### 依賴健康度摘要

- 總依賴數：~12 直接依賴（間接依賴由 Spring BOM 管理）
- 有已知漏洞：無直接確認（建議執行 `mvn dependency-check:check` 驗證）
- EOL 或不再維護：0 個
- 嚴重落後版本（> 2 major）：0 個
- **特別注意：** `jjwt 0.11.5` 建議升至 `0.12.x` 以採用更安全的 Key 管理 API

---

## Phase 4｜程式碼品質指標

### 複雜度（估算）

| 模組 / 檔案 | 複雜度評估 | 說明 |
|-----------|-----------|------|
| `CheckoutService.java` (77 行) | CC ≈ 8 | 防超賣迴圈合理，尚在可接受範圍 |
| `OrderService.java` (150 行) | CC ≈ 12 | 多個 if-return 分支，可接受 |
| `SecurityConfig.java` | CC ≈ 3 | 配置類，複雜度低 |
| `JwtFilter.java` | CC ≈ 6 | 標準 filter 邏輯 |

> 複雜度基準：CC ≤ 10 良好，11-20 需注意，> 20 必須重構。整體偏低，品質尚可。

### 測試覆蓋率

| 模組 | 測試類型 | 狀況 |
|------|---------|------|
| Controller 層（8/16） | 整合測試（MockMvc + 真實 DB） | 覆蓋主要 CRUD 路徑 |
| Service 層（15 個） | ❌ 無測試 | 0% 覆蓋 |
| Repository 層（11 個） | 間接由 Controller 測試涵蓋 | 部分覆蓋 |
| `CheckoutService`（核心交易） | ❌ 無專屬測試 | 未被任何測試直接覆蓋 |

**整體估計覆蓋率：** ~30%（僅 Controller 端點）

**嚴重問題：** 測試直接連接本地 MySQL（`localhost:3306/demodb`），在沒有本地資料庫的 CI 環境中無法執行。

### 程式碼重複率

- `toResp()` 轉換模式在多個 Service 中重複實作
- 錯誤回應格式（`Map.of("message", "...")` vs `ErrorResponse`）在 Controller 間不一致
- 估計重複率：~10%

### 文件化程度

| 類型 | 覆蓋率 | 品質 |
|------|--------|------|
| 公開 API 註解（@Operation） | 0% | 缺失（只有 springdoc 依賴，無實際標注） |
| 複雜邏輯說明（中文註解） | ~60% | 良好，CheckoutService 等核心邏輯有說明 |
| README 完整度 | 3/5 | 有環境設置說明，缺 API 文件連結與架構圖 |

---

## Phase 5｜安全風險評估

> ⚠️ 此為靜態分析初步評估，不取代專業滲透測試。

### OWASP Top 10 快速檢核

| 風險類別 | 狀態 | 發現 |
|---------|------|------|
| A01 存取控制缺失 | ❌ 有風險 | `/api/teacher/**` 寫入端點無授權；`/actuator/**` 公開 |
| A02 加密機制失效 | ⚠️ 需確認 | JWT secret 為弱佔位符；BCrypt 密碼哈希已實作 |
| A03 注入攻擊 | ✅ 良好 | Spring Data JPA 使用參數化查詢；`@Query` 有一處使用命名參數 |
| A04 不安全設計 | ⚠️ 需確認 | CheckoutService 防超賣邏輯在 `@Transactional` 內，但並發鎖需確認 |
| A05 安全設定錯誤 | ❌ 有風險 | CORS 全開、Swagger/Actuator 公開、test-email 公開 |
| A06 易受攻擊的元件 | ⚠️ 需確認 | 建議執行 `mvn dependency-check:check` |
| A07 身份驗證失效 | ⚠️ 需確認 | JWT 驗證已實作；但 secret 強度不足時可偽造 token |
| A08 軟體完整性失效 | ✅ 良好 | Maven 依賴由 Spring BOM 管理 |
| A09 記錄與監控不足 | ❌ 有風險 | 無結構化日誌；無異常告警機制 |
| A10 SSRF | ✅ 良好 | 無外部 URL 請求邏輯 |

### 發現的安全問題

| 嚴重度 | 問題描述 | 位置 | 建議修復 |
|--------|---------|------|---------|
| 🔴 高 | JWT secret 為弱佔位符已提交 repo | `application.properties:13` | 改用環境變數 `${JWT_SECRET}`，≥32 字元隨機值 |
| 🔴 高 | DB 憑證明文提交 | `application.properties:3-4` | 加入 `.gitignore`，改用環境變數 |
| 🔴 高 | `/api/teacher/**` POST/PUT/DELETE 無鑑權 | `SecurityConfig.java:40` | 區分 GET（公開）vs 寫入（需 TEACHER 角色） |
| 🟡 中 | CORS 允許所有來源 | `SecurityConfig.java:70` | 生產環境限制為前端網域 |
| 🟡 中 | Swagger UI 及 Actuator 公開 | `SecurityConfig.java:54` | 生產環境需 IP 白名單或認證保護 |
| 🟡 中 | `test-email/**` 觸發真實寄信且無鑑權 | `SecurityConfig.java:51` | 移除或加上 Admin 角色保護 |
| 🟡 中 | CheckoutService 防超賣無資料庫層級鎖 | `CheckoutService.java:36-47` | 考慮樂觀鎖（`@Version`）或 SELECT FOR UPDATE |

---

## Phase 6｜效能基準評估

> 因未取得監控數據，效能評估為靜態程式碼推斷，建議導入 APM 工具（Micrometer + Grafana）後取得實際數據。

### 程式碼層效能問題

| 問題 | 位置 | 預估影響 | 建議 |
|------|------|---------|------|
| Checkout 迴圈逐筆 `save()` | `CheckoutService.java:65-74` | N 次 DB round-trip | 改用 `bookingRepo.saveAll(list)` |
| `show-sql=true` 輸出所有 SQL | `application.properties:9` | Log 量爆增影響效能 | prod 設為 false |
| 缺少分頁（getOrdersByUserId） | `OrderService.java:85-90` | 用戶訂單多時全量載入 | 加入 `Pageable` 參數 |
| 缺少快取（Course 查詢重複） | `CourseService`, `CheckoutService` | 熱門課程重複查 DB | 考慮 `@Cacheable` + Spring Cache |

---

## Phase 7｜改善 Roadmap

### 立即行動（0-2 週，無需架構變動）

| 優先序 | 行動項目 | 預期效益 | 負責人建議 |
|--------|---------|---------|-----------|
| 1 | 將 `application.properties` 加入 `.gitignore`，改用 `application-local.properties`（不提交） | 消除憑證洩漏風險 | DevOps/後端 |
| 2 | 替換 JWT secret 為強隨機值並改用環境變數 `${JWT_SECRET}` | 防止 Token 偽造 | 後端 |
| 3 | 修復 `.gitignore` 中的 merge conflict 標記 | 確保敏感檔案被正確忽略 | 任何人 |
| 4 | 建立 `application-prod.properties`：`show-sql=false`，`ddl-auto=validate` | 防止生產 Schema 意外異動 | 後端 |
| 5 | 將 `/api/teacher/**` 的 POST/PUT/DELETE 加上 `hasRole('TEACHER')` 保護 | 防未授權寫入 | 後端 |
| 6 | 移除 `pom.xml` 中重複的 WebSocket 依賴 | 消除建置噪音 | 後端 |

### 短期改善（1-3 個月，局部重構）

| 優先序 | 行動項目 | 預期效益 | 複雜度 |
|--------|---------|---------|--------|
| 1 | 將所有魔法數字（role, status, transactionType）替換為 Java Enum | 提升可讀性，防止錯誤賦值 | 小 |
| 2 | `CheckoutService` 改用建構子注入（`@RequiredArgsConstructor`） | 統一注入風格，提升可測試性 | 小 |
| 3 | 為 Service 層補充單元測試（Mockito mock repository） | 提升 Service 測試覆蓋至 ≥ 60% | 中 |
| 4 | 測試改用 H2 in-memory 或 Testcontainers MySQL | 解除本地 DB 依賴，支援 CI 執行 | 中 |
| 5 | 統一 Controller 錯誤回應格式（全用 `ErrorResponse`） | API 一致性，前端易整合 | 小 |
| 6 | 限制生產環境 Swagger/Actuator 存取（環境變數控制） | 防止敏感資訊洩漏 | 小 |

### 中期重構（3-6 個月，架構調整）

| 優先序 | 行動項目 | 預期效益 | 複雜度 |
|--------|---------|---------|--------|
| 1 | 啟用 Flyway 管理 DB Schema 版本 | Schema 變更可追蹤、可回滾 | 中 |
| 2 | 建立 `application-dev.properties` / `application-prod.properties` 環境分離 | 避免生產配置錯誤 | 小 |
| 3 | 加入 logback.xml，區分 package log level | 生產 log 可觀測性 | 小 |
| 4 | 為 `getOrdersByUserId` 等查詢加入分頁 | 大資料量效能保障 | 小 |
| 5 | 建立 CI/CD pipeline（GitHub Actions） | 自動化測試與部署 | 中 |

### 長期演進（6 個月以上，策略方向）

- 考慮導入 Docker Compose 統一開發環境，解決「只能在 local MySQL 跑測試」問題
- 建立 API 版本控制策略（`/v1/`），為未來 breaking change 做準備
- 評估是否引入 Redis 作為 Session/Cache 層（登入狀態查詢、課程熱點快取）
- 建立 APM 監控基礎設施（Micrometer + Grafana），讓效能數據可視化

---

### 投資回報分析

| 行動類型 | 預估投入 | 預估回報 |
|---------|---------|---------|
| 立即行動（安全修復） | 1 人天 | 消除 3 個高嚴重度安全風險 |
| 短期重構（測試/品質）| 5 人天 | Service 測試覆蓋率 0% → 60%，CI 可執行 |
| 中期演進（基礎設施）| 8 人天 | 可觀測性建立、Schema 版本管理 |

---

## 附錄：架構現況圖

```
HTTP Client / WebSocket Client
         │
    ┌────▼────┐
    │Controller│  (21 個，含 WebSocket)
    └────┬────┘
         │ DTO
    ┌────▼────┐
    │ Service │  (15 個)
    └────┬────┘
         │ Entity
    ┌────▼────────┐
    │ Repository  │  (11 個，extends JpaRepository)
    └────┬────────┘
         │ JPA/Hibernate
    ┌────▼────┐
    │  MySQL  │
    └─────────┘

橫切關注點：
  - JwtFilter（每次 Request 驗證 Token）
  - GlobalExceptionHandler（統一錯誤處理）
  - @Transactional（CheckoutService 購課原子性）
```

---

> 本報告基於靜態程式碼分析，效能數據與 CVE 掃描建議搭配 `mvn dependency-check:check` 及 APM 工具取得更精確結果。

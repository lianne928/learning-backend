<<<<<<< HEAD
# Learning Backend 程式現況分析報告

**分析日期：** 2026-03-13
**分析範圍：** d:\learning-backend 完整程式庫（src/main、src/test、設定檔、pom.xml）
=======
# Learning-Backend 程式現況分析報告

**分析日期：** 2026-03-13
**分析範圍：** 全專案 Java 原始碼（src/main/java）、設定檔、相依套件（pom.xml）
>>>>>>> upstream/feature/Review
**報告版本：** 1.0

---

## 執行摘要

### 整體健康度評分

| 面向 | 評分 | 說明 |
|------|------|------|
<<<<<<< HEAD
| 架構健康度 | ⭐⭐⭐☆☆ | 分層清晰但 JWT 認證殼子為空，安全機制尚未接通 |
| 程式碼品質 | ⭐⭐⭐☆☆ | 結構一致，但存在硬編碼憑證與部分回應格式不統一 |
| 測試覆蓋 | ⭐⭐⭐☆☆ | 有 9 個整合測試但缺乏 Service 層單元測試 |
| 安全風險 | ⭐⭐☆☆☆ | JWT Service 未實作、DB 密碼寫死、多個端點實際無保護 |
| 依賴健康 | ⭐⭐⭐☆☆ | Spring Boot 版本異常（4.0.2 不存在），jjwt 0.11.5 非最新 |
| 效能基準 | ⭐⭐⭐☆☆ | 無監控數據；show-sql=true 在生產環境有效能影響 |

**整體評級：** C（58/100）

### 三大最緊急問題

1. 🔴 **JwtService 為空殼** — SecurityConfig 宣告需要認證的端點實際上無 JWT 驗證邏輯，所有保護形同虛設
2. 🔴 **硬編碼機密資訊** — `application.properties` 包含 DB 密碼（`root`）與 JWT Secret，直接暴露於版控
3. 🟡 **Spring Boot 版本號異常** — pom.xml 寫 `4.0.2`，Spring Boot 4.x 尚未正式發布，可能為筆誤（應為 3.4.x 或 3.2.x）
=======
| 架構健康度 | ⭐⭐⭐☆☆ | MVC 分層清晰，但安全設定層完全失效 |
| 程式碼品質 | ⭐⭐☆☆☆ | 存在致命邏輯 bug、magic numbers、濫用 System.out.println |
| 測試覆蓋 | ⭐☆☆☆☆ | 僅 1 個空白啟動測試，業務邏輯零測試 |
| 安全風險 | ⭐☆☆☆☆ | 全部 API 無驗證、Login 永遠成功、CORS 全開 |
| 依賴健康 | ⭐⭐⭐⭐☆ | 依賴版本合理，Spring Boot 4.0.2 較新但無已知 CVE |
| 效能基準 | ⭐⭐⭐☆☆ | 無監控數據，靜態推斷無明顯 N+1，但缺分頁查詢保護 |

**整體評級：** D（38/100）

### 三大最緊急問題

1. 🔴 **安全機制完全停用**：`SecurityConfig.java` 全行註解，`TestSecurityConfig` 生效，所有 API 端點無需任何驗證即可呼叫，資料庫完全暴露。
2. 🔴 **Login 永遠回傳成功**：`UserController.login()` 呼叫的是 `userService.login(User user)` 重載版本，該版本第 72 行直接 `return true`，任何人皆可「登入」。
3. 🔴 **課程建立功能全面損壞**：`CourseService.java:46` 邏輯錯誤（`!=1 || !=2` 恆真），所有建立課程請求永遠回傳失敗。
>>>>>>> upstream/feature/Review

### 建議立即行動

> 本週可以做的三件事：
<<<<<<< HEAD
> 1. 實作 `JwtService`（`generateToken` / `validateToken`），補齊認證過濾器
> 2. 將 DB 密碼、JWT Secret 移至 `.env` 或 Spring Profiles，並加入 `.gitignore`
> 3. 確認 pom.xml `spring-boot.version` 為 `3.x.x`，並執行 `mvn dependency:tree` 驗證實際解析版本
=======
> 1. 刪除 `TestSecurityConfig.java`，移除 `SecurityConfig.java` 的所有註解並啟用
> 2. 修正 `CourseService.java:46`：將 `||` 改為 `&&`
> 3. 修正 `UserController.java:33`：將 `userService.login(user)` 改為 `userService.login(user.getEmail(), user.getPassword())`
>>>>>>> upstream/feature/Review

---

## Phase 1｜架構健康度分析

### 1.1 架構模式識別

<<<<<<< HEAD
**整體模式：** Modular Monolith（單一 Spring Boot 服務，內部依功能分包）
**分層結構：** Layered（Controller → Service → Repository → Entity）
**通訊方式：** 同步 HTTP REST + WebSocket（STOMP over SockJS）
=======
**整體模式：** Monolith（單體應用）
**分層結構：** Layered MVC（Controller → Service → Repository → Entity）
**通訊方式：** 同步 HTTP（REST） + 非同步 WebSocket（STOMP over SockJS）
>>>>>>> upstream/feature/Review

### 架構符合度評分

| 面向 | 評分 (1-5) | 說明 |
|------|-----------|------|
<<<<<<< HEAD
| 關注點分離 | 4 | Controller / Service / Repo 分工清晰；DTO 轉換存在於 Service 或 Controller 層，略不一致 |
| 模組邊界清晰度 | 3 | 所有模組同一 package，未按業務域（booking、course、user）做子模組切分 |
| 依賴方向一致性 | 4 | 上層依賴下層，方向正確；部分 Service 直接操作 Entity 而非透過 Repository 抽象 |
| 可測試性 | 3 | 有整合測試；Service 層缺乏 Mock 單元測試；JwtService 為空導致難以測試認證流程 |
| 可觀測性 | 2 | `spring-boot-starter-actuator` 已引入但無自訂 health indicator；`show-sql=true` 為唯一 debug 工具 |

**架構整體健康度：** 3.2 / 5

### 1.2 關鍵架構問題清單

**問題 1：JwtService 為空實作**
- **影響等級：** 🔴 高
- **位置：** `service/JwtService.java`
- **影響範圍：** 所有需認證的 API 端點（`/api/auth` 以外的端點）
- **改善方向：** 實作 `generateToken(UserDetails)`、`extractUsername(String)`、`validateToken(String, UserDetails)`；在 `SecurityConfig` 加入 `JwtAuthenticationFilter`

**問題 2：`VideoRoomController` 與 `ChatMessageController` 共用相同業務概念但分開設計**
- **影響等級：** 🟡 中
- **位置：** `controller/VideoRoomController.java`、`controller/ChatMessageController.java`
- **影響範圍：** 聊天訊息同時走 REST（/api/chatMessage）與 WebSocket（/app/chat/{bookingId}），資料來源可能不一致
- **改善方向：** 釐清 WebSocket chat 與 REST chat 的職責邊界，考慮合併或明確文件化兩者差異

**問題 3：`BookingService.sendBooking()` 承擔過多職責**
- **影響等級：** 🟡 中
- **位置：** `service/BookingService.java`
- **影響範圍：** 預約建立、訂單建立、價格計算同時在一個 Service 方法處理
- **改善方向：** 提取 `PricingService` 或將訂單建立委派給 `OrderService`，符合 SRP
=======
| 關注點分離 | 3 | Controller / Service / Repo 分層明確，但 Service 層直接操作多個 Repo（CheckoutService 依賴 5 個 Repo，耦合偏高） |
| 模組邊界清晰度 | 3 | 套件依功能命名清楚，但 `entity` 包內存在 `Review` 與 `Reviews` 兩個重複意義的類別 |
| 依賴方向一致性 | 4 | 整體由 Controller 往 Service 往 Repo 方向正確，無循環依賴 |
| 可測試性 | 1 | 所有相依使用 `@Autowired` 欄位注入，難以做 unit test mock；JwtService 為空類別 |
| 可觀測性 | 1 | 使用 `System.out.println` 輸出日誌，無結構化 log，Actuator 雖引入但無相關監控設定 |

**架構整體健康度：** 2.4 / 5

### 1.2 關鍵架構問題清單

**問題 1：安全設定層完全失效**
- **影響等級：** 🔴 高
- **位置：** [SecurityConfig.java](src/main/java/com/learning/api/config/SecurityConfig.java)（全行註解）、[TestSecurityConfig.java](src/main/java/com/learning/api/config/TestSecurityConfig.java)（生效中）
- **影響範圍：** 所有 API 端點，包含購買、預約、課程建立
- **改善方向：** 刪除 TestSecurityConfig，取消 SecurityConfig 的所有註解並配合 JwtFilter 實作

**問題 2：JwtService 為空實作**
- **影響等級：** 🔴 高
- **位置：** [JwtService.java](src/main/java/com/learning/api/service/JwtService.java)（第 1-4 行，只有空類別殼）
- **影響範圍：** 整個身份驗證流程
- **改善方向：** 實作 `generateToken()` / `validateToken()` / `extractUserId()` 方法，搭配 `JwtAuthenticationFilter`

**問題 3：User Entity 直接作為 API DTO**
- **影響等級：** 🟡 中
- **位置：** [UserController.java:20,31](src/main/java/com/learning/api/controller/UserController.java)
- **影響範圍：** 使用者相關端點（register / login）
- **改善方向：** 建立 `RegisterReq` / `LoginReq` DTO，避免暴露 `id`、`wallet`、`role` 等敏感欄位
>>>>>>> upstream/feature/Review

---

## Phase 2｜技術債盤點

<<<<<<< HEAD
### 🔴 優先處理（影響開發速度或安全）

| # | 技術債項目 | 位置 | 預估影響 | 修復成本 |
|---|-----------|------|---------|---------|
| 1 | `JwtService` 為空，整體認證機制未完成 | `service/JwtService.java` | 所有受保護 API 無法正常驗證身份 | 中（2-3 天） |
| 2 | DB 密碼（`root`）、JWT Secret 硬編碼於 `application.properties` | `src/main/resources/application.properties` | 機密資訊洩露至版控 | 小（0.5 天） |
| 3 | `spring-boot.version=4.0.2` — 不存在的版本 | `pom.xml` | 可能使用錯誤的依賴解析樹，建置不穩定 | 小（0.5 天） |
| 4 | Flyway 停用（`spring.flyway.enabled=false`）且有多個 `.sql` 腳本在 resources | `application.properties`、`*.sql` | 無自動化 schema 管理，多人開發 DB 結構易出現分歧 | 中（1-2 天） |

### 🟡 中期改善（影響可維護性）

| # | 技術債項目 | 位置 | 預估影響 | 修復成本 |
|---|-----------|------|---------|---------|
| 5 | `spring.jpa.show-sql=true` 未限制於開發環境 | `application.properties` | 生產環境 SQL 日誌影響效能與 log 可讀性 | 小 |
| 6 | `SecurityConfig` 白名單過寬（`/api/teacher/**` 全部公開） | `config/SecurityConfig.java` | 教師資料修改端點（PUT/DELETE）無需認證即可操作 | 中 |
| 7 | 回應格式不統一（部分用 `Map<String,Object>` 回傳，部分用自訂 DTO） | 多個 Controller | 前端解析困難，不易統一 API 規格文件 | 中（1 天） |
| 8 | `jwt.exp-minutes=5` 過短 | `application.properties` | 使用者每 5 分鐘需重新登入，體驗極差 | 小 |
| 9 | `TestController`（`/api/TestController` GET）留存於主程式 | `controller/TestController.java` | Debug 端點未移除，暴露系統資訊 | 小 |
| 10 | `Course` Entity 內有被註解掉的 `level` 欄位 | `entity/Course.java` | 代碼意圖不明，`CourseReq` 仍有 `level` 欄位但 Entity 不存儲 | 小 |

### 🟢 長期優化（提升品質但不緊急）

| # | 技術債項目 | 位置 | 預估影響 | 修復成本 |
|---|-----------|------|---------|---------|
| 11 | 缺乏 Service 層單元測試（僅有 Controller 整合測試） | `src/test/` | 業務邏輯錯誤難以快速定位 | 大（3-5 天） |
| 12 | 硬編碼 magic number（role: 1/2/3、subject: 11-13/21-23/31）缺少 Enum 包裝 | 多個 Service / Entity | 可讀性差，修改時需全文搜尋 | 中 |
| 13 | `WalletLog` 有詳細設計但無對應的 `WalletService`，業務邏輯散落 | `entity/WalletLog.java`、`repo/WalletLogRepository.java` | 錢包操作邏輯一致性難以保障 | 中 |

**技術債總覽：**
- 高優先技術債：4 項
- 預估修復工時：約 6-8 人天（高優先）/ 約 16-22 人天（全部）
- 技術債比率（估計）：~30%
=======
### 技術債清單

#### 🔴 優先處理（影響開發速度或安全）

| # | 技術債項目 | 位置 | 預估影響 | 修復成本 |
|---|-----------|------|---------|---------|
| 1 | SecurityConfig 全行註解，TestSecurityConfig 代替生效 | [SecurityConfig.java](src/main/java/com/learning/api/config/SecurityConfig.java) | 系統完全無身份驗證 | 小 |
| 2 | JwtService 為空類別 | [JwtService.java](src/main/java/com/learning/api/service/JwtService.java) | 無法簽發 / 驗證 Token | 中 |
| 3 | Login 呼叫永遠成功的重載方法 | [UserController.java:33](src/main/java/com/learning/api/controller/UserController.java) | 任意帳號密碼皆可登入 | 小 |
| 4 | CourseService 主題驗證邏輯錯誤（`\|\|` 應為 `&&`） | [CourseService.java:46](src/main/java/com/learning/api/service/CourseService.java) | 課程建立 100% 失敗 | 小 |
| 5 | userId 從 Request Body 傳入（無 Auth Context） | [BookingService.java:25](src/main/java/com/learning/api/service/BookingService.java)、[CheckoutService.java:23](src/main/java/com/learning/api/service/CheckoutService.java) | 任何人可冒充他人身份操作 | 中 |

#### 🟡 中期改善（影響可維護性）

| # | 技術債項目 | 位置 | 預估影響 | 修復成本 |
|---|-----------|------|---------|---------|
| 6 | Status 狀態碼使用 Magic Numbers（1/2/3） | [Order.java:36](src/main/java/com/learning/api/entity/Order.java)、[CheckoutService.java:61](src/main/java/com/learning/api/service/CheckoutService.java) | 可讀性差，容易誤用 | 小 |
| 7 | 使用 `System.out.println` 代替 Logger | [CourseService.java:21,31,43,57,61](src/main/java/com/learning/api/service/CourseService.java) | 無法控制 log level，不可關閉 | 小 |
| 8 | Flyway 資料庫遷移關閉 | [application.properties:20](src/main/resources/application.properties)、[pom.xml:122-128](pom.xml) | 無法追蹤 schema 變更歷史，上線風險高 | 中 |
| 9 | `Review` 與 `Reviews` 兩個相似 Entity 共存 | [entity/](src/main/java/com/learning/api/entity/) | 語意混淆，維護困難 | 小 |
| 10 | CheckoutService 防超賣使用「查詢後鎖」非樂觀鎖 | [CheckoutService.java:36-47](src/main/java/com/learning/api/service/CheckoutService.java) | 高並發下仍可能超賣 | 中 |
| 11 | User Entity 作為 register / login 的 Request Body | [UserController.java:20,31](src/main/java/com/learning/api/controller/UserController.java) | 過度暴露 Entity 欄位 | 小 |
| 12 | `spring.jpa.show-sql=true` 在 application.properties | [application.properties:7](src/main/resources/application.properties) | 生產環境 log 暴露所有 SQL | 小 |

#### 🟢 長期優化（提升品質但不緊急）

| # | 技術債項目 | 位置 | 預估影響 | 修復成本 |
|---|-----------|------|---------|---------|
| 13 | 所有 Controller 使用 `@Autowired` 欄位注入 | 全部 Controller | 難以 mock、隱藏必要相依 | 小（統一換建構子注入） |
| 14 | CheckoutService 建立多筆 Booking 用 loop 逐筆 save | [CheckoutService.java:65-74](src/main/java/com/learning/api/service/CheckoutService.java) | 多時段時 N 次 INSERT | 小（改 saveAll） |
| 15 | 無 API 輸入驗證 Annotation（@Valid、@NotBlank） | 全部 Controller | 惡意輸入未在入口攔截 | 小 |
| 16 | 無 CI/CD Pipeline | 整個專案 | 無法自動化測試與部署 | 中 |

**技術債總覽：**
- 高優先技術債：5 項
- 預估修復工時：3 人天（高優先）/ 10 人天（全部）
- 技術債比率（估計）：40%（大量開發測試用 workaround 尚未移除）
>>>>>>> upstream/feature/Review

---

## Phase 3｜依賴風險分析

### 執行環境

<<<<<<< HEAD
| 項目 | 目前版本 | 說明 | 風險 |
|------|---------|------|------|
| Java | 21 | LTS，Active Support | 低 |
| Spring Boot | 4.0.2（異常） | Spring Boot 4.x 未正式發布，應為 3.x | 🔴 高－需立即確認實際解析版本 |
| MySQL Connector | 使用 Spring Boot 管理版本 | 依 Boot 版本決定 | 依版本而定 |

### 核心依賴

| 套件名稱 | 目前版本 | 備注 | 升級難度 |
|---------|---------|------|---------|
| jjwt | 0.11.5 | 非最新（目前 0.12.x），但無已知重大 CVE | 低 |
| springdoc-openapi | 2.5.0 | 較新版本，符合 Spring Boot 3.x | 低 |
| MySQL Connector/J | Spring 管理 | 依 Boot 版本 | 低 |
| spring-boot-starter-security | Spring 管理 | JWT Filter 尚未接上 | 中 |

> ⚠️ 因無法執行 `mvn dependency:tree`，CVE 資訊為靜態推估，建議執行 `mvn dependency-check:check` 取得實際漏洞掃描結果。

### 依賴健康度摘要

- 直接依賴：約 10 個 starter + 2 個工具庫
- 有已知漏洞：待掃描確認
- EOL 或不再維護：0（已知範圍內）
- 版本異常：1 個（Spring Boot 4.0.2）
=======
| 項目 | 目前版本 | 最新版本 | 維護狀態 | 風險 |
|------|---------|---------|---------|------|
| Java | 21 | 21 LTS | Active | 低 |
| Spring Boot | 4.0.2 | 4.0.2 | Active（最新） | 低，但為最新版本，生態系成熟度待觀察 |
| Maven Wrapper | 3.x | - | Active | 低 |

### 核心依賴

| 套件名稱 | 目前版本 | 備註 | 已知 CVE | 升級難度 |
|---------|---------|------|---------|---------|
| jjwt-api/impl/jackson | 0.11.5 | 非最新（0.12.x 為新 API） | 無已知 CVE | 中（API 有 Breaking Change） |
| springdoc-openapi | 2.5.0 | 合理版本 | 無已知 CVE | 低 |
| mysql-connector-j | 由 Spring Boot BOM 管理 | - | 無 | 低 |
| lombok | 由 Spring Boot BOM 管理 | - | 無 | 低 |

### 依賴健康度摘要

- 總依賴數：~14 個直接依賴（間接依賴由 Spring Boot BOM 管理）
- 有已知漏洞：0 個
- EOL 或不再維護：0 個
- 嚴重落後版本（> 2 major versions）：0 個

**建議處理：** jjwt 從 0.11.5 升至 0.12.x（引入更直觀的 API），待 JwtService 實作時一併升版。
>>>>>>> upstream/feature/Review

---

## Phase 4｜程式碼品質指標

<<<<<<< HEAD
### 複雜度（估計）

| 模組 / 檔案 | 觀察 | 建議 |
|-----------|------|------|
| `BookingService.sendBooking()` | 同時處理訂單建立、預約建立、價格計算，估計 CC > 15 | 重構，提取子方法 |
| `OrderService` | 多個 status 轉換 if-else，CC 約 12-18 | 可用狀態機模式簡化 |
| `SecurityConfig.filterChain()` | 白名單列舉超過 15 條，可讀性低 | 整理為常數或 Enum |
| 其他 Service / Controller | 多數方法簡短，CC ≤ 10 | 可接受 |

### 測試覆蓋率（估計）

| 模組 | 測試類型 | 覆蓋評估 |
|------|---------|---------|
| Controller 層 | Integration（MockMvc） | 高（9 個測試類，CourseControllerTest 有 19 個測試案例） |
| Service 層 | 無單元測試 | 低（0%） |
| Repository 層 | 依賴整合測試間接測試 | 中 |
| 認證流程 | 無（JwtService 為空） | 0% |

**整體覆蓋率估計：** ~40%（目標：核心業務邏輯 ≥ 80%）
=======
### 複雜度

| 模組 / 方法 | Cyclomatic Complexity（估計） | 建議 |
|-----------|-----|------|
| `CheckoutService.processPurchase()` | ~8 | 可接受，但需分拆驗證段與交易段 |
| `UserService.register()` | ~7 | 可接受 |
| `CourseService.sendCourses()` | ~8 | 需重構（含邏輯 bug） |
| `BookingService.sendBooking()` | ~6 | 可接受 |

> 複雜度整體尚在合理範圍（CC ≤ 10），但 `CourseService.sendCourses()` 含致命邏輯錯誤需優先修正。

### 測試覆蓋率

| 模組 | 行覆蓋率 | 分支覆蓋率 | 測試類型 |
|------|---------|---------|---------|
| UserService | 0% | 0% | 無 |
| CourseService | 0% | 0% | 無 |
| CheckoutService | 0% | 0% | 無 |
| BookingService | 0% | 0% | 無 |
| 全專案 | < 1% | < 1% | 僅 contextLoads（空白測試） |

**整體覆蓋率：** < 1%（目標：核心業務邏輯 ≥ 80%）
>>>>>>> upstream/feature/Review

### 程式碼重複率

- 估計重複程式碼比例：~15%
<<<<<<< HEAD
- 主要重複區域：Controller 的 `try-catch` 錯誤處理模式、Service 層的 CRUD 樣板（findAll, findById, save, deleteById）、review 與 lessonFeedback 高度相似的結構
=======
- 主要重複區域：各 Controller 的 null-check / bad-request 回傳樣板、Service 層的 null-check 邏輯
>>>>>>> upstream/feature/Review

### 文件化程度

| 類型 | 覆蓋率 | 品質 |
|------|--------|------|
<<<<<<< HEAD
| 公開 API 註解（Swagger/OpenAPI） | ~20%（springdoc 引入但未見 `@Operation`） | 形式化 |
| 複雜邏輯說明 | ~10% | 缺失 |
| README 完整度 | 4/5 | 良好（含環境設定、git 規範） |
=======
| 公開 API 註解（Swagger） | ~40% | 形式化（有 Swagger 套件但大部分無 @Operation / @Schema 標注） |
| 複雜邏輯說明 | ~30% | 良好（CheckoutService 流程有中文行內註解） |
| README 完整度 | 3/5 | 有環境需求、Git 規範，但無 API 清單、部署說明 |
>>>>>>> upstream/feature/Review

---

## Phase 5｜安全風險評估

> ⚠️ 此為靜態分析初步評估，不取代專業滲透測試。

### OWASP Top 10 快速檢核

| 風險類別 | 狀態 | 發現 |
|---------|------|------|
<<<<<<< HEAD
| A01 存取控制缺失 | ❌ 有風險 | JwtService 為空，`/api/teacher/**` 全公開，PUT/DELETE 教師資源無需認證 |
| A02 加密機制失效 | ⚠️ 需確認 | BCrypt 用於密碼，JWT Secret 128-bit 長度尚可，但寫死於設定檔 |
| A03 注入攻擊 | ✅ 良好 | 使用 JPA/Hibernate ORM，無手拼 SQL |
| A04 不安全設計 | ⚠️ 需確認 | `OrderService.payOrder()` 無支付驗證（PaymentService 不完整） |
| A05 安全設定錯誤 | ❌ 有風險 | DB 密碼 `root`、CORS 允許所有來源（`*`）、CSRF 停用 |
| A06 易受攻擊的元件 | ⚠️ 需確認 | Spring Boot 版本異常，需掃描實際依賴 CVE |
| A07 身份驗證失效 | ❌ 有風險 | JWT 認證流程未完成，`jwt.exp-minutes=5` 過短 |
| A08 軟體完整性失效 | ✅ 良好 | Maven wrapper + lockfile 存在 |
| A09 記錄與監控不足 | ⚠️ 需確認 | Actuator 引入但無自訂端點；無 structured logging；無安全事件 log |
| A10 SSRF | ✅ 良好 | 無外部 URL 拉取邏輯 |
=======
| A01 存取控制缺失 | ❌ 有風險 | `TestSecurityConfig` 生效，所有端點 `permitAll()`，無任何 Role 控制 |
| A02 加密機制失效 | ⚠️ 需確認 | JDBC URL 設定 `useSSL=false`，DB 連線明文傳輸 |
| A03 注入攻擊 | ✅ 良好 | 使用 Spring Data JPA（參數化查詢），無明顯 SQL 注入風險 |
| A04 不安全設計 | ❌ 有風險 | `studentId` / `userId` 由前端 Request Body 傳入，未驗證是否為當前登入者（IDOR 風險） |
| A05 安全設定錯誤 | ❌ 有風險 | CSRF 關閉、CORS `*`、`show-sql=true`、`TestSecurityConfig` 未移除 |
| A06 易受攻擊的元件 | ✅ 良好 | 無已知 CVE 依賴 |
| A07 身份驗證失效 | ❌ 有風險 | `UserService.login(User)` 第 72 行直接 `return true`；Login 端點不回傳任何 Token |
| A08 軟體完整性失效 | ⚠️ 需確認 | 無 checksum / SBOM 驗證，Maven Wrapper 存在但未設 checksum |
| A09 記錄與監控不足 | ❌ 有風險 | 僅 `System.out.println`，無結構化日誌，無存取稽核記錄 |
| A10 SSRF | ✅ 良好 | 無對外部 URL 的 HTTP 請求邏輯 |
>>>>>>> upstream/feature/Review

### 發現的安全問題

| 嚴重度 | 問題描述 | 位置 | 建議修復 |
|--------|---------|------|---------|
<<<<<<< HEAD
| 🔴 高 | JWT Secret 硬編碼於版控 | `application.properties:jwt.secret` | 移至環境變數 `JWT_SECRET`，使用 `${JWT_SECRET}` 引用 |
| 🔴 高 | DB 密碼 `root` 硬編碼 | `application.properties:spring.datasource.password` | 移至 `.env` 並加入 `.gitignore` |
| 🔴 高 | JwtService 未實作，所有 `/api/**`（排除白名單）實際上可能無驗證 | `service/JwtService.java` | 實作完整 JWT 驗證鏈 |
| 🟡 中 | CORS 允許所有來源（`*`） | `config/SecurityConfig.java` | 限制為前端部署的具體 Origin |
| 🟡 中 | `TestController` 保留在主程式 | `controller/TestController.java` | 移除或限制為 `@Profile("dev")` |
| 🟡 中 | `PaymentService.payOrder()` 無實際支付驗證 | `service/OrderService.java:payOrder()` | 接入支付閘道回呼驗證後再更新狀態 |
=======
| 🔴 高 | 所有 API 無驗證（TestSecurityConfig 允許全通） | [TestSecurityConfig.java](src/main/java/com/learning/api/config/TestSecurityConfig.java) | 刪除此檔案，啟用 SecurityConfig + JwtFilter |
| 🔴 高 | Login 永遠回傳成功 | [UserController.java:33](src/main/java/com/learning/api/controller/UserController.java)、[UserService.java:71](src/main/java/com/learning/api/service/UserService.java) | 改呼叫 `login(String email, String password)` 重載版本 |
| 🔴 高 | IDOR：操作者 ID 由前端控制 | [BookingService.java:26](src/main/java/com/learning/api/service/BookingService.java)、[CheckoutService.java:23](src/main/java/com/learning/api/service/CheckoutService.java) | 從 JWT Token 取得當前使用者 ID |
| 🟡 中 | CORS 全開 `@CrossOrigin(origins = "*")` | 全部 Controller | 限縮為允許的前端 Origin |
| 🟡 中 | WebSocket 允許所有 Origin | [WebSocketConfig.java:21](src/main/java/com/learning/api/config/WebSocketConfig.java) | 限縮 `setAllowedOriginPatterns` |
| 🟡 中 | DB 連線 SSL 停用 | [application.properties:3](src/main/resources/application.properties) | 將 `useSSL=false` 改為 `useSSL=true`，或在受控內網環境加以記錄 |
| 🟢 低 | `show-sql=true` 可能洩漏查詢內容 | [application.properties:7](src/main/resources/application.properties) | 移至 dev profile，production 關閉 |
>>>>>>> upstream/feature/Review

---

## Phase 6｜效能基準評估

<<<<<<< HEAD
> 因未取得監控數據（無 APM 工具、無 Prometheus metrics），效能評估為靜態程式碼推斷。建議建立效能基準後再進行優化。
=======
> 無監控數據，以下為靜態分析推斷。建議先建立效能基準（APM 工具，如 Spring Boot Actuator + Micrometer + Prometheus）再進行優化，避免過早優化。
>>>>>>> upstream/feature/Review

### 程式碼層效能問題

| 問題 | 位置 | 預估影響 | 建議 |
|------|------|---------|------|
<<<<<<< HEAD
| `spring.jpa.show-sql=true` 未關閉 | `application.properties` | 生產環境 log 量大，IO 影響吞吐量 | 限制為 `application-dev.properties` |
| `CourseService.getAllCourses()` 可能載入所有課程 | `service/CourseService.java` | 課程數量增長後無分頁，記憶體/回應時間線性增長 | 加入 `Pageable` 分頁參數 |
| `ReviewRepository.findByCourseId()` + 平均分計算 | `repo/ReviewRepository.java` | 每次查課程都計算所有 review 的平均值 | 考慮快取或在 Course 存 denormalized avg rating |
| `LessonFeedbackRepository` 的多個 `@Query` | `repo/LessonFeedbackRepository.java` | 多次批次查詢，無索引確認 | 確認 `bookingId` 欄位有 index |
=======
| CheckoutService 建立 Booking 逐筆 `save()` | [CheckoutService.java:65-74](src/main/java/com/learning/api/service/CheckoutService.java) | 低（正常使用時段數量有限） | 改為 `bookingRepo.saveAll(bookings)` |
| WebSocket 使用 SimpleBroker（記憶體） | [WebSocketConfig.java:14](src/main/java/com/learning/api/config/WebSocketConfig.java) | 高（不支援多實例橫向擴展） | 未來改為 RabbitMQ / Redis Pub-Sub Broker |
| 無分頁查詢保護 | 多個 Repository | 低（目前資料量小） | 日後加入 Pageable 支援 |
>>>>>>> upstream/feature/Review

---

## Phase 7｜改善 Roadmap

### 立即行動（0-2 週，無需架構變動）

| 優先序 | 行動項目 | 預期效益 | 負責人建議 |
|--------|---------|---------|-----------|
<<<<<<< HEAD
| 1 | 實作 `JwtService`（generateToken, validateToken, extractClaims）並加入 `JwtAuthenticationFilter` | 修復認證機制，所有受保護端點實際受保護 | 後端工程師 |
| 2 | 將 `spring.datasource.password`、`jwt.secret` 移至環境變數 | 消除機密資訊洩露風險 | DevOps / 後端工程師 |
| 3 | 確認並修正 pom.xml Spring Boot 版本（應為 `3.x.x`） | 確保依賴解析樹正確 | 後端工程師 |
| 4 | 移除 `TestController` 或加上 `@Profile("dev")` | 消除不必要的公開端點 | 後端工程師 |
| 5 | 將 `show-sql=true` 移至 `application-dev.properties` | 改善生產環境日誌品質 | 後端工程師 |
=======
| 1 | 刪除 `TestSecurityConfig.java`，取消 `SecurityConfig.java` 全部註解 | 恢復基本路由保護 | 後端開發 |
| 2 | 修正 `UserController.java:33`：呼叫 `login(email, password)` 重載 | Login 邏輯正確執行 | 後端開發 |
| 3 | 修正 `CourseService.java:46`：`\|\|` 改為 `&&` | 課程建立功能恢復正常 | 後端開發 |
| 4 | 實作 `JwtService`（generateToken / validateToken / extractUserId） | 建立 Token 基礎建設 | 後端開發 |
| 5 | 將所有 `System.out.println` 替換為 `SLF4J Logger` | 可控 log level、可關閉 | 後端開發 |
>>>>>>> upstream/feature/Review

### 短期改善（1-3 個月，局部重構）

| 優先序 | 行動項目 | 預期效益 | 複雜度 |
|--------|---------|---------|--------|
<<<<<<< HEAD
| 1 | 啟用 Flyway 並整理現有 SQL 腳本為版控遷移 | 統一多人開發的 DB schema 狀態 | 中 |
| 2 | 統一 Controller 回應格式（建立 `ApiResponse<T>` wrapper DTO） | API 規格一致，前端整合簡化 | 中 |
| 3 | 為 `BookingService`、`OrderService` 補充 Service 層單元測試（使用 Mockito） | 提升業務邏輯可信度，易於 CI 快速執行 | 中 |
| 4 | 建立 `WalletService` 封裝錢包業務邏輯 | 錢包操作一致性，減少散落的直接 Repository 存取 | 中 |
| 5 | 修正 `SecurityConfig` 白名單，教師 API 的 PUT/DELETE 需加認證 | 修復存取控制漏洞 | 小 |
| 6 | 將 role（1/2/3）、subject（11/21/31）、status 包裝為 Enum | 提升可讀性，消除 magic number | 小 |
=======
| 1 | 從 JWT Token 取得當前使用者 ID，移除 Request Body 傳 userId | 消除 IDOR 漏洞 | 中 |
| 2 | 建立 `RegisterReq` / `LoginReq` DTO，分離 Entity 與 Request 模型 | 避免過度暴露欄位 | 小 |
| 3 | 啟用 Flyway 並補齊初始 migration script | 版控 schema，降低部署風險 | 中 |
| 4 | 為 Order.status 等狀態值建立 Enum | 消除 magic numbers，提升可讀性 | 小 |
| 5 | 為 `UserService`、`CourseService`、`CheckoutService` 補充 Unit Test | 防止 bug 重現，提升覆蓋率至 ≥50% | 中 |
| 6 | 限縮 CORS origin 至前端實際 domain | 減少跨站攻擊面 | 小 |
>>>>>>> upstream/feature/Review

### 中期重構（3-6 個月，架構調整）

| 優先序 | 行動項目 | 預期效益 | 複雜度 |
|--------|---------|---------|--------|
<<<<<<< HEAD
| 1 | 為 `CourseService.getAllCourses()` 加入分頁支援 | 防止隨資料增長的效能退化 | 中 |
| 2 | 導入 Spring Profiles（dev / prod）完整分離設定 | 環境隔離，減少設定錯誤風險 | 中 |
| 3 | 補充 Swagger `@Operation` / `@ApiResponse` 至所有 Controller | 自動生成完整 API 文件，改善前後端溝通 | 高（工時大） |
| 4 | 導入 Structured Logging（logback JSON）+ Actuator metrics export | 建立可觀測性基礎設施 | 中 |

### 長期演進（6 個月以上，策略方向）

- **模組化強化：** 依業務域（`booking`、`course`、`user`、`payment`）切分子套件，為未來微服務化準備邊界
- **支付系統完整化：** `PaymentService` 需對接實際支付閘道（如綠界 ECPay），加入冪等性保護防止重複扣款
- **快取層：** 對 `getAllCourses()`、`getAverageRating()` 等讀多寫少的查詢加入 Redis 快取
- **非同步通知：** 預約確認 Email 改為非同步（Spring `@Async` 或 MQ），避免阻塞 HTTP 回應
=======
| 1 | 建立 CI/CD Pipeline（GitHub Actions：build + test + lint） | 自動化品質保護，早期發現 bug | 中 |
| 2 | 設定 Spring Profiles（dev / prod），隔離設定 | 避免開發設定流入生產 | 小 |
| 3 | 合併 `Review` / `Reviews` 重複 Entity | 降低認知負擔 | 小 |
| 4 | CheckoutService 超賣防護加入資料庫樂觀鎖（`@Version`） | 高並發場景下確保數據一致 | 中 |
| 5 | 整合測試覆蓋率達到核心業務 ≥ 80% | 大幅降低回歸 bug | 高 |

### 長期演進（6 個月以上，策略方向）

- 建立完整可觀測性基礎設施：Actuator → Micrometer → Prometheus + Grafana，設定 API 延遲與錯誤率 Alert
- WebSocket Broker 從 SimpleBroker 遷移至 RabbitMQ/Redis，支援多實例部署
- 導入 API 版本管理（`/api/v1/`），為未來向後相容打底
>>>>>>> upstream/feature/Review

---

### 投資回報分析

| 行動類型 | 預估投入 | 預估回報 |
|---------|---------|---------|
<<<<<<< HEAD
| 立即行動（安全修復 + JWT）| 5-7 人天 | 修復 3 個高風險安全漏洞，認證機制完整 |
| 短期重構（統一格式 + 測試 + Flyway）| 8-12 人天 | API 文件品質提升，Service 測試覆蓋率從 0% 至 ~60% |
| 中期架構改善（分頁 + Profiles + 文件）| 10-15 人天 | 系統可維護性顯著提升，預計 bug 率降低 ~30% |

---

## Phase 1-6 附錄：完整架構圖

### 系統架構概覽

```
Client (HTTP/WebSocket)
        │
        ▼
┌─────────────────────────────────────────────────────┐
│                   Spring Boot App                    │
│                                                      │
│  ┌──────────────────────────────────────────────┐   │
│  │         Security Filter Chain                │   │
│  │  [JWT Filter - 未完成] → CORS → Auth          │   │
│  └──────────────────────────────────────────────┘   │
│                                                      │
│  ┌────────────┐  ┌────────────┐  ┌───────────────┐  │
│  │ REST APIs  │  │ WebSocket  │  │  Email/Notif  │  │
│  │ 14 Ctrls  │  │ STOMP/SJS  │  │  EmailService │  │
│  └─────┬──────┘  └─────┬──────┘  └───────────────┘  │
│        │               │                              │
│  ┌─────▼───────────────▼──────────────────────────┐  │
│  │              Service Layer (13)                 │  │
│  │  UserSvc | CourseSvc | BookingSvc | OrderSvc   │  │
│  │  ChatMsgSvc | ReviewSvc | FeedbackSvc          │  │
│  │  TutorProfileSvc | WalletLog(無Service) | ...  │  │
│  └─────┬──────────────────────────────────────────┘  │
│        │                                              │
│  ┌─────▼──────────────────────────────────────────┐  │
│  │            Repository Layer (10)               │  │
│  │         Spring Data JPA Repositories           │  │
│  └─────┬──────────────────────────────────────────┘  │
│        │                                              │
└────────┼─────────────────────────────────────────────┘
         │
         ▼
    MySQL 8+ (demodb2)
    10 Tables: users, courses, bookings, orders,
    chat_messages, tutors, tutor_schedules,
    reviews, lesson_feedback, wallet_logs
```

### 資料流：課程預約流程

```
POST /api/bookings
    │
    ▼
BookingController.sendBooking(BookingReq)
    │
    ▼
BookingService.sendBooking(BookingReq)
    ├─ [驗證] userId, courseId, lessonCount
    ├─ [價格計算] lessonCount >= 10 → 95% discount
    ├─ OrderRepository.save(新 Order)
    └─ BookingRepository.save(新 Booking)
    │
    ▼
回傳 200 OK (Map or DTO)
    │
    ▼（非同步，待實作）
EmailService.sendBookingEmail(EmailBookingDTO)
    └─ JavaMailSender.send(MimeMessage)
```

---

*此報告由 Claude Code 靜態分析產出，效能數據為推斷值。建議搭配 `mvn dependency-check:check` 和 APM 工具取得更精確的依賴漏洞與效能數據。*
=======
| 立即行動（安全 + Bug 修復）| 2 人天 | 恢復 5 個功能正確性，消除 3 個高風險安全漏洞 |
| 短期重構 | 5 人天 | 消除 IDOR 漏洞，預計開發速度提升 20%（減少 debug 時間）|
| 測試補強 | 4 人天 | 預計 bug 率降低 40%，新功能上線信心提升 |
| CI/CD 建立 | 2 人天 | 防止未來的 regression，降低 code review 成本 |

---

## 附錄｜技術債詳細觀察

### CourseService 邏輯 Bug 說明

```java
// CourseService.java:46（現狀 - 永遠為 true）
if (courseReq.getSubject()!=1 || courseReq.getSubject()!=2) return false;

// 正確寫法
if (courseReq.getSubject()!=1 && courseReq.getSubject()!=2) return false;
```

### Login 重載問題說明

```java
// UserService.java:69-73（錯誤重載 - 永遠 return true）
public boolean login(User user){
    if (user == null) return false;
    return true; // ← 永遠成功！
}

// UserService.java:75-86（正確重載 - 有驗證密碼）
public boolean login(String email, String password){ ... }

// UserController.java:33（目前呼叫錯誤重載）
if (!userService.login(user)){ ... }
// 應改為：
if (!userService.login(user.getEmail(), user.getPassword())){ ... }
```

### CheckoutService 潛在超賣風險說明

雖然 `@Transactional` 保證原子性，但「SELECT 後 INSERT」的 check-then-act 模式在高並發下，兩個請求可能同時通過檢查，再同時 INSERT，導致同一時段被雙重預約。
**建議：** 在 `Booking` 表對 `(tutor_id, date, hour)` 加入 `UNIQUE INDEX`，或使用 `SELECT ... FOR UPDATE` 悲觀鎖。

---

> 分析限制：因未取得監控數據與正式環境資訊，效能評估為靜態推斷。資料庫 schema 未直接存取，以 Entity class 反推。
>>>>>>> upstream/feature/Review

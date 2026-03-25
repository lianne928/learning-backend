-- ============================================================
-- 五名新老師假資料 (id 11~15)
-- 生成時間: 2026-03-25
-- ============================================================

SET FOREIGN_KEY_CHECKS = 0;
-- ============================================================
-- 老師測試文件
-- ============================================================
-- 測試頭像4:https://drive.google.com/file/d/1PRcsio0qw1es3Eph11SZGKhiBRUVdWFy/view?usp=drive_link
-- 測試頭像5:https://drive.google.com/file/d/1JG-R3qN1Ud24vm4eg8Sg3MURn9EzOwNw/view?usp=drive_link
-- 測試頭像6:https://drive.google.com/file/d/17RHPU4vqmk0yXNQABjSROzJIDMRzFfz3/view?usp=drive_link
-- 測試頭像7:https://drive.google.com/file/d/1TAY1ow8NC9yqSeJ7z9cqUC7egMHU1RfV/view?usp=drive_link
-- 測試頭像8:https://drive.google.com/file/d/1qWmOham47s_YF69aMK1m_njcxbHRbw0v/view?usp=drive_link
-- 測試證照4:https://drive.google.com/file/d/1aCGYv6GldHtweRgnSR3nrTkH_bPZzMSy/view?usp=drive_link
-- 測試證照5:https://drive.google.com/file/d/1HBUib8fAq7S3u9eXFuPH-If0PDEMs0gd/view?usp=drive_link
-- 測試證照6:https://drive.google.com/file/d/1ORFO_YcMI8Ew8dmSO4PoJ2q6L309v73d/view?usp=drive_link
-- 測試證照7:https://drive.google.com/file/d/1wsMV6ZSGj_XNq2TatjzXpVyi6JJ2fFyu/view?usp=drive_link
-- 測試證照8:https://drive.google.com/file/d/1uah4kB2KU-LUycCQMsA4KgieTsb7e_xq/view?usp=drive_link
-- 自我介紹測試影片4:https://drive.google.com/file/d/12alT0Rmmd0IXXfKwiCi9xowuBoSIhlqM/view?usp=drive_link
-- 自我介紹測試影片5:https://drive.google.com/file/d/1-Tq3AgPZfVFccqzlbYsI9utqkC2z30KW/view?usp=drive_link
-- 自我介紹測試影片6:https://drive.google.com/file/d/1eTHX5sb3TjP_mdRZZ6DTb-BQUtr-24tt/view?usp=drive_link
-- 自我介紹測試影片7:https://drive.google.com/file/d/1qNsC0KKaDhjr_BJmXQhqeX52MgqE2U8w/view?usp=drive_link
-- 自我介紹測試影片8:https://drive.google.com/file/d/177EteMAeyLa5lBpR1OYuNT3MU_yktYxw/view?usp=drive_link


-- ============================================================
-- users (role: 2=老師) password="12345678"
-- ============================================================
INSERT INTO `users` (`id`, `name`, `email`, `password`, `birthday`, `role`, `wallet`, `created_at`, `updated_at`)
VALUES
(11, '許志遠', 'zhiyuan@example.com', '$2a$10$3XT3q7wUNnc8g6aNX6TbkOfs2uI9bUjh/sAxHj2QeTyufUVPV6O.W', '1987-06-15', 2,
 22000, '2025-07-20 09:00:00', '2026-03-16 08:00:00'),
(12, '蔡佩珊', 'peishan@example.com', '$2a$10$3XT3q7wUNnc8g6aNX6TbkOfs2uI9bUjh/sAxHj2QeTyufUVPV6O.W', '1992-04-03', 2,
 14500, '2025-08-10 10:00:00', '2026-03-16 08:00:00'),
(13, '黃俊傑', 'junjie@example.com', '$2a$10$3XT3q7wUNnc8g6aNX6TbkOfs2uI9bUjh/sAxHj2QeTyufUVPV6O.W', '1990-11-22', 2,
 11200, '2025-09-05 11:00:00', '2026-03-16 08:00:00'),
(14, '林宜婷', 'yiting@example.com', '$2a$10$3XT3q7wUNnc8g6aNX6TbkOfs2uI9bUjh/sAxHj2QeTyufUVPV6O.W', '1997-02-14', 2,
 4600, '2025-11-01 14:00:00', '2026-03-16 08:00:00'),
(15, '陳冠宇', 'guanyu@example.com', '$2a$10$3XT3q7wUNnc8g6aNX6TbkOfs2uI9bUjh/sAxHj2QeTyufUVPV6O.W', '1994-08-09', 2,
 2300, '2025-12-15 13:00:00', '2026-03-16 08:00:00');

-- ============================================================
-- tutors (id 對應 users.id，status: 1=pending 2=qualified 3=停權)
-- ============================================================
INSERT INTO `tutors` (`id`, `apply_date`, `avatar`, `title`, `intro`, `certificate_1`, `certificate_name_1`,
                      `certificate_2`, `certificate_name_2`, `video_url_1`, `video_url_2`, `bank_code`, `bank_account`,
                      `status`)
VALUES
(11, '2025-07-20', 'https://drive.google.com/file/d/1PRcsio0qw1es3Eph11SZGKhiBRUVdWFy/view?usp=drive_link',
 '多益 TOEIC 衝刺名師',
 '擁有 8 年 TOEIC 備考教學經驗，學生平均進步 150 分，課程涵蓋聽讀雙科精準訓練與考試策略。',
 'https://drive.google.com/file/d/1aCGYv6GldHtweRgnSR3nrTkH_bPZzMSy/view?usp=drive_link', 'TOEIC 990 滿分認證',
 NULL, NULL,
 'https://drive.google.com/file/d/12alT0Rmmd0IXXfKwiCi9xowuBoSIhlqM/view?usp=drive_link',
 NULL,
 '007', '234567890123', 2),

(12, '2025-08-10', 'https://drive.google.com/file/d/1JG-R3qN1Ud24vm4eg8Sg3MURn9EzOwNw/view?usp=drive_link',
 '高年級英語作文與口說教練',
 '政治大學英文系畢業，專攻小學高年級英語寫作與口說，擅長引導學生發展邏輯表達與創意寫作。',
 'https://drive.google.com/file/d/1HBUib8fAq7S3u9eXFuPH-If0PDEMs0gd/view?usp=drive_link', '政治大學英國語文學系',
 NULL, NULL,
 'https://drive.google.com/file/d/1-Tq3AgPZfVFccqzlbYsI9utqkC2z30KW/view?usp=drive_link', NULL,
 '011', '345678901234', 2),

(13, '2025-09-05', 'https://drive.google.com/file/d/17RHPU4vqmk0yXNQABjSROzJIDMRzFfz3/view?usp=drive_link',
 'YLE Movers／Flyers 備考專師',
 '專攻劍橋兒童英語 YLE Movers 與 Flyers 備考，課程系統化整合字彙、聽力與口說，通關率高。',
 'https://drive.google.com/file/d/1ORFO_YcMI8Ew8dmSO4PoJ2q6L309v73d/view?usp=drive_link', 'TKT 劍橋教師認證',
 NULL, NULL,
 'https://drive.google.com/file/d/1eTHX5sb3TjP_mdRZZ6DTb-BQUtr-24tt/view?usp=drive_link', NULL,
 '108', '456789012345', 2),

(14, '2025-11-01', 'https://drive.google.com/file/d/1TAY1ow8NC9yqSeJ7z9cqUC7egMHU1RfV/view?usp=drive_link',
 '國中英語會話與口試訓練師',
 '輔仁大學英文系畢業，擅長國中英語口說訓練與英語口試模擬，幫助學生建立開口說英文的自信。',
 'https://drive.google.com/file/d/1wsMV6ZSGj_XNq2TatjzXpVyi6JJ2fFyu/view?usp=drive_link', '輔仁大學英文學系',
 NULL, NULL,
 'https://drive.google.com/file/d/1qNsC0KKaDhjr_BJmXQhqeX52MgqE2U8w/view?usp=drive_link', NULL,
 '812', '567890123456', 1),

(15, '2025-12-15', 'https://drive.google.com/file/d/1qWmOham47s_YF69aMK1m_njcxbHRbw0v/view?usp=drive_link',
 '英語故事創作與寫作老師',
 '東吳大學英文系畢業，熱愛兒童文學，以英語故事創作引領學生進入閱讀與寫作的世界，適合各年級。',
 'https://drive.google.com/file/d/1uah4kB2KU-LUycCQMsA4KgieTsb7e_xq/view?usp=drive_link', '東吳大學英文學系',
 NULL, NULL,
 'https://drive.google.com/file/d/177EteMAeyLa5lBpR1OYuNT3MU_yktYxw/view?usp=drive_link', NULL,
 '103', '678901234567', 1);

-- ============================================================
-- courses
-- subject: 11=低年級 12=中年級 13=高年級 21=GEPT 22=YLE 23=國中先修 31=其他
-- ============================================================
INSERT INTO `courses` (`id`, `tutor_id`, `name`, `subject`, `description`, `price`, `is_active`)
VALUES
-- 許志遠 (tutor_id=11)
(11, 11, 'TOEIC 聽讀雙科全攻略', 31, '系統整合 TOEIC 聽力與閱讀兩大科目，含題型解析與模擬測驗，目標 750 分以上。', 900, 1),
(12, 11, 'TOEIC 聽力高分特訓班', 31, '針對 TOEIC Part 1–4 精準訓練，強化語音辨識與長對話理解能力。', 850, 1),
(13, 11, '體驗課：TOEIC 閱讀策略', 31, '30 分鐘體驗，介紹 TOEIC 閱讀解題策略與時間分配技巧。', 200, 1),
-- 蔡佩珊 (tutor_id=12)
(14, 12, '高年級英語作文入門班', 13, '從段落寫作開始，教導學生英語寫作架構、連接詞使用與例句仿寫。', 720, 1),
(15, 12, '高年級英語口說自信班', 13, '情境對話練習為主，讓高年級學生在模擬情境中開口說英文，增強口說流利度。', 750, 1),
-- 黃俊傑 (tutor_id=13)
(16, 13, 'YLE Movers 完整備考班', 22, '涵蓋 YLE Movers 五大題型完整備考，字彙量建立與聽說讀寫全面提升。', 780, 1),
(17, 13, 'YLE Flyers 衝刺精修班', 22, '針對 YLE Flyers 高難度題型強化訓練，適合已通過 Movers 的進階學生。', 820, 1),
(18, 13, '體驗課：YLE 單字與聽力', 22, '30 分鐘體驗，介紹 YLE 常考字彙與聽力技巧，了解備考方向。', 200, 1),
-- 林宜婷 (tutor_id=14)
(19, 14, '國中英語口說全攻略', 23, '模擬國中口試情境，訓練學生在問答、對話與短篇朗讀中流利表達。', 700, 1),
(20, 14, '國中英語日常會話班', 23, '以日常生活情境設計對話課程，讓學生在輕鬆氛圍中練習實用英語口說。', 650, 1),
-- 陳冠宇 (tutor_id=15)
(21, 15, '英語故事繪本寫作班', 31, '引導學生以英語創作短篇故事與繪本腳本，培養創意表達與寫作能力。', 680, 1),
(22, 15, '英語閱讀理解與賞析班', 31, '精選英語童書與短篇故事，訓練閱讀理解策略與文本分析能力。', 640, 1);

-- ============================================================
-- tutor_schedules (weekday: 1=一~7=日, hour: 9~21)
-- ============================================================
INSERT INTO `tutor_schedules` (`id`, `tutor_id`, `weekday`, `hour`, `is_available`)
VALUES
-- 許志遠 (tutor_id=11)：週一三五 19~21點，週六 10~13點
(67, 11, 1, 19, 1),
(68, 11, 1, 20, 1),
(69, 11, 1, 21, 1),
(70, 11, 3, 19, 1),
(71, 11, 3, 20, 1),
(72, 11, 3, 21, 1),
(73, 11, 5, 19, 1),
(74, 11, 5, 20, 1),
(75, 11, 5, 21, 1),
(76, 11, 6, 10, 1),
(77, 11, 6, 11, 1),
(78, 11, 6, 12, 1),
(79, 11, 6, 13, 0),
-- 蔡佩珊 (tutor_id=12)：週二四 14~18點，週日 9~13點
(80, 12, 2, 14, 1),
(81, 12, 2, 15, 1),
(82, 12, 2, 16, 1),
(83, 12, 2, 17, 1),
(84, 12, 2, 18, 1),
(85, 12, 4, 14, 1),
(86, 12, 4, 15, 1),
(87, 12, 4, 16, 1),
(88, 12, 4, 17, 1),
(89, 12, 4, 18, 0),
(90, 12, 7, 9, 1),
(91, 12, 7, 10, 1),
(92, 12, 7, 11, 1),
(93, 12, 7, 12, 1),
-- 黃俊傑 (tutor_id=13)：週二六 10~13點 & 15~18點
(94, 13, 2, 10, 1),
(95, 13, 2, 11, 1),
(96, 13, 2, 12, 1),
(97, 13, 2, 15, 1),
(98, 13, 2, 16, 1),
(99, 13, 2, 17, 1),
(100, 13, 6, 10, 1),
(101, 13, 6, 11, 1),
(102, 13, 6, 12, 1),
(103, 13, 6, 15, 1),
(104, 13, 6, 16, 1),
(105, 13, 6, 17, 0),
-- 林宜婷 (tutor_id=14)：週一三五 16~19點
(106, 14, 1, 16, 1),
(107, 14, 1, 17, 1),
(108, 14, 1, 18, 1),
(109, 14, 3, 16, 1),
(110, 14, 3, 17, 1),
(111, 14, 3, 18, 1),
(112, 14, 5, 16, 1),
(113, 14, 5, 17, 1),
(114, 14, 5, 18, 0),
-- 陳冠宇 (tutor_id=15)：週二四 15~18點，週六 14~17點
(115, 15, 2, 15, 1),
(116, 15, 2, 16, 1),
(117, 15, 2, 17, 1),
(118, 15, 4, 15, 1),
(119, 15, 4, 16, 1),
(120, 15, 4, 17, 1),
(121, 15, 6, 14, 1),
(122, 15, 6, 15, 1),
(123, 15, 6, 16, 0);

SET FOREIGN_KEY_CHECKS = 1;

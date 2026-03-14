-- Migration: 新增 education 欄位到 tutors 資料表
-- 執行時機：在啟動新版後端之前先執行此 SQL
ALTER TABLE tutors ADD COLUMN education VARCHAR(100) NULL COMMENT '最高學歷';

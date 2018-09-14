/**
交通事故数据库升级脚本。
适用范围：从 0.2.8 升级到 0.2.9
@author RJ
*/
-- 事故报案
alter table gf_accident_draft rename column overdue to overdue_draft;
alter table gf_accident_draft rename column report_time to draft_time;

-- 事故登记
alter table gf_accident_register rename column overdue to overdue_register;
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

-- 当事车辆
alter table gf_accident_car rename column tow_money to guess_tow_money;
comment on column gf_accident_car.guess_tow_money is '预估拖车费（元）';
alter table gf_accident_car rename column repair_money to guess_repair_money;
comment on column gf_accident_car.guess_repair_money is '预估维修费（元）';
alter table gf_accident_car add column actual_tow_money decimal(10, 2);
comment on column gf_accident_car.actual_tow_money is '实际拖车费（元）';
alter table gf_accident_car add column actual_repair_money decimal(10, 2);
comment on column gf_accident_car.actual_repair_money is '实际维修费（元）';
alter table gf_accident_car drop damage_money;
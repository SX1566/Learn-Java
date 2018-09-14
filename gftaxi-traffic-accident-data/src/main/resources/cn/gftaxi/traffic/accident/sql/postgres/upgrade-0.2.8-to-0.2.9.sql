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

-- 当事人
alter table gf_accident_people rename column treatment_money to guess_treatment_money;
comment on column gf_accident_people.guess_treatment_money is '预估医疗费（元）';
alter table gf_accident_people rename column compensate_money to guess_compensate_money;
comment on column gf_accident_people.guess_compensate_money is '预估赔偿损失（元）';
alter table gf_accident_people add column actual_treatment_money decimal(10, 2);
comment on column gf_accident_people.actual_treatment_money is '实际医疗费（元）';
alter table gf_accident_people add column actual_compensate_money decimal(10, 2);
comment on column gf_accident_people.actual_compensate_money is '实际赔偿损失（元）';
alter table gf_accident_people drop damage_money;

-- 其他物体
alter table gf_accident_other rename column damage_money to guess_money;

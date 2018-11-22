-- 交通事故数据库删表脚本

-- drop functions
drop function if exists cn_first_char(varchar);

-- drop tables
drop table if exists gf_accident_car;
drop table if exists gf_accident_people;
drop table if exists gf_accident_other;
drop table if exists gf_accident_situation;
drop table if exists gf_accident_case;
-- old
drop table if exists gf_accident_operation;
drop table if exists gf_accident_register;
drop table if exists gf_accident_draft;

-- drop views
drop view if exists bs_car;
drop view if exists bs_carman;
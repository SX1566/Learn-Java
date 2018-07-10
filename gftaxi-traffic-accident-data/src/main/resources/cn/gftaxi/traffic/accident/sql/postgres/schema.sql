-- 交通事故数据库构建脚本

-- drop tables/sequences
drop table if exists gf_accident_draft;

-- create tables
create table gf_accident_draft (
  code        varchar(11)  primary key,
  status      smallint     not null,
  car_plate   varchar(8)   not null,
  driver_name varchar(8)   not null,
  happen_time timestamp    not null,
  report_time timestamp    not null,
  location    varchar(100) not null,
  hit_form    varchar(50)  not null,
  hit_type    varchar(50)  not null,
  overdue     boolean      not null,
  source      varchar(10)  not null,
  author_name varchar(50)  not null,
  author_id   varchar(50)  not null,
  describe    text,
  constraint uk_gf_accident_draft__car_plate_happen_time unique (car_plate, happen_time)
);
comment on table gf_accident_draft              is '事故报案';
comment on column gf_accident_draft.code        is '事故编号，格式为 yyyyMMdd_nn';
comment on column gf_accident_draft.status      is '状态：1-待登记、2-已登记';
comment on column gf_accident_draft.car_plate   is '车号，如 "粤A123456"';
comment on column gf_accident_draft.driver_name is '当事司机姓名';
comment on column gf_accident_draft.happen_time is '事发时间';
comment on column gf_accident_draft.report_time is '报案时间';
comment on column gf_accident_draft.location    is '事发地点';
comment on column gf_accident_draft.hit_form    is '事故形态';
comment on column gf_accident_draft.hit_type    is '碰撞类型';
comment on column gf_accident_draft.overdue     is '是否逾期报案';
comment on column gf_accident_draft.source      is '报案来源：BC-BC系统Web端、EMAIL-邮件、WEIXIN-微信、SMS-短信、{appId}-应用ID';
comment on column gf_accident_draft.author_name is '接案人姓名';
comment on column gf_accident_draft.author_id   is '接案人标识：邮件报案为邮箱、短信报案为手机号、其余为对应的登陆账号';
comment on column gf_accident_draft.describe    is '简要描述';
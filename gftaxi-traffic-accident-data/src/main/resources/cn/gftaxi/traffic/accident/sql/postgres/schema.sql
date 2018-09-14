-- 交通事故数据库构建脚本

-- create extension
create extension if not exists dblink;

-- drop tables/sequences
drop table if exists gf_accident_operation;
drop table if exists gf_accident_car;
drop table if exists gf_accident_people;
drop table if exists gf_accident_other;
drop table if exists gf_accident_register;
drop table if exists gf_accident_draft;

-- create tables
create table gf_accident_operation (
  id              serial primary key,
  operator_id     int         not null,
  operator_name   varchar(50) not null,
  operate_time    timestamptz not null,
  operation_type  smallint    not null,
  target_type     smallint    not null,
  target_id       int         not null,
  tag             smallint    not null default 0,
  comment         text,
  attachment_id   varchar(36),
  attachment_name varchar(100)
);
comment on table gf_accident_operation                  is '事故相关事务操作记录';
comment on column gf_accident_operation.operator_id     is '操作人ID';
comment on column gf_accident_operation.operator_name   is '操作人姓名';
comment on column gf_accident_operation.operate_time    is '操作时间';
comment on column gf_accident_operation.operation_type  is '操作类型：10-创建、20-修改、30-提交、40-审核通过、50-审核不通过、90-删除';
comment on column gf_accident_operation.target_type     is '事务类型：10-事故报案、20-事故登记、30-事故报告、40-事故处理、50-事故结案';
comment on column gf_accident_operation.target_id       is '事务ID，如事故登记ID';
comment on column gf_accident_operation.tag             is '操作类型的扩展标记';
comment on column gf_accident_operation.comment         is '操作结果描述，如审核意见、审计日志描述等';
comment on column gf_accident_operation.attachment_id   is '附件ID';
comment on column gf_accident_operation.attachment_name is '附件名称';

-- create tables
create table gf_accident_draft (
  id             serial        primary key,
  code           varchar(11)  unique,
  status         smallint     not null,
  motorcade_name varchar(10),
  car_plate      varchar(10)  not null,
  driver_name    varchar(10)  not null,
  happen_time    timestamptz   not null,
  draft_time     timestamptz   not null,
  location       varchar(100) not null,
  hit_form       varchar(50)  not null,
  hit_type       varchar(50)  not null,
  overdue_draft  boolean       not null,
  source         varchar(10)  not null,
  author_name    varchar(50)  not null,
  author_id      varchar(50)  not null,
  describe       text,
  constraint gf_accident_draft_car_plate_happen_time_ukey unique (car_plate, happen_time)
);
comment on table gf_accident_draft                 is '事故报案';
comment on column gf_accident_draft.code           is '事故编号，格式为 yyyyMMdd_nn';
comment on column gf_accident_draft.status         is '状态：1-待登记、2-已登记';
comment on column gf_accident_draft.motorcade_name is '事发车队名称';
comment on column gf_accident_draft.car_plate      is '车号，如 "粤A123456"';
comment on column gf_accident_draft.driver_name    is '当事司机姓名';
comment on column gf_accident_draft.happen_time    is '事发时间';
comment on column gf_accident_draft.draft_time     is '报案时间';
comment on column gf_accident_draft.location       is '事发地点';
comment on column gf_accident_draft.hit_form       is '事故形态';
comment on column gf_accident_draft.hit_type       is '碰撞类型';
comment on column gf_accident_draft.overdue_draft  is '是否逾期报案';
comment on column gf_accident_draft.source         is '报案来源：BC-BC系统Web端、EMAIL-邮件、WEIXIN-微信、SMS-短信、{appId}-应用ID';
comment on column gf_accident_draft.author_name    is '接案人姓名';
comment on column gf_accident_draft.author_id      is '接案人标识：邮件报案为邮箱、短信报案为手机号、其余为对应的登陆账号';
comment on column gf_accident_draft.describe       is '简要描述';

create table gf_accident_register (
  id                            integer primary key references gf_accident_draft on delete no action,
  status                        smallint     not null,
  motorcade_name                varchar(10),
  -- 车辆信息
  car_id                        int,
  car_plate                     varchar(10)  not null,
  -- 司机信息
  driver_id                     int,
  driver_name                   varchar(8)   not null,
  driver_type                   varchar(50),
  driver_linkman_name           varchar(50),
  driver_linkman_phone          varchar(50),
  -- 事故信息
  happen_time                   timestamptz  not null,
  describe                      text,
  register_time                 timestamptz,
  overdue_register              boolean,
  -- 事发地点
  location_level1               varchar(50),
  location_level2               varchar(50),
  location_level3               varchar(50),
  location                      varchar(255) not null,
  gps_speed                     smallint,
  -- 处理部门相关
  deal_department               varchar(50),
  deal_way                      varchar(50),
  -- 保险相关
  insurance_company             varchar(50),
  insurance_code                varchar(50),
  -- 分类信息
  level                         varchar(50),
  load_state                    varchar(50),
  hit_form                      varchar(50),
  hit_type                      varchar(50),
  weather                       varchar(50),
  driving_direction             varchar(50),
  light                         varchar(50),
  road_type                     varchar(50),
  road_structure                varchar(50),
  road_state                    varchar(50),
  -- 车辆冗余字段信息
  car_model                     varchar(50),
  car_operate_date              date,
  car_contract_type             varchar(50),
  car_contract_drivers          varchar(50),
  -- 司机冗余字段信息
  driver_hired_date             date,
  driver_phone                  varchar(50),
  driver_identity_code          varchar(50),
  driver_service_code           varchar(50),
  driver_origin                 varchar(255),
  driver_age                    decimal(4, 1),
  driver_license_date           date,
  driver_drive_years            decimal(4, 1),
  driver_pic_id                 varchar(50),
  -- 历史统计
  history_accident_count        smallint,
  history_traffic_offence_count smallint,
  history_service_offence_count smallint,
  history_complain_count        smallint,
  -- 车号+事发时间唯一
  constraint gf_accident_register_car_plate_happen_time_ukey unique (car_plate, happen_time)
);
comment on table gf_accident_register                       is '事故登记';
comment on column gf_accident_register.status               is '状态：1-待登记、2-待审核、4-审核不通过、8-审核通过';
comment on column gf_accident_register.motorcade_name       is '事发车队名称';
-- 车辆信息
comment on column gf_accident_register.car_id               is '车辆 ID，对应 BC 系统车辆 ID';
comment on column gf_accident_register.car_plate            is '事故车号，如 "粤A.12345';
comment on column gf_accident_register.car_model            is '车辆/车型';
comment on column gf_accident_register.car_operate_date     is '车辆/投产日期';
comment on column gf_accident_register.car_contract_type    is '车辆/合同性质';
comment on column gf_accident_register.car_contract_drivers is '车辆/承包司机';
-- 司机信息
comment on column gf_accident_register.driver_name          is '当事司机姓名';
comment on column gf_accident_register.driver_id            is '当事司机 ID，对应 BC 系统司机 ID，非编司机则为 null';
comment on column gf_accident_register.driver_type          is '司机/驾驶状态，Official-正班、Shift-替班、Outside-非编';
comment on column gf_accident_register.driver_linkman_name  is '司机/紧急联系人姓名';
comment on column gf_accident_register.driver_linkman_phone is '司机/紧急联系人电话';
comment on column gf_accident_register.driver_hired_date    is '司机/入职日期';
comment on column gf_accident_register.driver_phone         is '司机/联系电话';
comment on column gf_accident_register.driver_identity_code is '司机/身份证号';
comment on column gf_accident_register.driver_service_code  is '司机/服务资格证号';
comment on column gf_accident_register.driver_origin        is '司机/籍贯';
comment on column gf_accident_register.driver_age           is '司机/年龄';
comment on column gf_accident_register.driver_license_date  is '司机/初领驾证日期';
comment on column gf_accident_register.driver_drive_years   is '司机/驾龄(年)';
comment on column gf_accident_register.driver_pic_id        is '司机/图片 ID，"S:"前缀-BC司机UID、"C:"前缀-自定义图片ID';
-- 事故信息
comment on column gf_accident_register.happen_time          is '事发时间';
comment on column gf_accident_register.describe             is '事发经过';
comment on column gf_accident_register.register_time        is '登记时间，等于首次提交审核的时间';
comment on column gf_accident_register.overdue_register     is '是否逾期登记';
-- 事发地点
--     广东省/广州市/荔湾区/芳村上市路
--     北京市/市辖区/东城区/东华门街道
--     广西壮族自治区/南宁市/兴宁区/民生街道
comment on column gf_accident_register.location_level1      is '事发地点的省级';
comment on column gf_accident_register.location_level2      is '事发地点的地级';
comment on column gf_accident_register.location_level3      is '事发地点的县级';
comment on column gf_accident_register.location             is '事发地点的县级下面的详细地点';
comment on column gf_accident_register.gps_speed            is 'GPS车速，km/h';
-- 处理部门、保险公司相关
comment on column gf_accident_register.deal_department      is '处理部门';
comment on column gf_accident_register.deal_way             is '处理方式';
comment on column gf_accident_register.insurance_company    is '保险公司';
comment on column gf_accident_register.insurance_code       is '保险报案编号';
-- 分类信息
comment on column gf_accident_register.load_state           is '载重状态，如 "载客"、"空车"';
comment on column gf_accident_register.level                is '事故等级，如 "财产1级"';
comment on column gf_accident_register.hit_form             is '事故形态';
comment on column gf_accident_register.hit_type             is '碰撞类型';
comment on column gf_accident_register.weather              is '天气情况';
comment on column gf_accident_register.driving_direction    is '行驶方向';
comment on column gf_accident_register.light                is '光线条件';
comment on column gf_accident_register.road_type            is '道路类型';
comment on column gf_accident_register.road_structure       is '路面状况';
comment on column gf_accident_register.road_state           is '路表状况';
-- 历史统计
comment on column gf_accident_register.history_accident_count        is '历史事故宗数，从事发日向前推一年期间当事司机的统计，不包含本宗';
comment on column gf_accident_register.history_traffic_offence_count is '历史交通违法次数，从事发日向前推一年期间当事司机的统计';
comment on column gf_accident_register.history_service_offence_count is '历史营运违章次数，从事发日向前推一年期间当事司机的统计';
comment on column gf_accident_register.history_complain_count        is '历史服务投诉次数，从事发日向前推一年期间当事司机的统计';

create table gf_accident_car (
  id           serial primary key,
  pid          int references gf_accident_register on delete cascade,
  sn           smallint    not null,
  name         varchar(10) not null,
  type         varchar(50),
  model        varchar(50),
  tow_count    smallint,
  tow_money    decimal(10, 2),
  repair_type  varchar(50),
  repair_money decimal(10, 2),
  damage_state varchar(50),
  damage_money decimal(10, 2),
  follow_type  varchar(50),
  updated_time  timestamptz not null,
  constraint gf_accident_car_plate_happen_time_ukey unique (pid, name)
);
comment on table gf_accident_car               is '事故当事车辆';
comment on column gf_accident_car.pid          is '所属事故ID';
comment on column gf_accident_car.sn           is '同一事故内的序号';
comment on column gf_accident_car.name         is '车号，如 粤A123456';
comment on column gf_accident_car.type         is '分类：自车、三者';
comment on column gf_accident_car.model        is '车型：出租车、小轿车、...';
comment on column gf_accident_car.tow_count    is '拖车次数';
comment on column gf_accident_car.tow_money    is '拖车费（元）';
comment on column gf_accident_car.repair_type  is '维修分类：厂修、外修';
comment on column gf_accident_car.repair_money is '维修费（元）';
comment on column gf_accident_car.damage_state is '受损情况';
comment on column gf_accident_car.damage_money is '损失预估（元）';
comment on column gf_accident_car.follow_type  is '跟进形式';
comment on column gf_accident_car.updated_time is '更新时间';

create table gf_accident_people (
  id               serial primary key,
  pid              int references gf_accident_register on delete cascade,
  sn               smallint    not null,
  name             varchar(50) not null,
  type             varchar(50),
  sex              smallint    not null,
  phone            varchar(50),
  transport_type   varchar(50),
  duty             varchar(50),
  damage_state     varchar(50),
  damage_money     decimal(10, 2),
  treatment_money  decimal(10, 2),
  compensate_money decimal(10, 2),
  follow_type      varchar(50),
  updated_time     timestamptz not null,
  constraint gf_accident_people_pid_name_ukey unique (pid, name)
);
comment on table gf_accident_people                   is '事故当事人';
comment on column gf_accident_people.pid              is '所属事故ID';
comment on column gf_accident_people.sn               is '同一事故内的序号';
comment on column gf_accident_people.name             is '姓名';
comment on column gf_accident_people.type             is '分类：自车、三者';
comment on column gf_accident_people.sex              is '性别：0-未设置,1-男,2-女';
comment on column gf_accident_people.phone            is '联系电话';
comment on column gf_accident_people.transport_type   is '交通方式';
comment on column gf_accident_people.duty             is '事故责任';
comment on column gf_accident_people.damage_state     is '伤亡情况';
comment on column gf_accident_people.damage_money     is '损失预估（元）';
comment on column gf_accident_people.treatment_money  is '医疗费用（元）';
comment on column gf_accident_people.compensate_money is '赔偿损失（元）';
comment on column gf_accident_people.follow_type      is '跟进形式';
comment on column gf_accident_people.updated_time     is '更新时间';

create table gf_accident_other (
  id            serial primary key,
  pid           int references gf_accident_register on delete cascade,
  sn            smallint    not null,
  name          varchar(50) not null,
  type          varchar(50),
  belong        varchar(50),
  linkman_name  varchar(50),
  linkman_phone varchar(50),
  damage_state  varchar(50),
  damage_money  decimal(10, 2),
  actual_money  decimal(10, 2),
  follow_type   varchar(50),
  updated_time  timestamptz not null,
  constraint gf_accident_other_pid_name_ukey unique (pid, name)
);
comment on table gf_accident_other                is '事故其他物体';
comment on column gf_accident_other.pid           is '所属事故ID';
comment on column gf_accident_other.sn            is '同一事故内的序号';
comment on column gf_accident_other.name          is '物品名称';
comment on column gf_accident_other.type          is '物品分类';
comment on column gf_accident_other.belong        is '归属';
comment on column gf_accident_other.linkman_name  is '联系人';
comment on column gf_accident_other.linkman_phone is '联系电话';
comment on column gf_accident_other.damage_state  is '受损情况';
comment on column gf_accident_other.damage_money  is '损失预估（元）';
comment on column gf_accident_other.actual_money  is '实际损失（元）';
comment on column gf_accident_other.follow_type   is '跟进形式';
comment on column gf_accident_other.updated_time  is '更新时间';

-- 获取汉字拼音首字母的大写 select cn_first_char('事故性质') > SGXZ
-- 来源：http://blog.qdac.cc/?p=1281
create or replace function cn_first_char(s varchar) returns varchar as $BODY$
  declare
    retval varchar;
    c varchar;
    l integer;
    b bytea;
    w integer;
  begin
  l = length(s);
  retval = '';
  while l > 0 loop
    c = left(s, 1);
    b = convert_to(c, 'GB18030')::bytea;
    if get_byte(b, 0) < 127 then
      retval = retval || upper(c);
    elsif length(b) = 2 then
      begin
      w = get_byte(b,0) * 256 + get_byte(b, 1);
      -- 汉字GBK编码按拼音排序，按字符数来查找
      if w between 48119 and 49061 then    --"J";48119;49061;942
        retval = retval || 'J';
      elsif w between 54481 and 55289 then --"Z";54481;55289;808
        retval = retval || 'Z';
      elsif w between 53689 and 54480 then --"Y";53689;54480;791
        retval = retval || 'Y';
      elsif w between 51446 and 52208 then --"S";51446;52208;762
        retval = retval || 'S';
      elsif w between 52980 and 53640 then --"X";52980;53640;660
        retval = retval || 'X';
      elsif w between 49324 and 49895 then --"L";49324;49895;571
        retval = retval || 'L';
      elsif w between 45761 and 46317 then --"C";45761;46317;556
        retval = retval || 'C';
      elsif w between 45253 and 45760 then --"B";45253;45760;507
        retval = retval || 'B';
      elsif w between 46318 and 46825 then --"D";46318;46825;507
        retval = retval || 'D';
      elsif w between 47614 and 48118 then --"H";47614;48118;504
        retval = retval || 'H';
      elsif w between 50906 and 51386 then --"Q";50906;51386;480
        retval = retval || 'Q';
      elsif w between 52218 and 52697 then --"T";52218;52697;479
        retval = retval || 'T';
      elsif w between 49896 and 50370 then --"M";49896;50370;474
        retval = retval || 'M';
      elsif w between 47297 and 47613 then --"G";47297;47613;316
        retval = retval || 'G';
      elsif w between 47010 and 47296 then --"F";47010;47296;286
        retval = retval || 'F';
      elsif w between 50622 and 50905 then --"P";50622;50905;283
        retval = retval || 'P';
      elsif w between 52698 and 52979 then --"W";52698;52979;281
        retval = retval || 'W';
      elsif w between 49062 and 49323 then --"K";49062;49323;261
        retval = retval || 'K';
      elsif w between 50371 and 50613 then --"N";50371;50613;242
        retval = retval || 'N';
      elsif w between 46826 and 47009 then --"E";46826;47009;183
        retval = retval || 'E';
      elsif w between 51387 and 51445 then --"R";51387;51445;58
        retval = retval || 'R';
      elsif w between 45217 and 45252 then --"A";45217;45252;35
        retval = retval || 'A';
      elsif w between 50614 and 50621 then --"O";50614;50621;7
        retval = retval || 'O';
      end if;
      end;
    end if;
    s = substring(s, 2, l-1);
    l = l-1;
  end loop;
    return retval;
  end;
$BODY$ language plpgsql immutable;

-- create views
create or replace view bs_car as
  select id, status, plate_type, plate_no, factory_type, operate_date, company, motorcade_name, charger, driver, code, manage_no
  from dblink(
    'dbname=bcsystem host=192.168.0.7 user=reader password=reader',
    'select c.id, c.status_, c.plate_type, c.plate_no, c.factory_type, c.operate_date, c.company, m.name, c.charger,
       c.driver, c.code, c.manage_no
     from bs_car c
     inner join bs_motorcade m on m.id = c.motorcade_id'
  )
  as t(
    id integer, status integer, plate_type varchar(255), plate_no varchar(255), factory_type varchar(255),
    operate_date date, company varchar(255), motorcade_name varchar(255), charger varchar(255), driver varchar(255),
    code varchar(255), manage_no integer
  );
comment on view bs_car                  is '车辆视图';
comment on column bs_car.status         is '状态：-2:新购,-1:草稿,0-在案,1-注销';
comment on column bs_car.plate_type     is '车牌归属，如"粤A"';
comment on column bs_car.plate_no       is '车牌号码，如"C4X74"';
comment on column bs_car.factory_type   is '厂牌类型，如"现代"';
comment on column bs_car.operate_date   is '投产日期';
comment on column bs_car.company        is '所属公司';
comment on column bs_car.motorcade_name is '所属车队';
comment on column bs_car.charger        is '责任人信息';
comment on column bs_car.driver         is '司机信息';
comment on column bs_car.code           is '自编号';
comment on column bs_car.manage_no      is '管理号';

create or replace view bs_carman as
  select id, uid, status, type, name, sex, origin, id_card_no, initial_license_date, model, work_date, service_cert_no, phone
  from dblink(
    'dbname=bcsystem host=192.168.0.7 user=reader password=reader',
    'select id, uid_, status_, type_, name, sex, origin, cert_identity, cert_driving_first_date, model_, work_date,
       cert_fwzg, phone
     from bs_carman'
  )
  as t(
    id integer, uid varchar(36), status integer, type integer, name varchar(255), sex integer, origin varchar(255),
    id_card_no varchar(255), initial_license_date date, model varchar(255), work_date date,
    service_cert_no varchar(255), phone varchar(255)
  );
comment on view bs_carman                        is '司机责任人视图';
comment on column bs_carman.uid                  is 'UID';
comment on column bs_carman.status               is '状态：-1:草稿,0-启用中,1-已禁用,2-已删除';
comment on column bs_carman.type                 is '类别：0-司机,1-责任人,2-司机和责任人';
comment on column bs_carman.name                 is '姓名';
comment on column bs_carman.sex                  is '性别：0-未设置,1-男,2-女';
comment on column bs_carman.origin               is '籍贯';
comment on column bs_carman.id_card_no           is '身份证号';
comment on column bs_carman.initial_license_date is '初次领证日期';
comment on column bs_carman.model                is '准驾车型';
comment on column bs_carman.work_date            is '入职日期';
comment on column bs_carman.service_cert_no      is '服务资格证号';
comment on column bs_carman.phone                is '电话';
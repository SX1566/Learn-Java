-- 交通事故数据库构建脚本

-- drop tables/sequences
drop table if exists gf_accident_draft;
-- create extension
create extension if not exists dblink;

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
  select plate_type, plate_no, factory_type, operate_date, company, motorcade, charger, driver, code, manage_no
  from dblink(
    'dbname=bcsystem host=192.168.0.7 user=reader password=reader',
    'select c.plate_type, c.plate_no, c.factory_type, c.operate_date, c.company, m.name, c.charger, c.driver, c.code, c.manage_no
     from bs_car c
     inner join bs_motorcade m on m.id = c.motorcade_id'
  )
  as t(
    plate_type varchar(255), plate_no varchar(255), factory_type varchar(255), operate_date timestamp, company varchar(255),
    motorcade varchar(255), charger varchar(255), driver varchar(255), code varchar(255), manage_no integer
  );
comment on view bs_car                is '车辆视图';
comment on column bs_car.plate_type   is '车牌归属，如"粤A"';
comment on column bs_car.plate_no     is '车牌号码，如"C4X74"';
comment on column bs_car.factory_type is '厂牌类型，如"现代"';
comment on column bs_car.operate_date is '投产日期';
comment on column bs_car.company      is '所属公司';
comment on column bs_car.motorcade    is '所属车队';
comment on column bs_car.charger      is '责任人信息';
comment on column bs_car.driver       is '司机信息';
comment on column bs_car.code         is '自编号';
comment on column bs_car.manage_no    is '管理号';

create or replace view bs_carman as
  select name, sex, origin, cert_identity, cert_driving_first_date, model_, work_date, cert_fwzg, phone
  from dblink(
    'dbname=bcsystem host=192.168.0.7 user=reader password=reader',
    'select name, sex, origin, cert_identity, cert_driving_first_date, model_, work_date, cert_fwzg, phone from bs_carman'
  )
  as t(
    name varchar(255), sex integer, origin varchar(255), cert_identity varchar(255), cert_driving_first_date timestamp,
    model_ varchar(255), work_date timestamp, cert_fwzg varchar(255), phone varchar(255)
  );
comment on view bs_carman                           is '司机责任人视图';
comment on column bs_carman.name                    is '姓名';
comment on column bs_carman.sex                     is '性别：0-未设置,1-男,2-女';
comment on column bs_carman.origin                  is '籍贯';
comment on column bs_carman.cert_identity           is '身份证号';
comment on column bs_carman.cert_driving_first_date is '初次领证日期';
comment on column bs_carman.model_                  is '准驾车型';
comment on column bs_carman.work_date               is '入职日期';
comment on column bs_carman.cert_fwzg               is '服务资格证号';
comment on column bs_carman.phone                   is '电话';
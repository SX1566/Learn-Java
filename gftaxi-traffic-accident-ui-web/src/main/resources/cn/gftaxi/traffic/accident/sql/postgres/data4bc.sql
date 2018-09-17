-- 清除插入的资源
with p(id) as ( -- 交通事故父目录
  select distinct id from bc_identity_resource where name in ('交通事故', '事故(新版)')
)
, resource(id) as (
  select id from p
  union select id from bc_identity_resource
    where name in ('事故报案', '事故登记', '事故报告', '统计报表') and belong in (select id from p)
  union select id from bc_identity_resource where name in ('事故登记汇总统计', '事故报告汇总统计')
    and belong in (select id from bc_identity_resource where name = '统计报表' and belong in (select id from p))
) -- select * from resource
, delete_role_resource(id) as ( -- 删除资源与角色的关联
  delete from bc_identity_role_resource where sid in (select id from resource)
  returning *
), delete_resource(id) as (     -- 删除资源
  delete from bc_identity_resource where id in (select id from resource)
  returning *
)
-- 清除插入的角色
, role(id) as (select id from bc_identity_role where code like 'ACCIDENT_%')
  --select * from role r inner join bc_identity_role o on o.id = r.id -- 查出要删除的角色
, delete_role_actor as (        -- 删除用户与角色的关联
  delete from bc_identity_role_actor where rid in (select id from role)
  returning *
)
, delete_role as (             -- 删除角色
  delete from bc_identity_role where id in (select id from role)
  returning *
)
, delete_group_user as (       -- 删除分公司车队长岗位中的用户
  delete from bc_identity_actor_relation ar
  where ar.type_ = 0 and ar.master_id in (
    select g.id from bc_identity_actor g where g.type_ = 3 and g.name = '分公司车队长'
  )
  returning master_id, follower_id
)
select (select count(*) from delete_role_resource) delete_role_resource
  , (select count(*) from delete_resource)          delete_resource
  , (select count(*) from delete_role_actor)        delete_role_actor
  , (select count(*) from delete_role)              delete_role
  , (select count(*) from delete_group_user)        delete_group_user;

-- 创建一分、二分公司的车队长岗位（用于简化后续车队长的用户权限配置）
-- 岗位基本配置信息
with cfg(sn, code, name, belong_unit_name) as (
         select '1007', 'fenGongSi1CDZ', '分公司车队长', '一分公司'
  union select '2007', 'fenGongSi2CDZ', '分公司车队长', '二分公司'
)
-- 创建岗位
, create_group(id) as (
  insert into bc_identity_actor (id, uid_, status_, inner_, type_, order_, code, name, pcode, pname)
    select nextval('core_sequence'), 'group.init.' || nextval('core_sequence'), 0, false, 3, c.sn, c.code, c.name,
      concat('[1]', u.pcode, '/[1]', u.code), concat(u.pname, '/', u.name)
    from cfg c
    inner join bc_identity_actor u on u.type_ = 1 and u.name = c.belong_unit_name
    where not exists(select 0 from bc_identity_actor a where a.code = c.code)
  returning id
)
-- 建立岗位与单位的关联
, relation_unit(master_id, follower_id) as (
  insert into bc_identity_actor_relation (type_, master_id, follower_id)
    select 0, m.id, f.id
    from cfg c
    inner join bc_identity_actor m on m.type_ = 1 and m.name = c.belong_unit_name
    inner join bc_identity_actor f on f.type_ = 3 and f.code = c.code
    where not exists(
      select 0 from bc_identity_actor_relation r where r.type_ = 0 and r.master_id = m.id and r.follower_id = f.id
    )
  returning master_id, follower_id
)
-- 建立岗位与车队管理模块所配置车队长的关联
, relation_user(group_id, user_id) as (
  insert into bc_identity_actor_relation (type_, master_id, follower_id)
    select 0, m.id, f.id
    from cfg c
    inner join bc_identity_actor m on m.type_ = 3 and m.code = c.code
    inner join bc_identity_actor f on f.type_ = 4
    inner join bs_motorcade t on t.principal_id = f.id and t.status_ = 0
    inner join bc_identity_actor tu on tu.id = t.unit_id and tu.name = c.belong_unit_name
    where not exists(
      select 0 from bc_identity_actor_relation r where r.type_ = 0 and r.master_id = m.id and r.follower_id = f.id
    )
  returning master_id, follower_id
)
select (select count(*) from create_group) as create_count
  , (select count(*) from relation_unit)    as relation_unit
  , (select count(*) from relation_user)    as relation_user;

-- 资源：营运系统/交通事故
with p(id) as (select id from bc_identity_resource where name = '营运系统')
, cfg(type, sn, name, iconclass) as (
  select 1, '031200-2', '事故(新版)', 'i0100'
)
insert into bc_identity_resource (status_, inner_, type_, order_, name, iconclass, belong, id)
  select 0, false, c.type, c.sn, c.name, c.iconclass, (select id from p), nextval('core_sequence')
  from cfg c
  where not exists (
    select 0 from bc_identity_resource s where s.name = c.name:: text and s.belong = (select id from p)
  );

-- 将原来的 "事故理赔" 更名为 "事故(旧版)"
update bc_identity_resource set name = '事故(旧版)' where name = '事故理赔';

-- 资源：营运系统/交通事故/*
with p(id) as (select id from bc_identity_resource where name = '事故(新版)')
, cfg(type, sn, name, url, iconclass) as (
         select 2, '072001', '事故报案':: text, '/static/accident/accident-draft/view.html', 'i0707'
  union select 2, '072002', '事故登记', '/static/accident/accident-register/view.html', 'i0001'
  union select 2, '072003', '事故报告', '/static/accident/accident-report/view.html', 'i0903'
  union select 1, '073001', '统计报表', null, 'i0100'
)
insert into bc_identity_resource (status_, inner_, type_, order_, name, url, iconclass, belong, id)
  select 0, false, c.type, c.sn, c.name, c.url, c.iconclass, (select id from p), nextval('core_sequence')
  from cfg c
  where not exists(select 0 from bc_identity_resource s where s.name = c.name and s.belong = (select id from p));

-- 资源：营运系统/交通事故/统计报表/*
with p(id) as (
  select id from bc_identity_resource
  where name = '统计报表' and belong = (select id from bc_identity_resource where name = '事故(新版)')
), cfg(type, sn, name, url, iconclass) as (
  select 2, '073101', '事故登记汇总统计':: text, 'static/accident/report/register-stat-summary/view.html', 'i0002'
  union select 2, '073201', '事故报告汇总统计':: text, 'static/accident/report/report-stat-summary/view.html', 'i0002'
)
insert into bc_identity_resource (status_, inner_, type_, order_, name, url, iconclass, belong, id)
  select 0, false, c.type, c.sn, c.name, c.url, c.iconclass, (select id from p), nextval('core_sequence')
  from cfg c
  where not exists(select 0 from bc_identity_resource s where s.name = c.name and s.belong = (select id from p));

-- 角色
with cfg(sn, name, code) as (
  -- 事故报案
         select '4011', '事故报案-查询':: text, 'ACCIDENT_DRAFT_READ':: text
  union select '4012', '事故报案-上报', 'ACCIDENT_DRAFT_SUBMIT'
  union select '4013', '事故报案-修改', 'ACCIDENT_DRAFT_MODIFY'

  -- 事故登记
  union select '4021', '事故登记-查询', 'ACCIDENT_REGISTER_READ'
  union select '4022', '事故登记-提交', 'ACCIDENT_REGISTER_SUBMIT'
  union select '4023', '事故登记-审核', 'ACCIDENT_REGISTER_CHECK'
  union select '4024', '事故登记-修改', 'ACCIDENT_REGISTER_MODIFY'

  -- 事故报告
  union select '4031', '事故报告-查询', 'ACCIDENT_REPORT_READ'
  union select '4032', '事故报告-提交', 'ACCIDENT_REPORT_SUBMIT'
  union select '4033', '事故报告-审核', 'ACCIDENT_REPORT_CHECK'
  union select '4034', '事故报告-修改', 'ACCIDENT_REPORT_MODIFY'
)
insert into bc_identity_role (status_, inner_, type_, order_, code, name, id)
  select 0, false, 0, c.sn, c.code, c.name, nextval('core_sequence')
  from cfg c
  where not exists(select 0 from bc_identity_role r where r.code = c.code);

-- 资源与角色的关联
with p(id) as (
  select id from bc_identity_resource where name = '事故(新版)'
  union select id from bc_identity_resource
  where name = '统计报表' and belong = (select id from bc_identity_resource where name = '事故(新版)')
), cfg(resource_name, role_codes) as (
  select '事故报案':: text, array_agg(code) from bc_identity_role where code like 'ACCIDENT_DRAFT_%'
  union select '事故登记', array_agg(code) from bc_identity_role where code like 'ACCIDENT_REGISTER_%'
  union select '事故报告', array_agg(code) from bc_identity_role where code like 'ACCIDENT_REPORT_%'
  union select '事故登记汇总统计', array_agg(code) from bc_identity_role where code like 'ACCIDENT_REGISTER_%'
  union select '事故报告汇总统计', array_agg(code) from bc_identity_role where code like 'ACCIDENT_REPORT_%'
)
insert into bc_identity_role_resource (rid, sid)
  select r.id, s.id
  from bc_identity_role r, bc_identity_resource s, (select unnest(role_codes) role_code, resource_name from cfg) c
  where r.code = c.role_code and s.name = c.resource_name and s.belong in (select id from p)
    and not exists(select 0 from bc_identity_role_resource rs where rs.rid = r.id and rs.sid = s.id);

-- 用户与角色的关联
with cfg(role_code, user_codes) as (
  -- 事故报案-查询
  select 'ACCIDENT_DRAFT_READ':: text, array ['baochengzongbu']
  -- 事故报案-上报
  union select 'ACCIDENT_DRAFT_SUBMIT', array ['fenGongSi1AQY', 'fenGongSi2AQY', 'fenGongSi1CDZ', 'fenGongSi2CDZ']
  -- 事故报案-修改
  union select 'ACCIDENT_DRAFT_MODIFY', array ['anquanguanlizu']

  -- 事故登记-查询
  union select 'ACCIDENT_REGISTER_READ', array ['baochengzongbu']
  -- 事故登记-提交
  union select 'ACCIDENT_REGISTER_SUBMIT',
    array ['fenGongSi1AQY', 'fenGongSi2AQY', 'fenGongSi1Manager', 'fenGongSi2Manager', 'fenGongSi1CDZ', 'fenGongSi2CDZ']
  -- 事故登记-审核
  union select 'ACCIDENT_REGISTER_CHECK', array ['anquanguanlizu']
  -- 事故登记-修改
  union select 'ACCIDENT_REGISTER_MODIFY', array ['anquanguanlizu']

  -- 事故报告-查询
  union select 'ACCIDENT_REPORT_READ', array ['baochengzongbu']
  -- 事故报告-提交
  union select 'ACCIDENT_REPORT_SUBMIT',
    array ['fenGongSi1AQY', 'fenGongSi2AQY', 'fenGongSi1Manager', 'fenGongSi2Manager', 'fenGongSi1CDZ', 'fenGongSi2CDZ']
  -- 事故报告-审核
  union select 'ACCIDENT_REPORT_CHECK', array ['anquanguanlizu']
  -- 事故报告-修改
  union select 'ACCIDENT_REPORT_MODIFY', array ['anquanguanlizu']
)
insert into bc_identity_role_actor (rid, aid)
  select r.id, a.id
  from bc_identity_role r, bc_identity_actor a, (select role_code, unnest(user_codes) user_code from cfg) c
  where r.code = c.role_code and a.code = c.user_code
  and not exists(select 0 from bc_identity_role_actor ra where ra.aid = a.id and ra.rid = r.id);

-- 微服务配置: 选项配置
-- select * from bc_option_item where pid in (select id from bc_option_group where key_ like 'micro-service%');
delete from bc_option_item where
  key_ in ('accident', 'file') and pid in (select id from bc_option_group where key_ like 'micro-service%');
with cfg(sn, pkey, key, address, name) as (
  -- 交通事故
         select '1101', 'micro-service',          'accident', 'http://127.0.0.1:9102/accident',     '交通事故'
  union select '1102', 'micro-service-internet', 'accident', 'http://www.gftaxi.cn:8081/accident', '交通事故'
  -- 文件服务器
  union select '1103', 'micro-service',          'file',     'http://127.0.0.1:9013/file',         '文件服务器'
  union select '1104', 'micro-service-internet', 'file',     'http://www.gftaxi.cn:8081/file',     '文件服务器'
)
insert into bc_option_item (id, status_, key_, value_, order_, pid)
  select nextval('core_sequence'), 0, c.key, c.address :: text || ' | ' || c.name :: text, c.sn,
    (select id from bc_option_group where key_ = c.pkey :: text)
  from cfg c where not exists(
    select 0 from bc_option_item where
      key_ = c.key:: text and pid = (select id from bc_option_group where key_ = c.pkey:: text)
  );

-- 测试环境的特殊权限配置：周文飞、开发组赋予全部事故权限
insert into bc_identity_role_actor (rid, aid)
  select r.id, a.id
  from bc_identity_role r, bc_identity_actor a
  where r.code like 'ACCIDENT_%' and r.code not like 'ACCIDENT_%_READ'
  and a.code in ('fei', 'DevelopmentGroup')
  and not exists(select 0 from bc_identity_role_actor ra where ra.aid = a.id and ra.rid = r.id);
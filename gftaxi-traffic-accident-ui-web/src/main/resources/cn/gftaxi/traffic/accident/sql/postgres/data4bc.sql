-- 清除插入的资源
with p(id) as ( -- 交通事故父目录
  select id from bc_identity_resource where name = '交通事故'
)
,resource(id) as (
  select id from p
  union select id from bc_identity_resource where name = '事故报案' and belong = (select id from p)
) -- select * from resource
, delete_role_resource(id) as ( -- 删除资源与角色的关联
  delete from bc_identity_role_resource where sid in (select id from resource)
  returning *
), delete_resource(id) as (     -- 删除资源
  delete from bc_identity_resource where id in (select id from resource)
  returning *
),
-- 清除插入的角色
role(id) as (select id from bc_identity_role where code like 'ACCIDENT_%')
--select * from role r inner join bc_identity_role o on o.id = r.id           -- 查出要删除的角色
, delete_role_actor as (        -- 删除用户与角色的关联
  delete from bc_identity_role_actor where rid in (select id from role)
  returning *
), delete_role as (             -- 删除角色
  delete from bc_identity_role where id in (select id from role)
  returning *
)
select (select count(*) from delete_role_resource) delete_role_resource
  , (select count(*) from delete_resource) delete_resource
  , (select count(*) from delete_role_actor) delete_role_actor
  , (select count(*) from delete_role) delete_role;

-- 资源：营运系统/交通事故
with p(id) as (select id from bc_identity_resource where name = '营运系统')
, cfg(type, sn, name, iconclass) as (
  select 1, '072000', '交通事故', 'i0100'
)
insert into bc_identity_resource (status_, inner_, type_, order_, name, iconclass, belong, id)
  select 0, false, c.type, c.sn, c.name, c.iconclass, (select id from p), nextval('core_sequence')
  from cfg c
  where not exists (select 0 from bc_identity_resource s where s.name = c.name::text and s.belong = (select id from p));

-- 资源：营运系统/交通事故/*
with p(id) as (select id from bc_identity_resource where name = '交通事故')
, cfg(type, sn, name, url, iconclass) as (
        select 2, '072001', '事故报案'::text, '/static/accident/accident-draft/view.html', 'i0707'
)
insert into bc_identity_resource (status_, inner_, type_, order_, name, url, iconclass, belong, id)
  select 0, false, c.type, c.sn, c.name, c.url, c.iconclass, (select id from p), nextval('core_sequence')
  from cfg c
  where not exists (select 0 from bc_identity_resource s where s.name = c.name and s.belong = (select id from p));

-- 角色
with cfg(sn, name, code) as (
  -- 提交报案信息角色
        select '4011', '提交报案信息角色'::text, 'ACCIDENT_DRAFT_MODIFY'::text
  -- 提交报案信息角色
  union select '4012', '提交报案信息角色'::text, 'ACCIDENT_DRAFT_SUBMIT'::text
  -- 查询报案信息角色
  union select '4012', '查询报案信息角色'::text, 'ACCIDENT_DRAFT_READ'::text
)
insert into bc_identity_role (status_, inner_, type_, order_, code, name, id)
  select 0, false, 0, c.sn, c.code, c.name, nextval('core_sequence')
  from cfg c
  where not exists (select 0 from bc_identity_role r where r.code = c.code);

-- 资源与角色的关联
with p(id) as (
  select id from bc_identity_resource
  where name = '交通事故'
)
, cfg(resource_name, role_codes) as (
        select '事故报案'::text, array['ACCIDENT_DRAFT_MODIFY', 'ACCIDENT_DRAFT_SUBMIT','ACCIDENT_DRAFT_READ']
)
insert into bc_identity_role_resource (rid, sid)
  select r.id, s.id
  from bc_identity_role r, bc_identity_resource s, (select unnest(role_codes) role_code, resource_name from cfg) c
  where r.code = c.role_code and s.name = c.resource_name and s.belong in (select id from p)
  and not exists (select 0 from bc_identity_role_resource rs where rs.rid = r.id and rs.sid = s.id);

-- 用户与角色的关联
with cfg(role_code, user_codes) as (
  -- 提交报案信息角色
        select 'ACCIDENT_DRAFT_MODIFY'::text,
    array[
      'foy', 'zws', 'zeng', 'jon', 'kelvin', 'owen', 'zsk',
      'lys', 'eagle', 'mars', 'wjb', 'cmy', 'hyp', 'hjx', 'zhong'
    ]
  -- 提交报案信息角色
  union select 'ACCIDENT_DRAFT_SUBMIT'::text,
    array[
      'foy', 'zws', 'zeng', 'jon', 'kelvin', 'owen', 'zsk',
      'lys', 'eagle', 'mars', 'wjb', 'cmy', 'hyp', 'hjx', 'zhong'
    ]
  -- 查询报案信息角色
  union select 'ACCIDENT_DRAFT_READ'::text, array['baochengzongbu']
)
insert into bc_identity_role_actor (rid, aid)
  select r.id, a.id
  from bc_identity_role r, bc_identity_actor a, (select role_code, unnest(user_codes) user_code from cfg) c
  where r.code = c.role_code and a.code = c.user_code
  and not exists (select 0 from bc_identity_role_actor ra where ra.aid = a.id and ra.rid = r.id);
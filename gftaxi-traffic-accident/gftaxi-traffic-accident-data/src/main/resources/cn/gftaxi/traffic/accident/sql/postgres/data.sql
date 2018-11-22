-- module version
insert into st_kv (key, value) values ('module-version-simter-category', '0.1-SNAPSHOT');
insert into st_kv (key, value) values ('module-version-gftaxi-traffic-accident', '${project.version}');

-- 初始化事故分类标准的数据
-- 交通事故分类标准（ID=2，附加到 ID=1 的 ROOT 节点下）
insert into st_category (pid, status, name, sn) values (1, 2, '交通事故分类标准', 'JTSG_CATEGORY');

-- 交通事故分类标准/*
insert into st_category (pid, status, name, sn)
  select 2, 2, name, cn_first_char(name) as sn
  from (select unnest(array['事故等级', '事故性质', '事故形态', '事故责任', '事故原因', '交通信号方式', '车辆状态', '碰撞类型',
    '行驶方向', '天气情况', '道路类型', '道路形态', '路面状况', '路表情况', '车辆拖车', '光线条件', '财产损失', '伤亡赔偿',
    '车辆维修状态', '保险资料递件', '启动其他程序', '理赔款项状态', '车辆分类', '当事人分类']) as name
  ) t;

-- 交通事故分类标准/事故等级/*
insert into st_category (pid, status, name, sn)
  select (select id from st_category where pid = 2 and name = '事故等级'), 2, name, (row_number() over())::text as sn
  from (select unnest(array['财产 1 级', '财产 2 级', '财产 3 级',
                              '人伤 1 级', '人伤 2 级', '人伤 3 级',
                              '死亡 1 级', '死亡 2 级', '死亡 3 级']) as name) t;

-- 交通事故分类标准/事故性质/*
insert into st_category (pid, status, name, sn)
  select (select id from st_category where pid = 2 and name = '事故性质'), 2, name, (row_number() over())::text as sn
  from (select unnest(array['财产损失事故','伤人事故', '死亡事故']) as name) t;

-- 交通事故分类标准/事故形态/*
insert into st_category (pid, status, name, sn)
  select (select id from st_category where pid = 2 and name = '事故形态'), 2, name, (row_number() over())::text as sn
  from (select unnest(array['车辆间事故', '车辆与人', '单车事故', '车辆与物事故']) as name) t;

-- 交通事故分类标准/事故责任/*
insert into st_category (pid, status, name, sn)
  select (select id from st_category where pid = 2 and name = '事故责任'), 2, name, (row_number() over())::text as sn
  from (select unnest(array['全部责任', '主要责任', '同等责任', '次要责任', '无责任', '单方全责']) as name) t;

-- 交通事故分类标准/事故原因/*
insert into st_category (pid, status, name, sn)
  select (select id from st_category where pid = 2 and name = '事故原因'), 2, name, (row_number() over())::text as sn
  from (select unnest(array['未按规定让行', '未保持安全距离', '变更车道', '超速行驶', '倒车', '超车', '未按规定停车'
    , '开车门措施不当', '其他']) as name) t;

-- 交通事故分类标准/交通信号方式/*
insert into st_category (pid, status, name, sn)
  select (select id from st_category where pid = 2 and name = '交通信号方式'), 2, name, (row_number() over())::text as sn
  from (select unnest(array['无信号', '民警指挥', '信号灯', '标志', '标线', '其他安全设施']) as name) t;

-- 交通事故分类标准/车辆状态/*
insert into st_category (pid, status, name, sn)
  select (select id from st_category where pid = 2 and name = '车辆状态'), 2, name, (row_number() over())::text as sn
  from (select unnest(array['运动', '静止']) as name) t;

-- 交通事故分类标准/碰撞类型/*
insert into st_category (pid, status, name, sn)
  select (select id from st_category where pid = 2 and name = '碰撞类型'), 2, name, (row_number() over())::text as sn
  from (select unnest(array['追尾碰撞', '环岛碰撞', '变道碰撞', '其他']) as name) t;

-- 交通事故分类标准/行驶方向/*
insert into st_category (pid, status, name, sn)
  select (select id from st_category where pid = 2 and name = '行驶方向'), 2, name
    , (case when (row_number() over()) < 10 then '0' || (row_number() over())::text else (row_number() over())::text end) as sn
  from (select unnest(array['由东向西', '由西向东', '由南向北', '由北向南', '由东向南', '由西向南', '由东向北', '由西向北'
    , '由南向东', '由南向西', '由北向东', '由北向西']) as name) t;

-- 交通事故分类标准/天气情况/*
insert into st_category (pid, status, name, sn)
  select (select id from st_category where pid = 2 and name = '天气情况'), 2, name, (row_number() over())::text as sn
  from (select unnest(array['晴', '阴', '雨', '雾', '大风', '其他']) as name) t;

-- 交通事故分类标准/道路类型/*
insert into st_category (pid, status, name, sn)
  select (select id from st_category where pid = 2 and name = '道路类型'), 2, name, (row_number() over())::text as sn
  from (select unnest(array['高速公路', '城市快速路', '一般城市道路', '单位小区自建路', '公共停车场', '公共广场', '高架'
    , '其他']) as name) t;

-- 交通事故分类标准/道路形态/*
insert into st_category (pid, status, name, sn)
  select (select id from st_category where pid = 2 and name = '道路形态'), 2, name, (row_number() over())::text as sn
  from (select unnest(array['直线', '弯道', '坡道', '路口', '环岛立交', '其他']) as name) t;

-- 交通事故分类标准/路面状况/*
insert into st_category (pid, status, name, sn)
  select (select id from st_category where pid = 2 and name = '路面状况'), 2, name, (row_number() over())::text as sn
  from (select unnest(array['路面完好', '施工', '凹凸', '塌陷', '路障', '其他']) as name) t;

-- 交通事故分类标准/路表情况/*
insert into st_category (pid, status, name, sn)
  select (select id from st_category where pid = 2 and name = '路表情况'), 2, name, (row_number() over())::text as sn
  from (select unnest(array['干燥', '潮湿', '积水', '漫水', '泥泞', '油污', '其他']) as name) t;

-- 交通事故分类标准/车辆拖车/*
insert into st_category (pid, status, name, sn)
  select (select id from st_category where pid = 2 and name = '车辆拖车'), 2, name, (row_number() over())::text as sn
  from (select unnest(array['内部拯救', '外部拯救', '无需拯救']) as name) t;

-- 交通事故分类标准/光线条件/*
insert into st_category (pid, status, name, sn)
  select (select id from st_category where pid = 2 and name = '光线条件'), 2, name, (row_number() over())::text as sn
  from (select unnest(array['良好', '一般', '较差']) as name) t;

-- 交通事故分类标准/财产损失/*
insert into st_category (pid, status, name, sn)
  select (select id from st_category where pid = 2 and name = '财产损失'), 2, name
    , (case when (row_number() over()) < 10 then '0' || (row_number() over())::text else (row_number() over())::text end) as sn
  from (select unnest(array['车辆维修费', '车辆施救费', '物品损失', '车辆重置费用', '经营性车辆停运损失', '评估费', '诉讼费'
    , '公告费', '财产保存费', '其他项目']) as name) t;

-- 交通事故分类标准/伤亡赔偿/*
insert into st_category (pid, status, name, sn)
  select (select id from st_category where pid = 2 and name = '伤亡赔偿'), 2, name
    , (case when (row_number() over()) < 10 then '0' || (row_number() over())::text else (row_number() over())::text end) as sn
  from (select unnest(array['医疗费', '误工费', '护理费', '交通费', '住宿费', '住院期间伙食补助费', '营养费', '鉴定费'
    , '残疾赔偿金', '残疾辅助器具费', '丧葬费', '被扶养人生活费', '死亡赔偿金', '精神损害抚慰金', '其他']) as name) t;

-- 交通事故分类标准/车辆维修状态/*
insert into st_category (pid, status, name, sn)
  select (select id from st_category where pid = 2 and name = '车辆维修状态'), 2, name, (row_number() over())::text as sn
  from (select unnest(array['接车核损', '派工维修', '竣工验收', '逾期未完工', '交付使用', '车辆返修', '二次维修'
    , '服务投诉']) as name) t;

-- 交通事故分类标准/保险资料递件/*
insert into st_category (pid, status, name, sn)
  select (select id from st_category where pid = 2 and name = '保险资料递件'), 2, name, (row_number() over())::text as sn
  from (select unnest(array['司机收集', '安全员签收', '安全组签收', '保险签收', '补充资料', '递件成功', '保险退件'
    , '赔付不足', '延期递件']) as name) t;

-- 交通事故分类标准/启动其他程序/*
insert into st_category (pid, status, name, sn)
  select (select id from st_category where pid = 2 and name = '启动其他程序'), 2, name
    , (case when (row_number() over()) < 10 then '0' || (row_number() over())::text else (row_number() over())::text end) as sn
  from (select unnest(array['无', '申请借款', '申请调解', '申请诉讼', '弃车现象', '申请公估', '申请直赔'
    , '交强险垫付抢救费用申请', '企业证照申领', '保单申领', '其他']) as name) t;

-- 交通事故分类标准/理赔款项状态/*
insert into st_category (pid, status, name, sn)
  select (select id from st_category where pid = 2 and name = '理赔款项状态'), 2, name, (row_number() over())::text as sn
  from (select unnest(array['未报损', '未到账', '已到账', '已领款']) as name) t;

-- 交通事故分类标准/车辆分类/*
insert into st_category (pid, status, name, sn)
  select (select id from st_category where pid = 2 and name = '车辆分类'), 2, name, (row_number() over())::text as sn
  from (select unnest(array['自车', '三者']) as name) t;

-- 交通事故分类标准/当事人分类/*
insert into st_category (pid, status, name, sn)
  select (select id from st_category where pid = 2 and name = '当事人分类'), 2, name, (row_number() over())::text as sn
  from (select unnest(array['自车', '三者']) as name) t;

-- 额外补充的交通事故分类标准
insert into st_category (pid, status, name, sn)
  select 2, 2, name, cn_first_char(name) as sn
  from (select unnest(array['载重情况', '事故处理部门', '事故处理方式', '保险公司',
    '车型分类', '伤亡分类', '维修分类', '受损情况', '跟进形式']) as name
  ) t;

-- 交通事故分类标准/载重情况/*
insert into st_category (pid, status, name, sn)
  select (select id from st_category where pid = 2 and name = '载重情况'), 2, name, (row_number() over())::text as sn
  from (select unnest(array['载客', '空车']) as name) t;

-- 交通事故分类标准/事故处理部门/*
insert into st_category (pid, status, name, sn)
  select (select id from st_category where pid = 2 and name = '事故处理部门'), 2, name, (row_number() over())::text as sn
  from (select unnest(array['快赔中心', '保险公司', '公司', '交警大队/白云一', '交警大队/白云二',
    '交警大队/天河', '交警大队/荔湾', '交警大队/越秀', '交警大队/海珠', '无']) as name) t;

-- 交通事故分类标准/事故处理方式/*
insert into st_category (pid, status, name, sn)
  select (select id from st_category where pid = 2 and name = '事故处理方式'), 2, name, (row_number() over())::text as sn
  from (select unnest(array['快处快赔', '交警认定', '保险查勘', '公司查勘', '协商私了']) as name) t;

-- 交通事故分类标准/保险公司/*
insert into st_category (pid, status, name, sn)
  select (select id from st_category where pid = 2 and name = '保险公司'), 2, name, (row_number() over())::text as sn
  from (select unnest(array['芳村人保', '天河人保']) as name) t;

-- 交通事故分类标准/车型分类/*
insert into st_category (pid, status, name, sn)
  select (select id from st_category where pid = 2 and name = '车型分类'), 2, name, (row_number() over())::text as sn
  from (select unnest(array['出租车', '小轿车', '客车', '货车', '摩托车', '电动踏板车', '挂车', '作业车', '残疾车', '其他']) as name) t;

-- 交通事故分类标准/伤亡分类/*
insert into st_category (pid, status, name, sn)
  select (select id from st_category where pid = 2 and name = '伤亡分类'), 2, name, (row_number() over())::text as sn
  from (select unnest(array['无', '伤', '亡']) as name) t;

-- 交通事故分类标准/维修分类/*
insert into st_category (pid, status, name, sn)
  select (select id from st_category where pid = 2 and name = '维修分类'), 2, name, (row_number() over())::text as sn
  from (select unnest(array['自厂修复', '外厂修复']) as name) t;

-- 交通事故分类标准/受损情况/*
insert into st_category (pid, status, name, sn)
  select (select id from st_category where pid = 2 and name = '受损情况'), 2, name, (row_number() over())::text as sn
  from (select unnest(array['前部', '后部', '左侧', '右侧', '全车多处']) as name) t;

-- 交通事故分类标准/跟进形式/*
insert into st_category (pid, status, name, sn)
  select (select id from st_category where pid = 2 and name = '跟进形式'), 2, name, (row_number() over())::text as sn
  from (select unnest(array['系统', '电话', '其他']) as name) t;

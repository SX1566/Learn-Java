/**
 * 交通事故通用 API 接口汇总
 *
 * @author RJ
 */
define(["bc", "context"], function (bc, context) {
  "use strict";
  let accidentDataServer = `${context.services.accident.address}`;  // 交通事故数据服务地址
  let accidentStaticServer = `${bc.root}/static/accident`;  // 静态文件服务地址

  /**
   * 定义超时用的 Promise。
   * 使用方式：Promise.race([yourPromise, timeout(30000)])
   */
  function timeout(ms) {
    return new Promise((resolve, reject) => {
      let id = setTimeout(() => {
        clearTimeout(id);
        reject(new Error('Timeout after ' + ms + 'ms.'));
      }, ms)
    })
  }

  /**
   * 定义 Promise 的 finally 便捷方法。
   * from http://es6.ruanyifeng.com/#docs/promise#Promise-prototype-finally
   */
  if (!Promise.prototype.finally) {
    Promise.prototype.finally = function (callback) {
      let P = this.constructor;
      return this.then(
        value => P.resolve(callback()).then(() => value),
        reason => P.resolve(callback()).then(() => {
          throw reason
        })
      );
    };
  }

  /** 获取 BC 系统的登录认证信息 */
  function getAuthorizationHeaders() {
    return {"Authorization": localStorage.authorization};
  }

  /** 跨域访问方法封装 */
  function cors(url, method, body, contentType) {
    let options = {headers: getAuthorizationHeaders()};
    if (method) options.method = method;
    if (body) options.body = body;
    if (contentType) options.headers["Content-Type"] = contentType;
    return fetch(url, options).then(function (res) {
      return res.ok ? (res.status === 204 ? null : (
        res.headers.get('Content-Type').startsWith('text/') ? res.text() : res.json() // 默认 json
      )) : res.text().then(function (msg) {
        throw new Error(msg)
      });
    });
  }

  /** 附加 URL 参数 */
  function appendUrlParams(url, params) {
    if (!params) return url;

    let kv = [];
    for (let key in params) kv.push(key + '=' + encodeURIComponent(params[key]));
    if (kv.length) url += (url.indexOf('?') !== -1 ? '&' : '?') + kv.join('&');
    return url;
  }

  let categories = cors(`${accidentDataServer}/category/group/`, "GET").then(r => {return r});
  const DRAFT_STATUSES = Object.freeze([
    {value: 1, id: 'ToSubmit', label: '待上报'},
    {value: 2, id: 'Drafting', label: '待登记'},
    {value: 4, id: 'Drafted', label: '已登记'}
  ]);
  const AUDIT_STATUSES = Object.freeze([
    {value: 1, id: 'ToSubmit', label: '待提交'},
    {value: 2, id: 'ToCheck', label: '待审核'},
    {value: 4, id: 'Rejected', label: '审核不通过'},
    {value: 8, id: 'Approved', label: '审核通过'}
  ]);
  const DRIVER_TYPES = Object.freeze([
    {id: 'Official', label: '正班'},
    {id: 'Shift', label: '替班'},
    {id: 'Outside', label: '非编'}
  ]);

  /** 事故中子列表的类型 */
  const subListTypes = ["AccidentCar", "AccidentPeople", "AccidentOther"]
  /** 当事车辆、当事人和其他物体的公共字段 */
  const subListBaseInfoComment = {sn: "序号", name: "名称", type: "分类", followType: "跟进形式", updatedTime: "更新时间"}
  /** 当事车辆、当事人和其他物体的字段 */
  const subListComment = {
    AccidentCar: Object.assign(subListBaseInfoComment, {
      model: "车型",
      towCount: "拖车次数",
      repairType: "维修分类",
      damageState: "受损情况",
      guessTowMoney: "预估拖车费",
      guessRepairMoney: "预估维修费",
      actualTowMoney: "实际拖车费",
      actualRepairMoney: "实际维修费"
    }),
    AccidentPeople: Object.assign(subListBaseInfoComment, {
      sex: "性别",
      phone: "联系电话",
      transportType: "交通方式",
      duty: "事故责任",
      damageState: "伤亡情况",
      guessTreatmentMoney: "预估医疗费",
      guessCompensateMoney: "预估赔偿损失",
      actualTreatmentMoney: "实际医疗费",
      actualCompensateMoney: "实际赔偿损失",
    }),
    AccidentOther: Object.assign(subListBaseInfoComment, {
      belong: "归属",
      linkmanName: "联系人",
      linkmanPhone: "联系电话",
      damageState: "受损情况",
      guessMoney: "损失预估",
      actualMoney: "实际损失"
    })
  }

  //==== 一些常数定义 ====
  const api = {
    /** 交通事故数据服务地址 */
    dataServer: accidentDataServer,
    /** 前端静态文件服务地址 */
    staticServer: accidentStaticServer,
    /** 交通事故分类信息 */
    categories: categories,
    /** 事故报案状态 */
    DRAFT_STATUSES: DRAFT_STATUSES,
    DRAFT_STATUS_MAP: Object.freeze(DRAFT_STATUSES.reduce((m, s) => { m[s.id] = s.label; return m }, {})),
    /** 案件审查状态（事故登记状态、事故报告状态） */
    AUDIT_STATUSES: AUDIT_STATUSES,
    AUDIT_STATUS_MAP: Object.freeze(AUDIT_STATUSES.reduce((m, s) => { m[s.id] = s.label; return m }, {})),
    /** 司机驾驶状态 */
    DRIVER_TYPES: DRIVER_TYPES,
    DRIVER_TYPE_MAP: Object.freeze(DRIVER_TYPES.reduce((m, s) => { m[s.id] = s.label; return m }, {})),

    /**
     * 打开模块的表单窗口。
     * @param module 模块标识，如事故报案为 'accident-draft'
     * @param option 窗口配置参数
     * @option mid 窗口ID
     * @option name 任务栏标题
     * @option title 【可选】窗口标题，默认等于 name 参数的值
     * @option data 【可选】传输的参数
     * @param type 【可选】URL 的后部，默认为 'form.html'
     * @option afterClose 【可选】窗口关闭后的回调函数
     */
    open: function (module, option, type) {
      let url = `text!${accidentStaticServer}/${module}/`;
      url += type ? type : 'form.html';
      require([url], function success(html) {
        bc.page.newWin(Object.assign({html: html}, option));
      });
    },
    /** 获取指定路径资源 */
    get: function (url, data) {
      return cors(url, "GET", data);
    },
    /**
     * 获取模块列表信息。
     *
     * @param module 模块标识，如出租方为 'accident-draft'
     * @param params [可选] 附加的查询参数，使用 json 格式，如 {status: 'Enabled'}，注意参数值不需要 uri 编码
     * @param throwError [可选] 是否冒泡异常，默认 false：true-异常由使用者通过 catch 自行处理，false-直接弹出框显示异常信息
     * @return {Promise}
     */
    find: function (module, params, throwError) {
      let url = `${accidentDataServer}/${module}`;

      // 附加 URL 参数
      url = appendUrlParams(url, params);

      // 获取数据
      return fetch(url, {
        headers: getAuthorizationHeaders(),
        method: "GET"
      }).then(res => {
        if (res.ok) {
          // 解析并返回 json 信息
          return res.status === 204 ? null : res.json();
        } else {
          // 解析异常信息并进行相应处理
          return res.text().then(msg => {
            if (throwError) throw new Error(msg); // 冒泡异常
            else bc.msg.info(error.message);      // 直接显示异常信息
          })
        }
      });
    },
    /**
     * 获取模块指定主键的信息
     * @param module 模块标识，如违法信息为 'accident-draft'
     * @param id 主键
     * @param [可选] throwError 是否冒泡异常，默认 false：true-异常由使用者通过 catch 自行处理，false-直接弹出框显示异常信息
     * @return {Promise}
     */
    getByModule: function (module, id, throwError) {
      let p = cors(`${accidentDataServer}/${module}/${id}`, "GET");

      // 冒泡异常
      if (!throwError) p.catch(error => bc.msg.info(error.message));

      return p;
    },
    /**
     * 保存信息
     * @param module 模块标识，如事故报案为 'accident-draft'
     * @param id [可选] 主键，如果指定主键代表更新数据，否则代表创建数据
     * @param data [可选] 要保存的数据，json 格式
     * @param throwError [可选] 是否冒泡异常，默认 false：true-异常由使用者通过 catch 自行处理，false-直接弹出框显示异常信息
     * @return {Promise}
     */
    save: function (module, id, data, throwError) {
      let url = `${accidentDataServer}/${module}`;
      let method;
      if (id) {
        url += `/${id}`;
        method = 'PATCH';
      } else method = 'POST';

      let p = cors(url, method, data ? JSON.stringify(data) : null, "application/json;charset=UTF-8");

      // 冒泡异常
      if (!throwError) p.catch(error => bc.msg.info(error.message));

      return p;
    },
    /**
     * 对指定资源的信息执行部分信息更新（如更新某个属性）或执行特定的操作（如审核、作废）
     * @param module 模块标识，如事故登记为 'accident-register'
     * @param id 主键，如果没有提供 data 则必须提供 id
     * @param bodyData [可选] 要提交的请求体数据，json 格式
     * @param urlParams [可选] 附加的查询参数，json 格式，如 {status: 'Enabled'}，注意参数值不需要 uri 编码
     * @param throwError [可选] 是否冒泡异常，默认 false：true-异常由使用者通过 catch 自行处理，false-直接弹出框显示异常信息
     * @return {Promise}
     */
    patch: function (module, id, bodyData, throwError, urlParams) {
      let url = `${accidentDataServer}/${module}`;
      if (id) url += `/${id}`;

      // 附加 URL 参数
      url = appendUrlParams(url, urlParams);

      // 暂时使用 PUT，等 java 后端支持 PATCH 方法时可以改为使用 PATCH
      let p = cors(url, 'PATCH', bodyData ? JSON.stringify(bodyData) : null, "application/json");

      // 冒泡异常
      if (!throwError) p.catch(error => bc.msg.info(error.message));

      return p;
    },
    /**
     * 获取分类列表。
     * @param sn 分类编码，如 "SGXZ"（事故性质）
     * @param includeDisabled 是否包含 Disabled 状态的二级分类，不指定默认仅返回 Enabled 状态
     * @return {Promise}
     */
    findCategory: function (sn, includeDisabled) {
      let url = `${accidentDataServer}/category/${sn}/children`;
      if (includeDisabled !== null) url = `${url}?include-disabled=${includeDisabled}`;
      return cors(url, "GET");
    },
    /**
     * 计算两个时间之间相差的小时数
     * @param startDate 开始时间
     * @param endDate 结束事件
     */
    calcInervalHour: function (startDate, endDate) {
      var ms = endDate.getTime() - startDate.getTime();
      if (ms < 0) return 0;
      return Math.round(ms / 1000 / 60 / 60);
    },
    /**
     * 计算两个时间之间相差的天数和小时
     * @param startDate 开始时间
     * @param endDate 结束事件
     * @return 相差的天数和小时，格式如: 1d8h (表示相差1天8小时)
     */
    calcInervalDayAndHour: function (startDate, endDate) {
      let hours = this.calcInervalHour(startDate, endDate);
      if (hours > 24) return `${Math.floor(hours / 24)}d${hours % 24 > 0 ? `${hours % 24}h` : ""}`;
      else if (hours < 24) return `${hours % 24}h`;
      else return "1d";
    },
    /**
     * 计算两个时间之间相差的年份
     * @param startDate 开始时间
     * @param endDate 结束事件
     * @return string 相差的年份,精确到小数点后一位
     */
    calcInervalYear: function (startDate, endDate) {
      return (this.calcInervalHour(startDate, endDate) / 24 / 365).toFixed(1);
    },
    /**
     * 筛选变更数据
     *
     * ### 数据筛选时增删改处理的说明如下：
     * - 如果原数据中没有相应的 Key 则作新增处理。
     * - 如果当前数据中没有相应的 Key 则作删除处理。
     * - 如果原数据和当前数据都有相应的 Key 并且判断值是否相同，不同则作修改处理，否则忽略。
     * - 如果原数据和当前数据都有相应的 Key 而且为嵌套对象则递归筛选处理。
     * - 如果原数据和当前数据都有相应的 Key 而且为 Array 对象：
     * -- 如果数组中原数据和当前数据相同，则只记录对象 Id ，表示无修改，如“{id: xxx}”。
     * -- 如果数组中原数据有,但当前数据中无关联的对象，则不记录，作删除处理。
     * -- 如果当前数据对象无 Id 属性，则作新增处理。
     * -- 如果数组中原数据和当前数据不一致，则记录当前数据的值。
     *
     * @param origin 原始数据
     * @param current 当前数据
     * @return 与原始数据不相同的数据的集合
     */
    filterChangedData: function (origin, current) {
      let keys = Object.keys(current);
      Object.keys(origin).forEach(k => {
        if (keys.indexOf(k) === -1) keys.push(k);
      });
      // 筛选修改过的数据
      let result = {};
      keys.forEach(key => {
        if (key.toLowerCase() === "id") {
          result.id = current.id;
          return;
        }
        // key 不在原数据中时而且当前数据不为 null 或空字符串则新增，否则忽略
        if (!(key in origin)) {
          if (current[key]) result[key] = current[key];
          else return;
        }
        // key 不在当前数据时则删除
        else if (!(key in current)) result[key] = null;
        else {  // key 都存在与原数据和当前数据则判断是否被修改过
          // 如果是 js 基本类型而且原始值和当前值不相同
          if (["number", "string", "boolean"].indexOf(typeof current[key]) > -1 && origin[key] != current[key])
            result[key] = current[key];
          else if (current[key] instanceof Array) { // 如果是 Array 类型
            // 原数据和当前数据都为空时则忽略
            if (origin[key].length === 0 && current[key].length === 0) return;
            // 无原数据但有当前数据时记录当前数据
            else if (origin[key].length === 0 && current[key].length > 0) result[key] = current[key];
            // 当前数据无但原数据有时当作清空处理
            else if (origin[key].length > 0 && current[key].length === 0) result[key] = [];
            else {
              let array = [];
              // 无 id 则直接添加
              array = array.concat(current[key].filter(obj => !obj.id));
              // 记录原数据和当前数据的 Id 值
              let ids = [];
              origin[key].concat(current[key]).filter(obj => obj.id).map(obj => obj.id).forEach(id => {
                if (ids.length === 0) ids.push(id);
                if (ids.indexOf(id) === -1) ids.push(id);
              });
              ids.forEach(id => {
                let currentObj = current[key].find(i => i.id === id);
                // 通过 Id 字段配对数组中原数据和当前数据，然后递归筛选修改过的数据
                if (currentObj) array.push(this.filterChangedData(origin[key].find(i => i.id === id), currentObj))
              });
              // 如果原始数据数组和当前数据数组的数据条数都相同而且数组中数据中只有 Id 值(表示数组中数据都无修改)则忽略
              if (origin[key].length === current[key].length && !array.some(a => Object.keys(a).some(k => k !== "id")))
                return;
              result[key] = array;
            }
          }
          // 如果是 Object 类型
          else if (typeof current[key] === "object") result[key] = this.filterChangedData(origin[key], current[key]);
        }
      });
      return result;
    },
    /**
     * 判断数据是否修改过
     * 如果数据主体或主体中的数组中对象只有 Id 属性则表示无修改，否则表示有修改
     *
     * @param data 需要验证的数据
     * @param origin 原始数据
     * @return boolean 有修改过则返回 true 否则返回 false
     */
    validateChange: function (data, origin) {
      return Object.keys(data).some(key => {
        if (data[key] instanceof Array && data[key].length > 0) {
          return data[key].length !== origin[key].length || data[key].some(d => Object.keys(d).some(k => k !== "id"));
        } else return key.toLowerCase() !== "id"
      })
    },
    pavedSubList: function (accidentOperation) {
      if (!!accidentOperation.fields) {
        accidentOperation.fields = accidentOperation.fields.filter(field => subListTypes.indexOf(field.type) !== -1)
          .map(field => {
            const newValue = JSON.parse(field.newValue)
            const oldValue = JSON.parse(field.oldValue)
            return Object.keys(newValue).map(key => {
              return {
                id: key,
                name: subListComment[field.type][key],
                newValue: newValue[key],
                oldValue: oldValue[key],
                group: field.name
              }
            })
          })
          .reduce((prev, cur) => prev.concat(cur),
            accidentOperation.fields.filter(field => subListTypes.indexOf(field.type) === -1))
      }
    }
  };
  return api;
});
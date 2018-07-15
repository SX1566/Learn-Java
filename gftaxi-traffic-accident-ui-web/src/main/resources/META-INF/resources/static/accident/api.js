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

  //==== 一些常数定义 ====
  const api = {
    /** 数据服务地址 */
    dataServer: accidentDataServer,
    /** 前端静态文件服务地址 */
    staticServer: accidentStaticServer,

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
    get: function (url, method, data) {
      return cors(url,method,data);
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
        method = 'PUT';
      } else method = 'POST';

      let p = cors(url, method, data ? JSON.stringify(data) : null, "application/json;charset=UTF-8");

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
      return  cors(`${accidentDataServer}/category/${sn}/children`, "GET", {"include-disabled": includeDisabled});
    }
  };
  return api;
});
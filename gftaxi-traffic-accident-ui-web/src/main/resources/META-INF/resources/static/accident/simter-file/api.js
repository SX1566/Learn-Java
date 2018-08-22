/**
 * 文件服务器 API 接口汇总
 *
 * @author RJ
 */
define(["jquery", "bc", "context"], function ($, bc, context) {
  "use strict";
  let fileDataServer = `${context.services.file.address}`;  // 文件服务器数据服务地址

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


  //==== 一些常数定义 ====
  const api = {
    /** 文件数据服务地址 */
    fileDataServer: fileDataServer,


    /**
     * 在线查看指定 Id 附件
     * @param id 附件Id
     */
    inlineById: function (id) {
      window.open(`${fileDataServer}/inline/${id}`);
    },
    /**
     * 在线查看指定模块分组附件
     * @param module 附件所属模块，如：ARxxx，AR 为 AccidentRegister 的缩写，xxx为事故登记的 Id
     * @param subgroup 附件所属模块的分组
     */
    inlineByModule: function (module, subgroup) {
      window.open(`${fileDataServer}/inline/parent/${module}/${subgroup}`);
    },
    /**
     * 使用流上传文件
     * @param files 文件数据
     * @param option 上传配置
     * @option url 上传请求的地址
     * @option puid 业务模块的唯一标识
     * @option subgroup 业务模块的子分类
     * @option onOk 上传成功回调函数
     * @option onError 上传失败回调函数
     * @option onProgress 上传进度回调函数
     * @return {Promise}
     */
    uploadByStream: function (files, option) {
      let xhrs = {};

      let url = appendUrlParams(option.url || fileDataServer, {puid: option.puid || 0, subgroup: option.subgroup || 0});

      // todo:检测文件数量的限制
      // todo:检测文件大小的限制
      // todo:检测文件类型的限制
      // todo:显示所有要上传的文件

      //开始上传
      let i = 0;
      let batchNo = "k" + new Date().getTime() + "-";//批号
      setTimeout(function () {
        uploadNext();
      }, 500);//延时小许时间再上传，避免太快看不到效果

      // 逐一上传文件
      function uploadNext() {
        if (i >= files.length) {//全部上传完毕
          option.onOk();
          return;
        }

        let key = `${batchNo}${i}`;
        //继续上传下一个附件
        uploadOneFile(key, files[i], url, uploadNext);
      }

      //上传一个文件
      function uploadOneFile(key, f, url, callback) {
        let xhr = new XMLHttpRequest();
        xhrs[key] = xhr;
        if ($.browser.safari) {//Chrome12、Safari5
          xhr.upload.onprogress = option.onProgress;
        } else if ($.browser.mozilla) {//Firefox4
          xhr.onuploadprogress = option.onProgress;
        }

        //上传完毕的处理
        xhr.onreadystatechange = function () {
          if (xhr.readyState === 4) {
            bc.attach.html5.xhrs[key] = null;
            //累计上传的文件数
            i++;
            //调用回调函数
            if (typeof callback == "function") callback();
          }
        };

        //上传失败的处理
        xhr.onerror = option.onError;

        // 发送请求
        xhr.open("POST", url);
        xhr.setRequestHeader('Authorization', localStorage.authorization);
        xhr.setRequestHeader('Content-Type', 'application/octet-stream');
        xhr.setRequestHeader('Content-Disposition', `attachment; name=\"filedata\"; filename=\"${encodeURIComponent(f.fileName || f.name)}\"`);
        xhr.send(f);
      }
    }
  };
  return api;
});
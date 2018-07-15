define(["bc", "bs", "bs/carMan.js", "vue", "context", 'static/accident/api'], function (bc, bs, carMan, Vue, context, accident) {
  "use strict";
  let resourceKey = "accident-draft";
  let isManager = context.isAny("ACCIDENT_DRAFT_MODIFY", "ACCIDENT_DRAFT_SUBMIT");

  function Page($page) {
    this["vm"] = new Vue({
      el: $page[0],
      data: {
        ui: {
          statuses: {"Todo": "待登记", "Done": "已登记"},
          readOnly: !isManager,
          hitForms: [{sn: "", name: ""}],
          hitTypes: [{sn: "", name: ""}],
          driverNames: [],
          happenTime:""
        },
        e: {status: "Todo", source: "BC"}
      },
      ready: function () {
        let code = $page.data("data");
        if (code) {
          Vue.set(this.e, "code", code);
          accident.getByModule(resourceKey, code).then(json => {
            Object.keys(json).forEach(key => Vue.set(this.e, key, json[key]));
          });
        } else {
          Vue.set(this.e, "authorName", context.userName);
          Vue.set(this.e, "authorId", context.userCode);
        }
        if (isManager) {
          this.loadHitForms();
          this.loadHitTypes();
          this.showHideButtons();
        }
      },
      watch:{
        'ui.happenTime': function (value) {
          Vue.set(this.e,"happenTime",value.replace("T", " "));
        },
        'e.happenTime':function (value) {
          Vue.set(this.ui,"happenTime",value.replace(" ", "T"));
        }
      },
      computed: {
        statusLabel: function () {
          return this.ui.statuses[this.e.status] || this.e.status;
        },
        overdueLabel: function () {
          if (this.e.code) return this.e.overdue ? "是" : "否";
          else {
            let happenTime = this.e.happenTime ? new Date(this.e.happenTime) : null;
            if (happenTime) return this.getInervalHour(happenTime, new Date()) > 12 ? "是" : "否";
            return "";
          }
        }
      },
      methods: {
        /** 获取事故形态列表 */
        loadHitForms: function () {
          accident.findCategory("SGXT", false)
            .then(json => {Vue.set(this.ui, "hitForms", this.ui.hitForms.concat(json))});
        },
        /** 获取碰撞类型列表 */
        loadHitTypes: function () {
          accident.findCategory("PZLX", false)
            .then(json => {Vue.set(this.ui, "hitTypes", this.ui.hitTypes.concat(json))});
        },
        /** 保存 */
        save: function () {
          if (!bc.validator.validate($page)) return;
          let isNew = !this.e.code;
          let saveKeys = ["carPlate","driverName","happenTime","location","hitForm","hitType","describe"];
          if (isNew) saveKeys = saveKeys.concat(["source", "authorName", "authorId"]);
          let data = {};
          Object.keys(this.e).forEach(key => {
            if(saveKeys.includes(key)) data[key] = this.e[key];
          });
          // 如果是保存请求则移除"报案来源","接案人姓名"和"接案人账号"
          accident.save(resourceKey,this.e.code,data).then(result => {
            if (isNew) {
              Vue.set(this.e, "code", result.code);
              Vue.set(this.e, "reportTime", result.reportTime);
            }
            $page.data("status", "saved");
            bc.msg.slide("保存成功！");
          });
        },
        /** 选择车辆 */
        selectCar:function () {
          bs.selectCar({
            vm: this,
            status: "-1,0", multiple: false,
            onOk: function (car) {
              // 设置车辆和车队
              Vue.set(this.vm.e, "carPlate", car.plate);
              Vue.set(this.vm.e, "motorcade", car.motorcadeName);
              // 加载车辆的营运司机
              carMan.findByCar(car.id, true).then(drivers =>
                Vue.set(this.vm.ui, "driverNames", drivers.map(d => d.name))
              )
            }
          })
        },
        /** 计算时间相差的小时 */
        getInervalHour: function (startDate, endDate) {
          var ms = endDate.getTime() - startDate.getTime();
          if (ms < 0) return 0;
          return Math.floor(ms / 1000 / 60 / 60);
        },
        /** 显示隐藏按钮 */
        showHideButtons: function () {
          if (!isManager) return;
          if (this.e.code && context.is("ACCIDENT_DRAFT_MODIFY"))// 有事故编号且有修改权限则显示保存按钮
            $page.parent().find("button#save").show();
          else if (!this.e.code && context.is("ACCIDENT_DRAFT_SUBMIT"))// 无事故编号且有提交权限则显示提交按钮
            $page.parent().find("button#submit").show();
        }
      }
    });
  }

  // 自定义窗口的 data-option 配置
  Page.option = {width: 500, minWidth: 500, minHeight: 290};
  Page.option.buttons = [
    {id: "save", text: "保存", click: "save", style: "display:none"},
    {id: "submit", text: "提交", click: "save", style: "display:none"}
  ];
  return Page;
});
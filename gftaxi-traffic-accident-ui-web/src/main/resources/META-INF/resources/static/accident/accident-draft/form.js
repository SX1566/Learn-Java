define(["bc", "bs", "bs/carMan.js", "vue", "context", 'static/accident/api'], function (bc, bs, carMan, Vue, context, accident) {
  "use strict";
  let resourceKey = "accident-draft";
  let isSubmitter = context.is("ACCIDENT_DRAFT_SUBMIT");
  let isEditor = context.is("ACCIDENT_DRAFT_MODIFY");
  let isManager = isEditor || isSubmitter;

  function Page($page) {
    this["vm"] = new Vue({
      el: $page[0],
      data: {
        ui: {
          statuses: {"Todo": "待登记", "Done": "已登记"},
          hitForms: [""],
          hitTypes: [""],
          driverNames: [],
          happenTime: ""
        },
        e: {status: "Todo", source: "BC"}
      },
      ready: function () {
        let id = $page.data("data");
        if (id) {
          Vue.set(this.e, "code", id);
          accident.getByModule(resourceKey, id).then(json => {
            Object.keys(json).forEach(key => Vue.set(this.e, key, json[key]));
            this.ui.hitForms.push(this.e.hitForm);
            this.ui.hitTypes.push(this.e.hitType);

            if (isManager) {
              this.loadHitForms();
              this.loadHitTypes();
              this.loadCarMans(this.e["carPlate"]);
              this.showHideButtons();
            }
          });
          // 初始化"简要描述"栏自动行高
          setTimeout(() => {
            $page.parent().find(".autoHeight").keyup()
          }, 200);
        } else {
          Vue.set(this.e, "authorName", context.userName);
          Vue.set(this.e, "authorId", context.userCode);
          if (isManager) {
            this.loadHitForms();
            this.loadHitTypes();
            this.showHideButtons();
          }
        }
      },
      watch: {
        'ui.happenTime': function (value) {
          Vue.set(this.e, "happenTime", value.replace("T", " "));
        },
        'e.happenTime': function (value) {
          Vue.set(this.ui, "happenTime", value.replace(" ", "T"));
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
            if (happenTime) return accident.calcInervalHour(happenTime, new Date()) > 12 ? "是" : "否";
            return "";
          }
        },
        readOnly: function () {
          if (!isManager) return true;
          if (this.e.code) return !isEditor; // 待登记状态时没有修改权限则为只读
          else return !isSubmitter; // 未上报状态时没有上报权限则为只读
        }
      },
      methods: {
        /** 获取事故形态列表 */
        loadHitForms: function () {
          accident.findCategory("SGXT", false)
            .then(json => {
              Vue.set(this.ui, "hitForms", this.ui.hitForms.concat(
                json.filter(h => !this.ui.hitForms.includes(h.name)).map(h => h.name)
              ))
            });
        },
        /** 获取碰撞类型列表 */
        loadHitTypes: function () {
          accident.findCategory("PZLX", false)
            .then(json => {
              Vue.set(this.ui, "hitTypes", this.ui.hitTypes.concat(
                json.filter(h => !this.ui.hitTypes.includes(h.name)).map(h => h.name)
              ))
            });
        },
        /**
         * 加载车辆的营运司机
         *
         * @params carKey 车辆关键信息，如车辆ID、车牌[粤A12345或12345]
         * */
        loadCarMans: function (carKey) {
          carKey = carKey.replace(".", ""); // 将车号的"粤A.12345" 改为 "粤A12345"
          carMan.findByCar(carKey, true).then(drivers =>
            Vue.set(this.ui, "driverNames", drivers.map(d => d.name))
          )
        },
        /** 保存 */
        save: function () {
          if (!bc.validator.validate($page)) return;
          let isNew = !this.e.id;
          let saveKeys = ["carPlate", "driverName", "happenTime", "location", "hitForm", "hitType", "describe"];
          if (isNew) saveKeys = saveKeys.concat(["source", "authorName", "authorId"]);
          let data = {};
          Object.keys(this.e).forEach(key => {
            if (saveKeys.includes(key)) {
              data[key] = this.e[key];
            }
          });
          // 上报操作
          if (isNew) {
            bc.msg.confirm("确定上报案件吗？", () =>
              accident.save(resourceKey, this.e.id, data).then(result => {
                Vue.set(this.e, "id", result.id);
                Vue.set(this.e, "code", result.code);
                Vue.set(this.e, "reportTime", result.reportTime);
                bc.msg.slide("上报成功！");
                $page.data("status", "saved");
                $page.dialog("close");      // 上报案件后关闭表单
              }));
          } else {// 保存操作
            accident.save(resourceKey, this.e.id, data).then(() => {
              $page.data("status", "saved");
              bc.msg.slide("保存成功！");
            });
          }
        },
        /** 选择车辆 */
        selectCar: function () {
          bs.selectCar({
            vm: this,
            status: "-1,0", multiple: false,
            onOk: function (car) {
              // 设置车辆和车队
              Vue.set(this.vm.e, "carPlate", car.plate);
              Vue.set(this.vm.e, "motorcade", car.motorcadeName);
              // 加载车辆的营运司机
              this.vm.loadCarMans(car.id);
            }
          })
        },
        /** 显示隐藏按钮 */
        showHideButtons: function () {
          if (!isManager) return;
          if (this.e.code && context.is("ACCIDENT_DRAFT_MODIFY"))// 有事故编号且有修改权限则显示保存按钮
            $page.parent().find("button#save").show();
          else if (!this.e.code && context.is("ACCIDENT_DRAFT_SUBMIT"))// 无事故编号且有上报权限则显示上报按钮
            $page.parent().find("button#submit").show();
        }
      }
    });
  }

  // 自定义窗口的 data-option 配置
  Page.option = {width: 500, minWidth: 500};
  Page.option.buttons = [
    {id: "save", text: "保存", click: "save", style: "display:none"},
    {id: "submit", text: "上报", click: "save", style: "display:none"}
  ];
  return Page;
});
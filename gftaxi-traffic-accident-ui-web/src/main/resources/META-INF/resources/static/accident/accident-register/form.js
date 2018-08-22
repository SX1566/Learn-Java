define(["bc", "bs", "bs/carMan.js", "vue", "context", 'static/accident/api','static/accident/simter-file/api'],
  function (bc, bs, carMan, Vue, context, accident, file) {
  "use strict";
  let resourceKey = "accident-register";
  let isRecorder = context.is("ACCIDENT_REGISTER_SUBMIT");
  let isEditor = context.is("ACCIDENT_REGISTER_MODIFY");
  let isChecker = context.is("ACCIDENT_REGISTER_CHECK");
  let isManager = isRecorder || isEditor || isChecker;

  function Page($page) {
    this["vm"] = new Vue({
      el: $page[0],
      data: {
        ui: {
          fixedWidth: '75.2em',
          isChecker: isChecker,
          happenTime: "",
          sexList: [{id: "Male", label: "男"}, {id: "Female", label: "女"}],
          driverStates: [{id: "Official", label: "正班"}, {id: "Shift", label: "替班"}, {id: "Outside", label: "非编"}]
        },
        categories: {},
        e: {status: "Draft"},
        accidentAttachments: []
      },
      ready: function () {
        let id = $page.data("data");
        Vue.set(this.e, "id", id);
        accident.getByModule(resourceKey, id).then(json => {
          Vue.set(this, "e", json);
          this.showHideButtons();
          // 初始化"简要描述"栏自动行高
          setTimeout(() => {
            $page.parent().find(".autoHeight").keyup()
          }, 200);
        });
        // 加载分类标准信息
        accident.categories.then(r => Vue.set(this, "categories", r));
        // 加载事故登记附件
        this.loadAccidentAttachments();
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
        isReadOnly: function () {
          if (!isRecorder && !isEditor) return true; // 无登记和修改权限
          else if (this.e.status === "Draft" && !isRecorder) return true; // 待登记状态但不是登记角色
          // 已登记但不是修改角色
          else if (["ToCheck", "Rejected", "Approved"].includes(this.e.status) && !isEditor) return true;
          else return false;
        },
        driverTypeLabel: function () {
          return this.ui.driverStates.find(d => d.id === this.e.driverType).label;
        },
        isShowRoadCategories: function () {
          return !["财产 1 级", "财产 2 级"].includes(this.e.level)
        },
        isShowItemDeleteButton: function () {
          return {
            cars: this.e.cars.some(i => i.selected),
            peoples: this.e.peoples.some(i => i.selected),
            others: this.e.others.some(i => i.selected),
          }
        }
      },
      components: {
        "accident-cars-column-definitions": {template: $page.find("script[name=accident-cars-column-definitions]").html()},
        "accident-people-column-definitions": {template: $page.find("script[name=accident-people-column-definitions]").html()},
        "accident-other-column-definitions": {template: $page.find("script[name=accident-other-column-definitions]").html()}
      },
      methods: {
      /** 加载事故附件信息 */
      loadAccidentAttachments: function () {
        accident.get(`${accident.fileDataServer}/parent/AR${this.e.id}/3`).then(attachments => {
          this.accidentAttachments = attachments
        })
      },
        /**
         * 保存表单
         * @param option 回调函数
         */
        save: function (option) {
          if (!bc.validator.validate($page)) return;
          let isNew = !this.e.id;
          let data = {};
          let ignoreKeys = ["status", "historyAccidentCount", "historyTrafficOffenceCount",
            "historyServiceOffenceCount", "historyComplainCount", "result", "comment", "attachmentId", "attachmentName"];
          Object.keys(this.e).forEach(key => {
            if (ignoreKeys.includes(key)) return;
            let value = this.e[key];
            if (!value || (Array.isArray(value) && value.length <= 0)) return;
            data[key] = value;
          });

          accident.save(resourceKey, this.e.id || null, data).then(result => {
            if (isNew) Vue.set(this.e, "id", result.id);
            if (typeof(option.afterSuccess) === "function") option.afterSuccess();
            else {
              bc.msg.slide("保存成功！");
              $page.data("status", "saved");
            }
          });
        },
        /** 提交 */
        submit: function () {
          let id = this.e.id;
          // 保存表单数据
          this.save({
            afterSuccess: function () {
              accident.patch(`${resourceKey}/to-check`, id).then(() => {
                bc.msg.slide("提交成功！");
                $page.data("status", "saved");
                $page.dialog("close");  // 提交完成后关闭表单
              })
            }
          })
        },
        /** 审核 */
        check: function () {
          let id = this.e.id;
          let data = {result: this.e.result, comment: this.e.comment};
          if (this.e.attachmentId) data.attachmentId = this.e.attachmentId;
          let check = function (id, data) {
            accident.patch(`${resourceKey}/checked`, id, data).then(() => {
              bc.msg.slide("审核完成！");
              $page.data("status", "saved");
              $page.dialog("close");  // 审核完成后关闭表单
            });
          };
          if (isEditor) this.save({afterSuccess: check(id, data)});
          else {
            if (!bc.validator.validate($page)) return;
            check(id, data);
          }
        },
        /** 上传当事司机照片 */
        uploadDriverPic: function () {
          bc.msg.alert("功能开发中！");
          // todo
        },
        /** 上传事故现场图 */
        uploadAccidentPic: function () {
          bc.msg.alert("功能开发中！");
          //todo
        },
        /** 编辑事故现场图 */
        editAccidentPic: function () {
          bc.msg.alert("功能开发中！");
          //todo
        },
        /** 上传事故照片、附件 */
        uploadAccidentAttachment: function (files) {
          // console.log(files[0].readAsBinaryString());
          file.uploadByStream(files, {
            vm: this,
            puid: `AR${this.e.id}`,
            subgroup: 3,
            onOk: function () {
              bc.msg.slide("上传成功");
              this.vm.loadAccidentAttachments();
            },
            onError: function () {
              bc.msg.slide("上传失败");
            },
            onProgress: function () {
              // todo
            }
          })
        },
        /** 上传审核附件 */
        uploadCheckedAttachment: function () {
          bc.msg.alert("功能开发中！");
          //todo
        },
        /** 选择车辆 */
        selectCar: function () {
          bs.selectCar({
            vm: this,
            multiple: false,
            onOk: function (car) {
              this.vm.clearCarInfo();
              // 设置车辆和车队
              Vue.set(this.vm.e, "carId", car.id);
              Vue.set(this.vm.e, "carPlate", car.plate);
              Vue.set(this.vm.e, "carModel", car.factoryModel);
              Vue.set(this.vm.e, "carOperateDate", new Date(car.operateDate).format("yyyy-MM-dd"));
              // todo：合同性质，承包司机
            }
          })
        },
        /** 选择司机 */
        selectDriver: function () {
          bs.selectDriver({
            vm: this,
            onOk: function (driver) {
              this.vm.clearDriverInfo();
              Vue.set(this.vm.e, "driverId", driver.id);
              Vue.set(this.vm.e, "driverPicId", `s:${driver.uid}`);
              Vue.set(this.vm.e, "driverName", driver.name);
              Vue.set(this.vm.e, "driverServiceCode", driver.cert4FWZG);
              Vue.set(this.vm.e, "driverIdentityCode", driver.certIndentity);
              Vue.set(this.vm.e, "driverHiredDate", driver.workDate);
              Vue.set(this.vm.e, "driverOrigin", driver.origin);
              Vue.set(this.vm.e, "driverLicenseDate", driver.certDriverFirstDate);
              Vue.set(this.vm.e, "driverPhone", !driver.phone ? driver.phone1 : driver.phone);
              Vue.set(this.vm.e, "driverAge",
                accident.calcInervalYear(new Date(driver.birthdate), new Date(this.vm.e.happenTime)));
              Vue.set(this.vm.e, "driverDriveYears",
                accident.calcInervalYear(new Date(driver.certDriverFirstDate), new Date(this.vm.e.happenTime)));
              // todo 紧急联系人姓名，紧急俩人电话、驾驶类型
            }
          });
        },
        /** 清空车辆信息 */
        clearCarInfo: function () {
          let keys = ["carPlate", "carId", "motorcadeName", "carModel", "carOperateDate",
            "carContractType", "carContractDrivers"];
          keys.forEach(k => {
            Vue.set(this.e, k, '');
          });
        },
        /** 清空司机信息 */
        clearDriverInfo: function () {
          let keys = ["driverName", "driverId", "driverType", "driverLinkmanName", "driverLinkmanPhone",
            "driverHiredDate", "driverPhone", "driverIdentityCode", "driverServiceCode", "driverOrigin",
            "driverAge", "driverLicenseDate", "driverDriveYears", "driverPicId"];
          keys.forEach(k => {
            Vue.set(this.e, k, '');
          });
        },
        /** 点击选择行 */
        selectItem: function (item) {
          // 只能选择可编辑的行
          Vue.set(item, "selected", !item.selected);
        },
        /**
         *  添加当事车辆、人和其他物行
         *  @param module 需要添加行的模块，cars（当事车辆）、peoples（当事人）、others（其他物品）
         */
        addItem: function (module) {
          this.e[module].push({updateTime: new Date().format("yyyy-MM-dd hh:mm")});
        },
        /**
         * 移除项目
         * @param module 需要添加行的模块，cars（当事车辆）、peoples（当事人）、others（其他物品）
         */
        removeItem: function (module) {
          let selectedIndexes = [];
          this.e[module].forEach((item, index) => item.selected && selectedIndexes.push(index));
          if (selectedIndexes.length === 0) return;
          bc.msg.confirm(`确定要删除选中的 <b>${selectedIndexes.length}</b> 项吗？`, () => {
            // 按索引号倒序删除（因 splice 会修改原始数组的值）
            selectedIndexes.reverse().forEach(index => this.e[module].splice(index, 1));
          });
        },
        /**
         * 上移项目
         * @param module 需要添加行的模块，cars（当事车辆）、peoples（当事人）、others（其他物品）
         */
        moveUpItem: function (module) {
          let selectedIndexes = [];
          this.e[module].forEach((item, index) => item.selected && selectedIndexes.push(index));
          if (selectedIndexes.length === 0) return;
          if (selectedIndexes.length > 1) {
            bc.msg.info('每次只可移动一项！');
            return;
          }
          let index = selectedIndexes[0];
          if (index === 0) return; // 第一条无法上移
          this.e[module].splice(index - 1, 0, this.e[module].splice(index, 1)[0]); // 上移
        },
        /**
         * 下移项目
         * @param module 需要添加行的模块，cars（当事车辆）、peoples（当事人）、others（其他物品）
         */
        moveDownItem: function (module) {
          let selectedIndexes = [];
          this.e[module].forEach((item, index) => item.selected && selectedIndexes.push(index));
          if (selectedIndexes.length === 0) return;
          if (selectedIndexes.length > 1) {
            bc.msg.info('每次只可移动一项！');
            return;
          }
          let index = selectedIndexes[0];
          if (index === this.e[module].length - 1) return; // 最后一条无法下移
          this.e[module].splice(index + 1, 0, this.e[module].splice(index, 1)[0]); // 下移
        },
        /**
         * 在线查看附件，如果配置参数有附件 Id 则按 Id 查看附件否则按照模块和分组查看
         * @param option 配置参数
         * @option id 附件 Id
         * @option subgroup 附件当前登记模块的所属分组
         */
        inline: function (option) {
          if (option.id) file.inlineById(option.id);
          else file.inlineByModule(`AR${this.e.id}`, option.subgroup);
        },
        /** 生成事故附件图片地址 */
        initAccidentAttachmentUrl: function (id) {
          return `${accident.fileDataServer}/inline/${id}`;
        },
        /** 生成事故附件图标样式 */
        initAccidentAttachmentIcon: function (ext) {
          return `file-icon ${ext}`
        },
        /** 判断附件是否图片类型 */
        isImageExt: function (ext) {
          let imageExts = ["BMP", "JPG", "JPEG", "PNG", "GIF"];
          return imageExts.includes(ext.toUpperCase());
        },
        // 初始化表单按钮
        showHideButtons: function () {
          if (!isManager) return;
          if ((this.e.status === "Draft" && isRecorder) || (this.e.status !== "Draft" && isEditor))
            $page.parent().find("button#save").show();
          if (["Draft", "Rejected"].includes(this.e.status) && isRecorder) $page.parent().find("button#submit").show();
          if (this.e.status === "ToCheck" && isChecker) $page.parent().find("button#check").show();
        },
        /**
         * 触发上传组件点击事件
         * @param name 上传组件名称
         * */
        triggerUploadButton: function (name) {
          $page.find(`input[name='${name}']`).click();
        }
      }
    })
  }

  // 自定义窗口的 data-option 配置
  Page.option = {width: 1011, minWidth: 600, height: 620, minHeight: 300};
  Page.option.buttons = [
    {id: "save", text: "保存", click: "save", style: "display:none"},
    {id: "submit", text: "确认提交", click: "submit", style: "display:none"},
    {id: "check", text: "审核确认", click: "check", style: "display:none"}
  ];
  return Page;
});
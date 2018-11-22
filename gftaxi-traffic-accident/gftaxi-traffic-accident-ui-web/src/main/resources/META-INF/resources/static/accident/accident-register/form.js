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
          autoWrapAttachName : false,
          thumbSize : '8em',
          fixedWidth: '75.2em',
          isChecker: isChecker,
          happenTime: "",
          sexList: [{id: "Male", label: "男"}, {id: "Female", label: "女"}],
          driverStates: accident.DRIVER_TYPES,
          accidentAttachments: []
        },
        categories: {},
        e: {registerStatus: "ToSubmit", locationLevel1: "广东", locationLevel2: "广州"}
      },
      ready: function () {
        let id = $page.data("data");
        Vue.set(this.e, "id", id);
        accident.getByModule(resourceKey, id).then(json => {
          Vue.set(this, "e", JSON.parse(JSON.stringify(Object.assign(this.e, json))));
          Vue.set(this, "origin", JSON.parse(JSON.stringify(this.e)));
          this.showHideButtons();
          // 初始化"简要描述"栏自动行高
          setTimeout(() => {
            $page.parent().find(".autoHeight").keyup()
          }, 200);
        });
        // 加载分类标准信息
        accident.categories.then(r => Vue.set(this, "categories", r));
        // 加载事故登记表单所有附件
        this.initAttachments();
      },
      watch: {
        'ui.happenTime': function (value) {
          Vue.set(this.e, "happenTime", value.replace("T", " "));
        },
        'e.happenTime': function (value) {
          Vue.set(this.ui, "happenTime", value.replace(" ", "T"));
        },
        'e.passed':function(value){
          if (typeof value === "string") this.e.passed = value === "true"
        },
        "e.dealDepartment": function (value) {
          if (value.indexOf("交警大队") !== -1) this.e.dealWay = "交警认定";
          else if (value === "公司") this.e.dealWay = "公司查勘";
          else if (value === "保险公司") this.e.dealWay = "保险查勘";
          else if (value === "无") this.e.dealWay = "协商私了";
        },
        accidentLevel: function (value) {
          Vue.set(this.e, "level", value)
        },
        isShowRoadCategories: function (value) {
          // 如果分类信息不显示“道路情况”信息则移除对应的值
          if (!value) ["light", "roadStructure", "roadType", "roadState"].forEach(key => delete this.e[key]);
        }
      },
      computed: {
        isReadOnly: function () {
          if (!isRecorder && !isEditor) return true; // 无登记和修改权限
          // 待登记或审核不通过状态但不是登记角色
          else if (["ToSubmit","Rejected"].indexOf(this.e.registerStatus) && !isRecorder) return true;
          // 已登记但不是修改角色
          else if (["ToCheck", "Approved"].indexOf(this.e.registerStatus) > -1 && !isEditor) return true;
          else return false;
        },
        driverTypeLabel: function () {
          return accident.DRIVER_TYPE_MAP[this.e.driverType] || this.e.driverType;
        },
        isShowRoadCategories: function () {
          return ["财产 1 级", "财产 2 级"].indexOf(this.accidentLevel) < 0;
        },
        isShowItemDeleteButton: function () {
          return {
            cars: this.e.cars.some(i => i.selected),
            peoples: this.e.peoples.some(i => i.selected),
            others: this.e.others ? this.e.others.some(i => i.selected) : false
          }
        },
        driverPicUrl: function () {
          if (!this.e.driverPicId) return "#";
          let picOption = this.e.driverPicId.split(":");
          return picOption[0] === "S" ?
            `${bc.root}/bc/image/download?ptype=portrait&puid=${picOption[1]}` :
            `${file.fileDataServer}/inline/${picOption[1]}`
        },
        accidentPicUrl: function () {
          return !this.ui.accidentPic ? "#" : `${file.fileDataServer}/inline/${this.ui.accidentPic.id}`
        },
        accidentLevel: function () {
          if (this.e.peoples.some(p => p.damageState === "亡")) { // 判断是否有死亡事故
            let level;
            switch (this.e.peoples.filter(p => p.damageState === "亡").length) {
              case 1: level = "死亡 1 级"; break;
              case 2: level = "死亡 2 级"; break;
              default: level = "死亡 3 级";
            }
            return level;
          } else if (this.e.peoples.some(p => p.damageState === "伤")) { // 判断是否有伤人事故
            // 大于 30000 的医疗费用判定为人伤 3 级
            if (this.e.peoples.some(p => p.guessTreatmentMoney >= 30000))
              return "人伤 3 级";
            // 大于等于 10000 而且小于 30000 的医疗费用判定为人伤 2 级
            else if (this.e.peoples.some(p => p.guessTreatmentMoney >= 10000 && p.guessTreatmentMoney < 30000))
              return "人伤 2 级";
            // 大于 0 而且小于 10000 的医疗费用判定为人伤 1 级
            else return "人伤 1 级";
          } else { // 判断是否有财产损失事故
            // 自车损失
            let selfCarLoss = this.e.cars.filter(c => c.type === "自车")
              .reduce((t, c) => t += (this.sumMonies(c.guessTowMoney, c.guessRepairMoney) | 0), 0);
            // 损失总额，包括所有车辆损失和所有其他物品损失
            let allLoss = this.e.others.reduce((t, o) => t += (o.guessMoney | 0), 0) +
              this.e.cars.reduce((t, c) => t += (this.sumMonies(c.guessTowMoney, c.guessRepairMoney) | 0), 0);
            // 自车损失大于等于 3 万元或总损失大于等于 5 万元时事故等级为"财产 3 级"
            if (selfCarLoss >= 30000 || allLoss >= 50000) return "财产 3 级";
            // 自车损失大于等于 1 万元并少于 3 万元或总损失大于等于 3 万元并少于 5 万元时事故等级为"财产 2 级"
            else if ((selfCarLoss >= 10000 && selfCarLoss < 30000) || (allLoss >= 30000 && allLoss < 50000))
              return "财产 2 级";
            else return "财产 1 级";
          }
        }
      },
      components: {
        "accident-cars-column-definitions": {template: $page.find("script[name=accident-cars-column-definitions]").html()},
        "accident-people-column-definitions": {template: $page.find("script[name=accident-people-column-definitions]").html()},
        "accident-other-column-definitions": {template: $page.find("script[name=accident-other-column-definitions]").html()}
      },
      methods: {
        /** 金额合计 */
        sumMonies: function (m1, m2) {
          let s = ((m1 || 0) * 1 + (m2 || 0) * 1);
          return s == 0 ? '' : s
        },
        /** 初始化表单附件信息 */
        initAttachments: function () {
          this.loadAccidentAttachments();
          this.loadAccidentPicAttachments();
          if (["Rejected", "Approved"].indexOf(this.e.registerStatus) > -1) this.loadCheckedAttachments()
        },
        /** 加载事故附件信息 */
        loadAccidentAttachments: function () {
          accident.get(`${file.fileDataServer}/parent/AR${this.e.id}/3`).then(attachments => {
            this.ui.accidentAttachments = attachments
          })
        },
        /** 加载事故现场图信息 */
        loadAccidentPicAttachments: function () {
          accident.get(`${file.fileDataServer}/parent/AR${this.e.id}/2`).then(attachments => {
            Vue.set(this.ui, "accidentPic", attachments[0]);
          })
        },
        /** 加载表单审核附件信息 */
        loadCheckedAttachments: function () {
          accident.get(`${file.fileDataServer}/parent/AR${this.e.id}/4`).then(attachments => {
            Vue.set(this.ui, "checkedAttachment", attachments[0]);
          })
        },
        // 获取司机事发一年内的倒查信息
        loadCarmanHistory: function () {
          if (!this.e.driverId || !this.e.happenTime) return;
          let url = `${bc.root}/bc-business/caseAdvice/getDriverBackgroundInfoByCarManId?` +
            `carManId=${this.e.driverId}&happenDate=${this.e.happenTime}`;
          accident.get(url).then(h => {
            h = JSON.parse(h);
            if (!h.stat) return;
            Vue.set(this.e, "historyAccidentCount", h.stat.count4shigulipei | 0);
            Vue.set(this.e, "historyTrafficOffenceCount", h.stat.count4jiaotongweizhang | 0);
            Vue.set(this.e, "historyServiceOffenceCount", h.stat.count4yingyunweizhang | 0);
            Vue.set(this.e, "historyComplainCount", (h.stat.count4keguantousu | 0) + (h.stat.count4gongsitousu | 0));
          })
        },
        /**
         * 保存表单
         * @param skipValidate 是否跳过验证表单的必填信息
         * @param option 回调函数
         */
        baseSave: function (skipValidate, option) {
          if (!skipValidate && !bc.validator.validate($page)) return;
          // 验证当事车辆、人项目中是否存在一条“自车”分类的信息
          if (!this.e.cars.some(c => c.type === "自车")) {
            bc.msg.alert("当事车辆项目中必须包含一条“自车”类型的信息！");
            return;
          } else if (!this.e.peoples.some(c => c.type === "自车")) {
            bc.msg.alert("当事人项目中必须包含一条“自车”类型的信息！");
            return;
          }
          let isNew = !this.e.id;
          // 当事人、车辆和其它物品添加序号
          ["cars", "peoples", "others"].forEach(it => {
            if (!this.e[it]) return;
            this.e[it].forEach((item, index) => item.sn = index + 1);
            this.origin[it].forEach((item, index) => item.sn = index + 1);
          });

          // 筛选需要提交的数据
          let ignoreKeys = ["id", "registerStatus", "code", "draftTime", "historyAccidentCount", "historyTrafficOffenceCount",
            "historyServiceOffenceCount", "historyComplainCount", "result", "comment", "attachmentId", "attachmentName"];
          let filteredOrigin = Object.keys(this.origin)
            .reduce((map, key) => {
              if (ignoreKeys.indexOf(key) === -1)
                if (this.origin[key] instanceof Array && this.origin[key].length > 0)
                  map[key] = JSON.parse(JSON.stringify(this.origin[key])).map(m => {delete m.updatedTime;return m;});
                else map[key] = this.origin[key];
              return map;
            }, {});
          let filteredCurrent = Object.keys(this.e)
            .reduce((map, key) => {
              if (ignoreKeys.indexOf(key) === -1)
                if (this.e[key] instanceof Array && this.e[key].length > 0)
                  map[key] = JSON.parse(JSON.stringify(this.e[key])).map(m => {delete m.updatedTime;return m;});
                else map[key] = this.e[key];
              return map;
            }, {});

          let data = accident.filterChangedData(filteredOrigin, filteredCurrent);

          if (accident.validateChange(data, filteredOrigin))  // 有数据改动
            accident.save(resourceKey, this.e.id || null, data).then(result => {
              if (isNew) Vue.set(this.e, "id", result.id);
              // 更新 origin 数据为当前表单数据
              Vue.set(this, "origin", JSON.parse(JSON.stringify(this.e)));
              if (option && option.afterSuccess && typeof(option.afterSuccess) === "function") option.afterSuccess();
              else {
                bc.msg.slide("保存成功！");
                $page.data("status", "saved");
              }
            });
          else {
            // 如果有回调方法则运行回调方法，否则提示无数据修改
            if (option && option.afterSuccess && typeof(option.afterSuccess) === "function") option.afterSuccess();
            else bc.msg.slide("您没有更改任何数据，无需保存！");
          }
        },
        /** 保存 */
        save: function(){this.baseSave(false, {})},
        /** 提交 */
        submit: function () {
          if (!bc.validator.validate($page)) return;
          let id = this.e.id;
          bc.msg.confirm(`确定提交案件吗？`, () => {
            // 保存表单数据
            this.baseSave(true, {
              afterSuccess: function () {
                accident.save(`${resourceKey}/to-check/${id}`).then(() => {
                  bc.msg.slide("提交成功！");
                  $page.data("status", "saved");
                  $page.dialog("close");  // 提交完成后关闭表单
                })
              }
            })
          });
        },
        /** 审核 */
        check: function () {
          // 审核结果添加必填验证
          [].forEach.call($page.find("input[name='checkedResult']"), e => e.setAttribute("data-validate", "required"));
          if (!bc.validator.validate($page)) return;
          let id = this.e.id;
          let data = {passed: this.e.passed};
          if (this.e.comment) data.comment = this.e.comment;
          if (this.ui.checkedAttachment) Object.assign(data, {
            attachmentId: this.ui.checkedAttachment.id,
            attachmentName: `${this.ui.checkedAttachment.name}.${this.ui.checkedAttachment.ext}`
          });

          bc.msg.confirm(`确定审核案件吗？`, () => {
            if (isEditor)
              this.baseSave(true, {
                afterSuccess: () =>
                  accident.save(`${resourceKey}/checked/${id}`, null, data).then(() => {
                    bc.msg.slide("审核完成！");
                    $page.data("status", "saved");
                    $page.dialog("close");  // 审核完成后关闭表单
                  })
              });
            else {
              if (!bc.validator.validate($page)) return;
              accident.save(`${resourceKey}/checked/${id}`, null, data).then(() => {
                bc.msg.slide("审核完成！");
                $page.data("status", "saved");
                $page.dialog("close");  // 审核完成后关闭表单
              });
            }
          });
        },
        /** 上传当事司机照片 */
        uploadDriverPic: function (files) {
          // 验证上传文件是否图片格式
          if (files[0].type.indexOf("image") < 0) {
            bc.msg.alert("只能上传图片格式的文件！");
            return;
          }
          // 开始上传
          file.uploadByStream(files, {
            vm: this,
            puid: `AR${this.e.id}`,
            subgroup: 1,
            onOk: function (result) {
              bc.msg.slide("上传成功");
              // 上传成功更新 driverPicId
              Vue.set(this.vm.e, "driverPicId", `C:${result.headers.location.replace(" /", "")}`);
            },
            onError: function () {
              bc.msg.slide("上传失败");
            },
            onProgress: function () {
              // todo
            }
          })
        },
        /** 上传事故现场图 */
        uploadAccidentPic: function (files) {
          // 验证上传文件是否图片格式
          if (files[0].type.indexOf("image") < 0) {
            bc.msg.alert("只能上传图片格式的文件！");
            return;
          }
          // 开始上传
          file.uploadByStream(files, {
            vm: this,
            puid: `AR${this.e.id}`,
            subgroup: 2,
            onOk: function () {
              bc.msg.slide("上传成功");
              // 加载事故现场图信息
              this.vm.loadAccidentPicAttachments()
            },
            onError: function () {
              bc.msg.slide("上传失败");
            },
            onProgress: function () {
              // todo
            }
          })
        },
        /** 编辑事故现场图 */
        editAccidentPic: function () {
          bc.msg.alert("功能开发中！");
          //todo
        },
        /** 上传事故照片、附件 */
        uploadAccidentAttachment: function (files) {
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
        uploadCheckedAttachment: function (files) {
          file.uploadByStream(files, {
            vm: this,
            puid: `AR${this.e.id}`,
            subgroup: 4,
            onOk: function () {
              bc.msg.slide("上传成功");
              this.vm.loadCheckedAttachments();
            },
            onError: function () {
              bc.msg.slide("上传失败");
            },
            onProgress: function () {
              // todo
            }
          })
        },
        /** 选择车辆 */
        selectCar: function () {
          bs.selectCar({
            vm: this,
            multiple: false,
            onOk: function (car) {
              this.vm.clearCarInfo();
              // 设置车辆和车队
              if(car.id)Vue.set(this.vm.e, "carId", car.id);
              if(car.plate)Vue.set(this.vm.e, "carPlate", car.plate);
              if(car.factoryModel)Vue.set(this.vm.e, "carModel", car.factoryModel);
              if(car.operateDate)Vue.set(this.vm.e, "carOperateDate", new Date(car.operateDate).format("yyyy-MM-dd"));
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
              if (driver.id) Vue.set(this.vm.e, "driverId", driver.id);
              if (driver.uid) Vue.set(this.vm.e, "driverPicId", `s:${driver.uid}`);
              if (driver.name) Vue.set(this.vm.e, "driverName", driver.name);
              if (driver.cert4FWZG) Vue.set(this.vm.e, "driverServiceCode", driver.cert4FWZG);
              if (driver.certIndentity) Vue.set(this.vm.e, "driverIdentityCode", driver.certIndentity);
              if (driver.workDate) Vue.set(this.vm.e, "driverHiredDate", driver.workDate);
              if (driver.origin) Vue.set(this.vm.e, "driverOrigin", driver.origin);
              if (driver.certDriverFirstDate) Vue.set(this.vm.e, "driverLicenseDate", driver.certDriverFirstDate);
              if (driver.phone1 || driver.phone)
                Vue.set(this.vm.e, "driverPhone", !driver.phone ? driver.phone1 : driver.phone);
              if (driver.birthdate) Vue.set(this.vm.e, "driverAge",
                accident.calcInervalYear(new Date(driver.birthdate), new Date(this.vm.e.happenTime)));
              if (driver.certDriverFirstDate) Vue.set(this.vm.e, "driverDriveYears",
                accident.calcInervalYear(new Date(driver.certDriverFirstDate), new Date(this.vm.e.happenTime)));
              // todo 紧急联系人姓名，紧急俩人电话、驾驶类型
              // 查询司机倒查信息
              this.vm.loadCarmanHistory()
            }
          });
        },
        /** 清空车辆信息 */
        clearCarInfo: function () {
          let keys = ["carPlate", "carId", "motorcadeName", "carModel", "carOperateDate",
            "carContractType", "carContractDrivers"];
          keys.forEach(k => delete this.e[k]);
        },
        /** 清空司机信息 */
        clearDriverInfo: function () {
          let keys = ["driverName", "driverId", "driverType", "driverLinkmanName", "driverLinkmanPhone",
            "driverHiredDate", "driverPhone", "driverIdentityCode", "driverServiceCode", "driverOrigin",
            "driverAge", "driverLicenseDate", "driverDriveYears", "driverPicId"];
          keys.forEach(k => delete this.e[k]);
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
        addItem: function (module) {this.e[module].push({})},
        /**
         * 移除项目
         * @param module 需要添加行的模块，cars（当事车辆）、peoples（当事人）、others（其他物品）
         */
        removeItem: function (module) {
          let selectedIndexes = [];
          this.e[module].forEach((item, index) => item.selected && selectedIndexes.push(index));
          if (selectedIndexes.length === 0) return;
          if (["cars", "peoples"].indexOf(module) > -1 &&
            (this.e[module].length === 1 || this.e[module].length === selectedIndexes.length)) {
            bc.msg.alert(`${module === "cars" ? "当事车辆" : "当事人"}必须存在一条信息！`);
            return;
          }
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
        inlineDriverPic: function () {
          window.open(this.driverPicUrl)
        },
        /**
         *  在线查看附件
         *  选项配置有 id 则使用 id 在线查看附件，否则通过所属模块和分组查看
         *
         *  @param option 在线查看选项配置
         *  @option id 附件Id
         *  @option puid 附件所属模块
         *  @option subgroup 附件所属模块的所属分组
         */
        inline: function (option) {
          if (option.id) file.inlineById(option.id);
          else file.inlineByModule(`AR${this.e.id}`, option.subgroup);
        },
        /** 生成事故附件图片地址 */
        initAccidentAttachmentUrl: function (id) {
          return `${file.fileDataServer}/inline/${id}`;
        },
        /** 生成事故附件图标样式 */
        initAccidentAttachmentIcon: function (ext) {
          return `file-icon ${ext}`
        },
        /** 判断附件是否图片类型 */
        isImageExt: function (ext) {
          let imageExts = ["BMP", "JPG", "JPEG", "PNG", "GIF"];
          return imageExts.indexOf(ext.toUpperCase()) > -1;
        },
        // 初始化表单按钮
        showHideButtons: function () {
          if (!isManager) return;
          if ((["ToSubmit","Rejected"].indexOf(this.e.registerStatus) > -1 && isRecorder) || (this.e.registerStatus !== "ToSubmit" && isEditor))
            $page.parent().find("button#save").show();
          if (["ToSubmit", "Rejected"].indexOf(this.e.registerStatus) > -1 && isRecorder) $page.parent().find("button#submit").show();
          if (this.e.registerStatus === "ToCheck" && isChecker) $page.parent().find("button#check").show();
        },
        /** 打开事故等级说明页面 */
        openLevelExplain: function () {
          accident.open(resourceKey, {
            mid: `${resourceKey}/level-explain`,
            name: "事故等级划分标准",
          }, 'level-explain.html');
        },
        /**
         * 触发上传组件点击事件
         * @param name 上传组件名称
         * */
        triggerUploadButton: function (name) {
          $page.find(`input[name='${name}']`).click();
        },
        /** 事故处理部门变动处理 */
        changeDealDepartment: function(event){$(event.target).blur();}
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
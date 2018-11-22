define(["bc", "bs", "bs/carMan.js", "vue", "context", 'static/accident/api', 'static/accident/simter-file/api'],
  function (bc, bs, carMan, Vue, context, accident, file) {
    "use strict";
    let resourceKey = "accident-report";
    let isRecorder = context.is("ACCIDENT_REPORT_SUBMIT");
    let isEditor = context.is("ACCIDENT_REPORT_MODIFY");
    let isChecker = context.is("ACCIDENT_REPORT_CHECK");
    let isManager = isRecorder || isEditor || isChecker;

    // 保存操作时需要提交的数据 Key
    let submitKeys = ["level", "peoples", "cars", "others", "appointDriverReturnTime", "actualDriverReturnTime",
      "driverReturnSponsorName", "driverReturnSupporterName", "safetyStartTime", "safetyEndTime", "safetySponsorName",
      "safetySupporterName", "talkStartTime", "talkEndTime", "talkSponsorName", "talkSupporterName", "caseReason",
      "safetyComment", "takeFurther", "correctiveAction", "driverAttitude", "lawsuit"];

    function Page($page) {
      this["vm"] = new Vue({
        el: $page[0],
        data: {
          ui: {
            fixedWidth: '81.7em',
            isChecker: isChecker,
            isShowItems: {
              carInfo: false,
              peopleInfo: false,
              category: true,
              maintain: false,
              educate: true,
              workflow: false,
              lawsuit: false,
              financial: false,
              compensation: false,
              attachment: true
            }
          }
        },
        ready: function () {
          let id = $page.data("data");
          accident.getByModule(resourceKey, id).then(json => {
            Vue.set(this, "e", json);
            Vue.set(this, "origin", JSON.parse(JSON.stringify(json)));
            this.showHideButtons();
            this.loadPolicy();  // 加载车辆保险信息
            this.initAttachments(); // 加载事故登记表单所有附件
            // 初始化"简要描述"栏自动行高
            setTimeout(() => {
              $page.parent().find(".autoHeight").keyup()
            }, 200);
          });
          // 加载分类标准信息
          accident.categories.then(r => Vue.set(this, "categories", r));
        },
        watch: {
          accidentLevel: function (value) {Vue.set(this.e, "level", value)}
        },
        computed: {
          isReadOnly: function () {
            if (!isRecorder && !isEditor) return true; // 无登记和修改权限
            else if (this.e.reportStatus === "ToSubmit" && !isRecorder) return true; // 待登记状态但不是登记角色
            // 已登记或已审核通过但不是修改角色
            else if (["ToCheck", "Approved"].indexOf(this.e.reportStatus) > -1 && !isEditor) return true;
            else return false;
          },
          driverTypeLabel: function () {
            return accident.DRIVER_TYPE_MAP[this.e.driverType] || this.e.driverType;
          },
          isShowRoadCategories: function () {
            return ["财产 1 级", "财产 2 级"].indexOf(this.e.level) < 0;
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
          /** 事故案件进度 */
          accidentStage: function () {
            let stages = [
              {label: "登记", isDone: true},
              {label: "处理中", isDone: true},
              {label: "理赔中", isDone: false},
              {label: "已领款", isDone: false},
              {label: "已结案", isDone: false}
            ];
            // todo 根据报告中理赔、财务、流程信息判断事故案件进度
            return stages;
          },
          /** 当前案件进度 */
          currentStage: function () {
            return this.accidentStage.reverse().find(s => s.isDone).label;
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
              if (this.e.peoples.some(p => p.actualTreatmentMoney >= 30000))
                return "人伤 3 级";
              // 大于等于 10000 而且小于 30000 的医疗费用判定为人伤 2 级
              else if (this.e.peoples.some(p => p.actualTreatmentMoney >= 10000 && p.actualTreatmentMoney < 30000))
                return "人伤 2 级";
              // 大于 0 而且小于 10000 的医疗费用判定为人伤 1 级
              else return "人伤 1 级";
            } else { // 判断是否有财产损失事故
              // 自车损失
              let selfCarLoss = this.e.cars.filter(c => c.type === "自车")
                .reduce((t, c) => t += (this.sumMonies(c.actualTowMoney, c.actualRepairMoney) | 0), 0);
              // 损失总额，包括所有车辆损失和所有其他物品损失
              let allLoss = this.e.others.reduce((t, o) => t += (o.actualMoney | 0), 0) +
                this.e.cars.reduce((t, c) => t += (this.sumMonies(c.actualTowMoney, c.actualRepairMoney) | 0), 0);
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
          "accident-cars-policy-column-definitions": {template: $page.find("script[name=accident-cars-policy-column-definitions]").html()},
          "accident-cars-column-definitions": {template: $page.find("script[name=accident-cars-column-definitions]").html()},
          "accident-people-column-definitions": {template: $page.find("script[name=accident-people-column-definitions]").html()},
          "accident-other-column-definitions": {template: $page.find("script[name=accident-other-column-definitions]").html()},
          "accident-workflow-column-definitions": {template: $page.find("script[name=accident-workflow-column-definitions]").html()},
          "accident-attachment-column-definitions": {template: $page.find("script[name=accident-attachment-column-definitions]").html()}
        },
        methods: {
          /** 金额合计 */
          sumMonies: function (m1, m2) {
            let s = ((m1 || 0) * 1 + (m2 || 0) * 1);
            return s == 0 ? '' : s
          },
          /** 初始化表单附件信息 */
          initAttachments: function () {
            // 事故附件
            this.loadAccidentAttachments();
            // 事故现场图
            this.loadAccidentPicAttachments();
            // 审核附件
            if (["Rejected", "Approved"].indexOf(this.e.reportStatus) > -1) this.loadCheckedAttachments()
          },
          /** 加载车辆保险信息 */
          loadPolicy: function () {
            if (!this.e.carId || !this.e.happenTime) return;
            let url = `${bc.root}/bc-business/caseAccident/loadPolicyInfo?carId=${this.e.carId}&happenTime=${this.e.happenTime}`;
            accident.get(url).then(policy => {
              policy = JSON.parse(policy)[0];
              policy.buyPlantLabel = policy.buyPlant
                .map(p => `[${p.name}:${p.coverage}${p.description ? `:${p.description}` : ""}]`).join("");
              Vue.set(this, "policy", policy)
            })
          },
          /** 加载事故附件信息 */
          loadAccidentAttachments: function () {
            // todo
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
            // 验证安全教育栏开始和结束时间先后顺序
            let deniedTimeLabel = [{label: "安全教育", startKey: "safetyStartTime", endKey: "safetyEndTime"},
              {label: "诫勉谈话", startKey: "talkStartTime", endKey: "talkEndTime"}].reduce((m, s) => {
              if (new Date(this.e[s.endKey]) < new Date(this.e[s.startKey])) m.push(s.label);
              return m;
            }, []);
            if (deniedTimeLabel.length > 0) {
              bc.msg.alert(`${deniedTimeLabel.join("，")}的结束时间不可以小于开始时间！`);
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
            let filteredOrigin = submitKeys.reduce((map, key) => {
              if (this.origin[key] instanceof Array && this.origin[key].length > 0)
                map[key] = JSON.parse(JSON.stringify(this.origin[key])).map(m => {delete m.updatedTime;return m;});
              else map[key] = this.origin[key];
              return map
            }, {});
            let filteredCurrent = submitKeys.reduce((map, key) => {
              if (this.e[key] instanceof Array && this.e[key].length > 0)
                map[key] = JSON.parse(JSON.stringify(this.e[key])).map(m => {delete m.updatedTime;return m;});
              else map[key] = this.e[key];
              return map
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
          save: function () {
            if (this.origin.level !== this.e.level) {
              let situation;
              if (this.e.level.indexOf("死亡") !== -1 || this.e.level.indexOf("人伤") !== -1 ||
                this.origin.level.indexOf("死亡") !== -1 || this.origin.level.indexOf("人伤") !== -1)
                situation = "事故伤亡情况";
              else situation = "财产损失金额";
              let msg = `因当前${situation}的变动，导致事故等级从“${this.origin.level}”变为“${this.e.level}”
              ，确定要保存案件吗？`;
              bc.msg.confirm(msg, () => this.baseSave(false, {}));
            } else this.baseSave(false, {});
          },
          /** 提交 */
          submit: function () {
            if (!bc.validator.validate($page)) return;
            let id = this.e.id;
            let msg = `确定提交案件吗？`;
            if (this.origin.level !== this.e.level) {
              let situation;
              if (this.e.level.indexOf("死亡") !== -1 || this.e.level.indexOf("人伤") !== -1 ||
                this.origin.level.indexOf("死亡") !== -1 || this.origin.level.indexOf("人伤") !== -1)
                situation = "事故伤亡情况";
              else situation = "财产损失金额";
              msg = `因当前${situation}的变动，导致事故等级从“${this.origin.level}”变为“${this.e.level}”，${msg}`;
            }
            bc.msg.confirm(msg, () => {
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
                })
              }
            });
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
          /** 初始化表单按钮 */
          showHideButtons: function () {
            if (!isManager) return;
            if (isEditor || (["ToSubmit", "Rejected"].indexOf(this.e.reportStatus) > -1 && isRecorder))
              $page.parent().find("button#save").show();
            if (["ToSubmit", "Rejected"].indexOf(this.e.reportStatus) > -1 && isRecorder)
              $page.parent().find("button#submit").show();
            if (this.e.reportStatus === "ToCheck" && isChecker) $page.parent().find("button#check").show();
          },
          /**
           * 触发上传组件点击事件
           * @param name 上传组件名称
           * */
          triggerUploadButton: function (name) {
            $page.find(`input[name='${name}']`).click();
          },
          developingAlert: function(){bc.msg.alert('功能开发中...')}
        }
      })
    }

    // 自定义窗口的 data-option 配置
    Page.option = {width: 1095, minWidth: 600, height: 620, minHeight: 300};
    Page.option.buttons = [
      {id: "save", text: "保存", click: "save", style: "display:none"},
      {id: "submit", text: "确认提交", click: "submit", style: "display:none"},
      {id: "check", text: "审核确认", click: "check", style: "display:none"}
    ];
    return Page;
  });
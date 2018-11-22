define(["bc", "vue", "context", "static/accident/api", "static/accident/simter-file/api", "bc/vue/components"]
  , function (bc, Vue, context, accident, file) {
    "use strict";
    let resourceKey = "accident-register";
    let resourceName = "事故登记";

    let isRecorder = context.is("ACCIDENT_REGISTER_SUBMIT");
    let isChecker = context.is("ACCIDENT_REGISTER_CHECK");
    let isAllRoles = isRecorder && isChecker;

    // 初始化视图状态配置
    function initReportStatuses() {
      let statuses = accident.AUDIT_STATUSES.reduce((m, s, i) => {
        m[s.id] = {sn: i, label: s.label, isChecked: false};
        return m;
      }, {});
      if (isAllRoles) {
        statuses.ToSubmit.isChecked = true;
        statuses.ToCheck.isChecked = true;
        statuses.Rejected.isChecked = true;
      } else if (isChecker) {
        statuses.ToCheck.isChecked = true;
      } else if (isRecorder) {
        statuses.ToSubmit.isChecked = true;
        statuses.Rejected.isChecked = true;
      }
      return statuses;
    }

    // 表格头
    let columns = [
      {
        id: "registerStatus", label: "案件状态", width: "10em", escape: false,
        filter: function (value, row) {
          let template = `<span class="ui-icon ui-icon-flag operation" style="display: inline-block;vertical-align: middle;cursor: pointer"></span>`;
          if (value === "Rejected" && row.attachmentId)
            template += `<span class="ui-icon ui-icon-document attachment" style="display: inline-block;vertical-align: middle;cursor: pointer"></span>`;
          return `${template}${accident.AUDIT_STATUS_MAP[value] || value}${row.checkedCount > 1 ? `[${row.checkedCount}]` : ""}`;
        },
        rowCellClick: function (value, row, column, event) {
          // 打开事故操作记录
          if ($(event.target).hasClass("operation")) {
            accident.open("accident-operation", {
              mid: `accident-operation-${row.id}`,
              name: `事故操作记录 ${row.code}`,
              data: {
                cluster: `accident-${row.id}`,
                title: `事故操作记录 - ${row.carPlate} ${row.driverName} ${row.happenTime}`,
                operationHandle: accident.pavedSubList
              }
            }, "view.html");
          }
          if ($(event.target).hasClass("attachment")) this.$parent.inline(row.attachmentId);
        }
      },
      {
        id: "happenTime", label: "事发时间", width: "10em",
        rowCellStyle: "cursor: pointer; text-decoration: underline",
        rowCellClick: function (value, row) {
          accident.open(resourceKey, {
            mid: `${resourceKey}-${row.id}`,
            name: `${resourceName} ${row.code}`,
            data: row.id,
            afterClose: (status) => {
              if (status) this.reload();
            }
          });
        }
      },
      {id: "motorcadeName", label: "事发车队", width: "5em"},
      {id: "carPlate", label: "事故车号", width: "6em", rowCellClass: "monospace"},
      {
        id: "driverName", label: "当事司机", width: "7em",
        filter: function (value, row) {
          let driverTypeMap = {Official: "正班", Shift: "替班", Outside: "非编"};
          return row.driverType && row.driverType !== undefined ? `[${driverTypeMap[row.driverType]}] ${value}` : value;
        }
      },
      {id: "location", label: "事发地点", width: "13em"},
      {id: "hitForm", label: "事故形态", width: "7em"},
      {id: "hitType", label: "碰撞类型", width: "7em"},
      {id: "authorName", label: "接案人", width: "5em"},
      {
        id: "overdueDraft", label: "逾期报案", width: "5em", rowCellStyle: "text-align: center",
        filter: function (value, row) {
          let lastHandleTime = new Date(row.happenTime);
          return value ? accident.calcInervalDayAndHour(lastHandleTime, new Date(row.draftTime)) : '';
        }
      },
      {id: "draftTime", label: "报案时间", width: "10em"},
      {
        id: "overdueRegister", label: "逾期登记", width: "5em", rowCellStyle: "text-align: center",
        filter: function (value, row) {
          return value ? accident.calcInervalDayAndHour(new Date(row.happenTime), new Date(row.registerTime)) : '';
        }
      },
      {id: "registerTime", label: "登记提交时间", width: "10em"},
      {id: "code", label: "事故编号", width: "7.5em"}
    ];

    return function Page($page) {
      new Vue({
        el: $page[0],
        data: {
          url: `${accident.dataServer}/${resourceKey}`,
          columns: columns,
          statuses: initReportStatuses()
        },
        computed: {
          // 视图所有查询条件的封装
          condition: function () {
            let condition = {};
            condition.status = Object.keys(this.statuses).filter(k => this.statuses[k].isChecked === true).join(",");
            return condition;
          }
        },
        methods: {
          reload: function () {
            this.$refs.grid.reload();
          },
          dblclick: function (data) {
            accident.open(resourceKey, {
              mid: `${resourceKey}-${data.id}`,
              name: `${resourceName} ${data.code}`,
              data: data.id,
              afterClose: (status) => {
                if (status) this.reload();
              }
            });
          },
          /**
           * 在线查看附件
           *
           * @param id 附件id
           */
          inline: function (id) {
            file.inlineById(id);
          }
        }
      });
    };
  });
define(["bc", "vue", "context", "static/accident/api", "static/accident/simter-file/api", "bc/vue/components"]
  , function (bc, Vue, context, accident, file) {
    "use strict";
    let resourceKey = "accident-report";
    let resourceName = "事故报告";

    let isRecorder = context.is("ACCIDENT_REPORT_SUBMIT");
    let isChecker = context.is("ACCIDENT_REPORT_CHECK");
    let isAllRoles = isRecorder && isChecker;

    // 表格头
    let columns = [
      {
        id: "status", label: "案件状态", width: "10em", escape: false,
        filter: function (value, row) {
          let statusMap = {Draft: "待登记", ToCheck: "待审核", Rejected: "审核不通过", Approved: "审核通过"};
          let template = `<span class="ui-icon ui-icon-flag operation" style="display: inline-block;vertical-align: middle;cursor: pointer"></span>`;
          if (value === "Rejected" && row.attachmentId)
            template += `<span class="ui-icon ui-icon-document attachment" style="display: inline-block;vertical-align: middle;cursor: pointer"></span>`;
          return `${template}${statusMap[value] || value}${row.checkedCount > 1 ? `[${row.checkedCount}]` : ""}`;
        },
        rowCellClick: function (value, row, column, event) {
          if ($(event.target).hasClass("operation")) this.$parent.openOperation();
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
      {id: "carModel", label: "车辆品牌", width: "5em"},
      {
        id: "driverName", label: "当事司机", width: "7em",
        filter: function (value, row) {
          let driverTypeMap = {Official: "正班", Shift: "替班", Outside: "非编"};
          return row.driverType && row.driverType !== undefined ? `[${driverTypeMap[row.driverType]}] ${value}` : value;
        }
      },
      {id: "location", label: "事发地点", width: "13em"},
      {id: "duty", label: "责任", width: "3em", rowCellStyle: "text-align: center"},
      {id: "level", label: "事故等级", width: "6em"},
      {
        id: "overdueRegister", label: "逾期登记", width: "5em", rowCellStyle: "text-align: center",
        filter: function (value, row) {
          return value ? accident.calcInervalDayAndHour(new Date(row.happenTime), new Date(row.registerTime)) : '';
        }
      },
      {id: "hitForm", label: "事故形态", width: "7em"},
      {id: "appointDriverReturnTime", label: "约定司机回队时间", width: "10em"},
      {id: "registerTime", label: "登记提交时间", width: "10em"},
      {
        id: "overdueReport", label: "逾期报告", width: "5em", rowCellStyle: "text-align: center",
        filter: function (value, row) {
          return value ? accident.calcInervalDayAndHour(new Date(row.happenTime), new Date(row.reportTime)) : '';
        }
      },
      {id: "reportTime", label: "报告提交时间", width: "10em"},
      {
        id: "overdueDraft", label: "逾期报案", width: "5em", rowCellStyle: "text-align: center",
        filter: function (value, row) {
          return value ? accident.calcInervalDayAndHour(new Date(row.happenTime), new Date(row.draftTime)) : '';
        }
      },
      {id: "registerTime", label: "报案时间", width: "10em"},
      {id: "code", label: "事故编号", width: "7.5em"}
    ];

    return function Page($page) {
      new Vue({
        el: $page[0],
        data: {
          url: `${accident.dataServer}/${resourceKey}`,
          columns: columns,
          statuses: {
            Draft: {label: "待报告", isChecked: false},
            ToCheck: {label: "待审核", isChecked: false},
            Rejected: {label: "审核不通过", isChecked: false},
            Approved: {label: "审核通过", isChecked: false}
          }
        },
        ready: function () {
          this.initViewStatuses()
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
          /** 初始化视图状态勾选框 */
          initViewStatuses: function () {
            if (isAllRoles) {
              this.statuses.Draft.isChecked = true;
              this.statuses.ToCheck.isChecked = true;
              this.statuses.Rejected.isChecked = true;
            } else if (isChecker) {
              this.statuses.ToCheck.isChecked = true;
            } else if (isRecorder) {
              this.statuses.Draft.isChecked = true;
              this.statuses.Rejected.isChecked = true;
            }
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
          /** 打开事故操作记录 */
          openOperation: function () {
            // todo
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
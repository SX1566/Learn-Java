define(["bc", "vue", "context", "static/accident/api", "bc/vue/components"], function (bc, Vue, context, accident) {
  "use strict";
  let resourceKey = "accident-register";
  let resourceName = "事故登记";

  let isRecorder = context.is("ACCIDENT_REGISTER_RECORD");
  let isChecker = context.is("ACCIDENT_REGISTER_CHECK");
  let isAllRoles = isRecorder && isChecker;

  // 汇总统计视图列
  let statSumColumns = [
    {id: "scope", label: "范围", width: "3em"},
    {id: "total", label: "事故总数", width: "6em", rowCellStyle: "text-align: center"},
    {id: "checked", label: "已登已审", width: "6em", rowCellStyle: "text-align: center"},
    {id: "checking", label: "已登在审", width: "6em", rowCellStyle: "text-align: center"},
    {id: "drafting", label: "尚未登记", width: "6em", rowCellStyle: "text-align: center"},
    {id: "overdueDraft", label: "逾期报案", width: "6em", rowCellStyle: "text-align: center"},
    {id: "overdueRegister", label: "逾期登记", width: "6em", rowCellStyle: "text-align: center"}
  ];
  // 待登记视图列
  let todoColumns = [
    {id: "code", label: "事故编号", width: "7.5em"},
    {id: "carPlate", label: "事故车号", width: "5.5em", rowCellClass: "monospace"},
    {id: "driverName", label: "当事司机", width: "5em"},
    {id: "happenTime", label: "事发时间", width: "9.5em"},
    {id: "location", label: "事发地点", width: "13em"},
    {id: "hitForm", label: "事故形态", width: "7em"},
    {id: "hitType", label: "碰撞类型", width: "5em"},
    {id: "authorName", label: "接案人", width: "5em"},
    {id: "reportTime", label: "报案时间", width: "9.5em"},
    {
      id: "overdueReport", label: "逾期报案", width: "5em",
      filter: function (value, row) {
        return !value ? "否" : accident.calcInervalDayAndHour(new Date(row.happenTime), new Date(row.reportTime));
      }
    }
  ];

  // 已审核视图列
  let checkedColumns = [
    {id: "code", label: "事故编号", width: "7.5em"},
    {id: "carPlate", label: "事故车号", width: "5.5em", rowCellClass: "monospace"},
    {id: "driverName", label: "当事司机", width: "5em"},
    {id: "checkedComment", label: "审核意见", width: "20em"},
    {id: "attachmentName", label: "附件", width: "14em"},
    {id: "checkedResult", label: "审核标记", width: "5em"},
    {id: "checkerName", label: "审核人", width: "5em"},
    {id: "checkedCount", label: "审核次数", width: "5em"},
    {id: "checkedTime", label: "审核时间", width: "9.5em"},
  ];

  return function Page($page) {
    // 用户为审核员则待登记视图添加"提交时间"和"逾期登记"字段
    if (isChecker) todoColumns = todoColumns.concat([
      {id: "submitTime", label: "提交时间", width: "9.5em"},
      {
        id: "overdueRegister", label: "逾期登记", width: "5em",
        filter: function (value, row) {
          return !value ? "否" : accident.calcInervalDayAndHour(new Date(row.happenTime), new Date(row.registerTime));
        }
      }
    ]);
    else todoColumns.find(c => c.id === "location").width = "27.45em"; // 否则加宽视图事故地点列

    new Vue({
      el: $page[0],
      data: {
        isRecorder: isRecorder,
        statSumView: {
          url: `${accident.dataServer}/${resourceKey}/stat/summary`,
          columns: statSumColumns,
          ui: {isOpen: true}
        },
        todoView: {
          url: `${accident.dataServer}/${resourceKey}/todo`,
          columns: todoColumns,
          title: "",
          status: "",
          statuses: [{id: "Draft", label: "待登记"}, {id: "ToCheck", label: "已登待审"}],
          ui: {isOpen: true}
        },
        checkedView: {
          url: `${accident.dataServer}/${resourceKey}/checked`,
          columns: checkedColumns,
          status: "Rejected",
          statuses: [{id: "Rejected", label: "审核不通过"}, {id: "Approved", label: "审核通过"}],
          ui: {isOpen: true}
        }
      },
      ready: function () {
        this.initTodoView();
      },
      watch: {
        "todoView.status": function () {
          this.reloadTodo();
        },
        "checkedView.status": function () {
          this.reloadChecked();
        }
      },
      computed: {
        checkedViewCondition: function () {
          let condition = {};
          if (this.checkedView.status) condition.status = this.checkedView.status;
          return condition;
        },
        todoViewCondition: function () {
          let condition = {};
          if (this.todoView.status) condition.status = this.todoView.status;
          return condition;
        }
      },
      methods: {
        /** 刷新汇总统计视图数据 */
        reloadStatSum: function () {
          this.$refs.stat.reload();
        },
        /** 刷新待审核视图数据 */
        reloadTodo: function () {
          this.$refs.todo.reload();
        },
        /** 刷新已登记视图数据 */
        reloadChecked: function () {
          this.$refs.checked.reload();
        },
        /** 视图行双击事件 */
        dblclickRow: function (data) {
          accident.open(resourceKey, {
            mid: `${resourceKey}-${data.code}`,
            name: `${data.code} ${resourceName}`,
            data: data.code,
            afterClose: (status) => {
              if (status) this.reload();
            }
          });
        },
        /** 根据用户角色初始化待审核视图标题和状态 */
        initTodoView: function () {
          if (isAllRoles) {
            this.todoView.title = "待登记/待审核：";
            this.todoView.status = "";
          } else if (isRecorder) {
            this.todoView.title = "待登记/已登待审的案件：";
            this.todoView.status = "Draft";
          } else if (isChecker) {
            this.todoView.title = "待审核案件：";
            this.todoView.status = "ToCheck";
          } else { // 查阅角色的视图
            this.todoView.title = "待登记/待审的案件：";
            this.todoView.status = "";
          }
        },
        /** 在线查看附件 */
        openAttach: function () {
          // todo
        },
        /** 隐藏展示视图 */
        showHideView: function (module) {
          this[module].ui.isOpen = !this[module].ui.isOpen;
        }
      }
    });
  };
});
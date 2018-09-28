define(["bc", "vue", "context", "static/accident/api", "bc/vue/components"], function (bc, Vue, context, accident) {
  "use strict";

  let currentYear = new Date().getFullYear();
  function zero2empty(v) {
    return v === 0 ? "" : v
  }

  return function Page($page) {
    new Vue({
      el: $page[0],
      data: {
        scopeTypes: [{id: "Monthly", label: "按月"}, {id: "Quarterly", label: "按季度"}, {id: "Yearly", label: "按年"}],
        scopeType: "Monthly",
        from: `${currentYear}-01`,
        to: `${currentYear}-12`
      },
      watch: {
        scopeType: function (value) {
          if (value === "Monthly") {
            this.from = `${currentYear}-01`;
            this.to = `${currentYear}-12`;
          } else if (value === "Quarterly") {
            this.from = currentYear;
            this.to = currentYear;
          } else {
            this.from = currentYear - 1;
            this.to = currentYear;
          }
          Vue.nextTick(this.reload);
        }
      },
      computed: {
        url: function () {
          return `${accident.dataServer}/accident-register/stat-${this.scopeType.toLowerCase()}-summary`;
        },
        columns: function () {
          let scopeWidths = {Monthly: "7.5em", Quarterly: "9em", Yearly: "5em"};
          return [
            {id: "scope", label: "统计范围", width: scopeWidths[this.scopeType]},
            {id: "total", label: "事故总数", width: "6em", rowCellStyle: "text-align: center", filter: zero2empty},
            {id: "checked", label: "已登已审", width: "6em", rowCellStyle: "text-align: center", filter: zero2empty},
            {id: "checking", label: "已登在审", width: "6em", rowCellStyle: "text-align: center", filter: zero2empty},
            {id: "drafting", label: "尚未登记", width: "6em", rowCellStyle: "text-align: center", filter: zero2empty},
            {id: "overdueDraft", label: "逾期报案", width: "6em", rowCellStyle: "text-align: center", filter: zero2empty},
            {id: "overdueRegister", label: "逾期登记", width: "6em", rowCellStyle: "text-align: center", filter: zero2empty}
          ];
        },
        condition: function () {
          if (!this.from && !this.to) return {};
          let condition = {};
          if (this.from) condition.from = `${this.from}`.replace("-", "");
          if (this.to) condition.to = `${this.to}`.replace("-", "");
          return condition;
        }
      },
      methods: {
        reload: function () {
          if (!this.validateScope()) {
            let type = this.scopeType === "Monthly" ? "月份" : "年份";
            bc.msg.alert(`开始${type}必须小于结束${type}！`);
            return;
          }
          this.$refs.grid.reload();
        },
        validateScope: function () {
          if (!this.from || !this.to) return true;
          return `${this.from}`.replace("-", "") <= `${this.to}`.replace("-", "");
        }
      }
    });
  };
});
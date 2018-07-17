define(["bc", "vue", "context", "static/accident/api", "bc/vue/components"], function (bc, Vue, context, accident) {
  "use strict";
  let resourceKey = "accident-draft";
  let resourceName = "事故报案";

  // 表格头
  let columns = [
    {
      id: "status", label: "状态", width: "4em",
      filter: function (value) {
        if ("Todo" === value) return "待登记";
        else if ("Done" === value) return "已登记";
        else return value;
      }
    },
    {id: "code", label: "事故编号", width: "7.5em"},
    {id: "carPlate", label: "事故车号", width: "6.5em"},
    {id: "driverName", label: "当事司机", width: "5em"},
    {id: "happenTime", label: "事发时间", width: "9.5em"},
    {id: "location", label: "事发地点", width: "20em"},
    {id: "hitForm", label: "事故形态", width: "7em"},
    {id: "hitType", label: "碰撞类型", width: "5em"},
    {id: "authorName", label: "接案人", width: "5em"},
    {id: "reportTime", label: "报案时间", width: "9.5em"},
    {
      id: "overdue", label: "逾期报案", width: "5em",
      filter: function (value) {
        return value ? "是" : "否";
      }
    },
    {id: "motorcade", label: "所属车队", width: "5em"},
    {id: "source", label: "报案来源", width: "5em"},
  ];

  return function Page($page) {
    new Vue({
      el: $page[0],
      data: {
        url: `${accident.dataServer}/${resourceKey}`,
        columns: columns,
        status: 'Todo',
        statuses: [{id: 'Todo', label: '待登记'}, {id: 'Done', label: '已登记'}, {id: '', label: '全部'}],
        fuzzySearch: '',
        isManager: context.isAny("ACCIDENT_DRAFT_MODIFY", "ACCIDENT_DRAFT_SUBMIT")
      },
      computed: {
        // 视图所有查询条件的封装
        condition: function () {
          let condition = {};
          if (this.fuzzySearch) condition.search = encodeURIComponent(this.fuzzySearch);
          if (this.status) condition.status = this.status;
          return condition;
        }
      },
      methods: {
        reload: function () {
          this.$refs.grid.reload();
        },
        create: function () {
          accident.open(resourceKey, {
            mid: `${resourceKey}-create`,
            name: `新建${resourceName}`,
            afterClose: (status) => {
              if (status) this.reload();
            }
          });
        },
        open: function () {
          let sel = this.$refs.grid.selection;
          if (sel.length === 1) {
            accident.open(resourceKey, {
              mid: `${resourceKey}-${sel[0].code}`,
              name: `${sel[0].code} ${resourceName}`,
              data: sel[0].code,
              afterClose: (status) => {
                if (status) this.reload();
              }
            });
          } else if (sel.length === 0) bc.msg.slide("请先选择！");
          else if (sel.length > 1) bc.msg.slide("只能选择一条！");
        },
        dblclickRow: function (row) {
          accident.open(resourceKey, {
            mid: `${resourceKey}-${row.code}`,
            name: `${row.code} ${resourceName}`,
            data: row.code,
            afterClose: (status) => {
              if (status) this.reload();
            }
          });
        }
      }
    });
  };
});
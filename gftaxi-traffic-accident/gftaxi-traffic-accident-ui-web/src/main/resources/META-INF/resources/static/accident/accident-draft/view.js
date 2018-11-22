define(["bc", "vue", "context", "static/accident/api", "bc/vue/components"], function (bc, Vue, context, accident) {
  "use strict";
  let resourceKey = "accident-draft";
  let resourceName = "事故报案";

  // 表格头
  let columns = [
    {
      id: "draftStatus", label: "状态", width: "4em",
      filter: function (value) {
        return accident.DRAFT_STATUS_MAP[value] || "";
      }
    },
    {id: "happenTime", label: "事发时间", width: "10em"},
    {id: "motorcadeName", label: "事发车队", width: "5em"},
    {id: "carPlate", label: "事故车号", width: "7em", rowCellClass: "monospace"},
    {id: "driverName", label: "当事司机", width: "5em"},
    {id: "location", label: "事发地点", width: "13em"},
    {id: "hitForm", label: "事故形态", width: "7em"},
    {id: "hitType", label: "碰撞类型", width: "5em"},
    {id: "authorName", label: "接案人", width: "5em"},
    {id: "draftTime", label: "报案时间", width: "10em"},
    {
      id: "overdueDraft", label: "逾期报案", width: "5em",
      filter: function (value) {
        return value ? "是" : "";
      }
    },
    {id: "source", label: "报案来源", width: "5em"},
    {id: "code", label: "事故编号", width: "7.5em"}
  ];

  return function Page($page) {
    new Vue({
      el: $page[0],
      data: {
        url: `${accident.dataServer}/${resourceKey}`,
        columns: columns,
        status: 'Drafting',
        statuses: accident.DRAFT_STATUSES.filter(status => status.id !== "ToSubmit"), // 临时隐藏待上报状态
        fuzzySearch: '',
        isSubmitter: context.is("ACCIDENT_DRAFT_SUBMIT"),
        isEditor: context.is("ACCIDENT_DRAFT_MODIFY")
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
              mid: `${resourceKey}-${sel[0].id}`,
              name: `${resourceName} ${sel[0].code}`,
              data: sel[0].id,
              afterClose: (status) => {
                if (status) this.reload();
              }
            });
          } else if (sel.length === 0) bc.msg.slide("请先选择！");
          else if (sel.length > 1) bc.msg.slide("只能选择一条！");
        },
        dblclickRow: function (row) {
          accident.open(resourceKey, {
            mid: `${resourceKey}-${row.id}`,
            name: `${resourceName} ${row.code}`,
            data: row.id,
            afterClose: (status) => {
              if (status) this.reload();
            }
          });
        }
      }
    });
  };
});
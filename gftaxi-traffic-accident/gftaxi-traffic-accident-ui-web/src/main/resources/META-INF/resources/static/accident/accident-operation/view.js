define(["bc", "vue", "context", "static/accident/api", "static/accident/simter-file/api", "bc/vue/components"]
  , function (bc, Vue, context, accident, file) {
    "use strict";

    return function Page($page) {
      new Vue({
        el: $page[0],
        data: {
          operations: [],
          drawers: [],
          sortInitStatus: "desc",
          sortStatuses: [{id: "esc", label: "正序"}, {id: "desc", label: "逆序"}],
          notEmptyNum: null,
          spreadNum: null,
          spreadStatuses: [{id: null, label: "展开"}, {id: 0, label: "折叠"}],
          preGroup: null,
        },
        methods: {
          isEmpty(operation) {
            return !operation.result && !operation.comment && !operation.attachments && !operation.fields
          },
          changeSpreadStatus(spreadNum) {
            if (this.notEmptyNum === spreadNum) {
              this.drawers.forEach(drawer => {
                drawer.isShow = true
              })
              this.spreadNum = this.notEmptyNum
            } else if (0 === spreadNum) {
              this.drawers.forEach(drawer => {
                drawer.isShow = false
              })
              this.spreadNum = 0
            }
          },
          changeSort() {
            this.operations.reverse()
            this.drawers.reverse()
          },
          changeSpread(drawer) {
            drawer.isShow = !drawer.isShow
            if (drawer.isShow) {
              this.spreadNum++
            } else {
              this.spreadNum--
            }
          },
          inlineById(id, event) {
            file.inlineById(id)
            event.preventDefault()
          }
        },
        ready: function () {
          const data = $page.data("data")
          // 设置标题
          $page.prev()[0].firstChild.textContent = data.title
          accident.getByModule("cluster", data.cluster).then(operations => {
            this.drawers = operations.map(operation => ({isShow: true, isEmpty: this.isEmpty(operation)}))
            operations.forEach(data.operationHandle)
            this.operations = operations
            this.notEmptyNum = this.spreadNum = this.spreadStatuses[0].id = this.drawers.filter(it => {
              return !it.isEmpty
            }).length
          })
        }
      })
    }
  }
)
/*!
* 事故附件组件 accident-attachment v0.1.0
* https://gitee.com/gftaxi/gftaxi-traffic-accident-vue/tree/master/accident-attachment
* @author RJ.Hwang <rongjihuang@gmail.com>
* @license gftaxi (Only can be used in gftaxi company)
*/
define(['exports', 'simter-vue-table'], function (exports, stTable) { 'use strict';

  var stTable__default = 'default' in stTable ? stTable['default'] : stTable;

  var testAttachData = [
    {
      id: 10,
      name: "现场照片",
      type: ":d",
      children: [
        {
          id: 11,
          name: "现场照片1",
          type: "png",
          size: 100,
          modifier: "黄小明",
          modifyOn: "2018-01-01 12:30"
        },
        {
          id: 12,
          name: "现场照片2",
          type: "jpg",
          size: 10.2345 * 1024,
          modifier: "黄小张",
          modifyOn: "2018-01-01 12:35"
        }
      ]
    },
    {
      id: 20,
      name: "维修类",
      type: ":d",
      children: [
        {
          id: 21,
          name: "维修类照片1",
          type: "png",
          size: 100 * 1024 * 1024,
          modifier: "黄小明",
          modifyOn: "2018-01-01 12:30"
        },
        {
          id: 22,
          name: "维修类照片2",
          type: "jpg",
          size: 100 * 1024 * 1024 * 1.45,
          modifier: "黄小张",
          modifyOn: "2018-01-01 12:40"
        },
        {
          id: 23,
          name: "维修记录",
          type: "docx",
          size: 654.321 * 1024 * 1024,
          modifier: "黄小张",
          modifyOn: "2018-01-01 12:50"
        }
      ]
    }
  ];

  /*
  Copyright (c) 2013, Yahoo! Inc. All rights reserved.
  Code licensed under the BSD License:
  http://yuilibrary.com/license/
  */

  const sizes = [
      'Bytes', 'kB', 'MB', 'GB', 'TB', 'PB', 'EB'
  ];

  /**
  Pretty print a size from bytes
  @method pretty
  @param {Number} size The number to pretty print
  @param {Boolean} [nospace=false] Don't print a space
  @param {Boolean} [one=false] Only print one character
  @param {Number} [places=1] Number of decimal places to return
  */

  var prettysize = (size, nospace, one, places) => {
      if (typeof nospace === 'object') {
          const opts = nospace;
          nospace = opts.nospace;
          one = opts.one;
          places = opts.places || 1;
      } else {
          places = places || 1;
      }

      let mysize;

      sizes.forEach((unit, id) => {
          if (one) {
              unit = unit.slice(0, 1);
          }
          const s = Math.pow(1024, id);
          let fixed;
          if (size >= s) {
              fixed = String((size / s).toFixed(places));
              if (fixed.indexOf('.0') === fixed.length - 2) {
                  fixed = fixed.slice(0, -2);
              }
              mysize = fixed + (nospace ? '' : ' ') + unit;
          }
      });

      // zero handling
      // always prints in Bytes
      if (!mysize) {
          let unit = (one ? sizes[0].slice(0, 1) : sizes[0]);
          mysize = '0' + (nospace ? '' : ' ') + unit;
      }

      return mysize;
  };

  /**
   * 根据权限生成事故附件组件相应的列配置
   */
  function columns (readonly) {
    return [
      {
        id: "name",
        label: "名称",
        width: "30em",
        cell: readonly
          ? {
            component: "st-cell-text"
          }
          : {
            mutate: true, 
            component: "st-cell-text-editor",
            classes: { text: "ui-widget-content" }
          }
      },
      { id: "type", label: "类型", width: "3em", class: "ext", cell: { component: "st-cell-text" } },
      {
        id: "size",
        label: "大小",
        width: "6em",
        class: "align-right",
        cell: {
          component: "st-cell-text",
          render: function (value, row) {
            return prettysize(value);
          }
        }
      },
      {
        id: "modifyOn",
        label: "更新时间",
        width: "13em",
        cell: {
          component: "st-cell-text",
          render: function (value, row) {
            return `${value} ${row.modifier}`;
          }
        }
      }
    ]
  }

  //

  /** 默认的事故附件分类 */
  const ACCIDENT_DEFAULT_CATEGORIES = [
    { name: "现场照片", path: "XCZP" },
    { name: "维修类", path: "WXL" },
    { name: "教育类", path: "JYL" },
    { name: "理赔类", path: "LPL" }
  ];

  var script = {
    props: {
      serverUrl: { type: String, required: true },
      caseId: { type: Number, required: true },
      caseName: { type: String, required: false, default: "事故附件" },
      readonly: { type: Boolean, required: false, default: true },
      defaultCategories: {
        type: Array,
        required: false,
        default() {
          return ACCIDENT_DEFAULT_CATEGORIES;
        }
      }
    },
    data() {
      return {
        tableClasses: {
          table: "ui-widget-content",
          headerRow: "st-header-row",
          headerCell: "st-header-cell"
        },
        rows: []
      };
    },
    computed: {
      columns() {
        return columns(this.readonly);
      },
      group() {
        return {
          id: "name",
          predicate: row => row.type === ":d",
          colspan: 1,
          cell: this.readonly
            ? { component: "st-cell-text" }
            : {
                mutate: true,
                component: "st-cell-text-editor",
                classes: { text: "ui-widget-content" }
              }
        };
      },
      picker() {
        return { label: "序号", width: "4.5em", component: "st-cell-row-picker" };
      }
    },
    components: {
      "st-table": stTable__default
    },
    created() {
      this.loadAttachData();
    },
    methods: {
      // 获取事故的附件列表
      loadAttachData() {
        // 服务器返回的数据结构是 ：（GET file/accident-{case-id}/descendent）
        // [{CHILD_DATA}, ...]
        // {CHILD_DATA}={id, name, path, type, size, modifyOn, modifier, children: [{CHILD_DATA}, ...]}
        const url = `${this.serverUrl}/accident-${this.caseId}/descendent`;
        console.log("TODO : load data from url=%s", url);
        this.rows = testAttachData; // TODO : 从服务器下载
      },
      // 新建分类
      addNewCategory() {
        console.log("TODO add by server");
        this.rows.push({ name: "新建分类", type: ":d", children: [] });
      },
      // 下载附件
      downloadAttach() {
        if (this.$refs.table.selection.length === 0) {
          alert("请先选择要要下载的附件！");
        } else {
          console.log("TODO downloadAttach");
        }
      },
      // 在线查看附件
      inlineAttach() {
        if (this.$refs.table.selection.length === 0) {
          alert("请先选择要查看的附件！");
        } else {
          console.log("TODO inlineAttach");
        }
      },
      // 删除附件
      deleteAttach() {
        if (this.$refs.table.selection.length === 0) {
          alert("请先选择要删除的附件！");
        } else {
          console.log("TODO delete to server");
          this.$refs.table.deleteSelection();
        }
      },
      // 添加财产案常用附件
      addEstateAttach() {
        console.log("TODO addEstateAttach");
      },
      // 添加人伤案常用附件
      addHurtAttach() {
        console.log("TODO addHurtAttach");
      },
      // 单元格值变动事件
      cellChangeEvent($event) {
        console.log("cellChangeEvent: $event=%o", $event);
      }
    }
  };

  /* script */
              const __vue_script__ = script;
              
  /* template */
  var __vue_render__ = function() {
    var _vm = this;
    var _h = _vm.$createElement;
    var _c = _vm._self._c || _h;
    return _c(
      "div",
      { staticClass: "accident-attachment" },
      [
        _c("div", { staticClass: "toolbar" }, [
          !_vm.readonly
            ? _c(
                "a",
                {
                  staticClass: "addNewCategory",
                  attrs: { href: "#" },
                  on: {
                    click: function($event) {
                      $event.stopPropagation();
                      $event.preventDefault();
                      return _vm.addNewCategory($event)
                    }
                  }
                },
                [_vm._v("新建分类")]
              )
            : _vm._e(),
          _vm._v(" "),
          _c(
            "a",
            {
              staticClass: "downloadAttach",
              attrs: { href: "#" },
              on: {
                click: function($event) {
                  $event.stopPropagation();
                  $event.preventDefault();
                  return _vm.downloadAttach($event)
                }
              }
            },
            [_vm._v("下载")]
          ),
          _vm._v(" "),
          _c(
            "a",
            {
              staticClass: "inlineAttach",
              attrs: { href: "#" },
              on: {
                click: function($event) {
                  $event.stopPropagation();
                  $event.preventDefault();
                  return _vm.inlineAttach($event)
                }
              }
            },
            [_vm._v("查看")]
          ),
          _vm._v(" "),
          !_vm.readonly
            ? _c(
                "a",
                {
                  staticClass: "deleteAttach",
                  attrs: { href: "#" },
                  on: {
                    click: function($event) {
                      $event.stopPropagation();
                      $event.preventDefault();
                      return _vm.deleteAttach($event)
                    }
                  }
                },
                [_vm._v("删除")]
              )
            : _vm._e(),
          _vm._v(" "),
          !_vm.readonly
            ? _c(
                "a",
                {
                  staticClass: "addEstateAttach",
                  attrs: { href: "#" },
                  on: {
                    click: function($event) {
                      $event.stopPropagation();
                      $event.preventDefault();
                      return _vm.addEstateAttach($event)
                    }
                  }
                },
                [_vm._v("添加财产案常用附件")]
              )
            : _vm._e(),
          _vm._v(" "),
          !_vm.readonly
            ? _c(
                "a",
                {
                  staticClass: "addHurtAttach",
                  attrs: { href: "#" },
                  on: {
                    click: function($event) {
                      $event.stopPropagation();
                      $event.preventDefault();
                      return _vm.addHurtAttach($event)
                    }
                  }
                },
                [_vm._v("添加人伤案常用附件")]
              )
            : _vm._e()
        ]),
        _vm._v(" "),
        _c("st-table", {
          ref: "table",
          attrs: {
            id: "id",
            classes: _vm.tableClasses,
            columns: _vm.columns,
            group: _vm.group,
            rows: _vm.rows,
            picker: _vm.picker
          },
          on: { "cell-change": _vm.cellChangeEvent }
        })
      ],
      1
    )
  };
  var __vue_staticRenderFns__ = [];
  __vue_render__._withStripped = true;

    /* style */
    const __vue_inject_styles__ = function (inject) {
      if (!inject) return
      inject("data-v-dfe3e80a_0", { source: "\n.accident-attachment .toolbar > a {\r\n  display: inline-block;\r\n  color: inherit;\r\n  margin: 0.25em 0 0.25em 0.25em;\n}\r\n\r\n/** table 列头字体不加粗 */\n.accident-attachment .st-table > thead > tr > th {\r\n  font-weight: normal;\n}\r\n\r\n/** 隐藏 table 的上、下、左边框 */\n.accident-attachment .st-table,\r\n.accident-attachment .st-table > tbody > tr > td:first-child {\r\n  border-left-width: 0;\n}\n.accident-attachment .st-table,\r\n.accident-attachment .st-table > tbody > tr:last-child,\r\n.accident-attachment .st-table > tbody > tr:last-child > td {\r\n  border-bottom-width: 0;\n}\r\n", map: {"version":3,"sources":["D:\\work\\gitee-gftaxi\\gftaxi-traffic-accident\\gftaxi-traffic-accident-vue\\accident-attachment/D:\\work\\gitee-gftaxi\\gftaxi-traffic-accident\\gftaxi-traffic-accident-vue\\accident-attachment\\src\\attachment.vue"],"names":[],"mappings":";AAuJA;EACA,sBAAA;EACA,eAAA;EACA,+BAAA;CACA;;AAEA,oBAAA;AACA;EACA,oBAAA;CACA;;AAEA,wBAAA;AACA;;EAEA,qBAAA;CACA;AACA;;;EAGA,uBAAA;CACA","file":"attachment.vue","sourcesContent":["<template>\r\n<div class=\"accident-attachment\">\r\n  <div class=\"toolbar\">\r\n    <a v-if=\"!readonly\" href=\"#\" class=\"addNewCategory\" @click.stop.prevent=\"addNewCategory\">新建分类</a>\r\n    <a href=\"#\" class=\"downloadAttach\" @click.stop.prevent=\"downloadAttach\">下载</a>\r\n    <a href=\"#\" class=\"inlineAttach\" @click.stop.prevent=\"inlineAttach\">查看</a>\r\n    <a v-if=\"!readonly\" href=\"#\" class=\"deleteAttach\" @click.stop.prevent=\"deleteAttach\">删除</a>\r\n    <a v-if=\"!readonly\" href=\"#\" class=\"addEstateAttach\" @click.stop.prevent=\"addEstateAttach\">添加财产案常用附件</a>\r\n    <a v-if=\"!readonly\" href=\"#\" class=\"addHurtAttach\" @click.stop.prevent=\"addHurtAttach\">添加人伤案常用附件</a>\r\n  </div>\r\n  <st-table ref=\"table\" \r\n    id=\"id\"\r\n    :classes=\"tableClasses\" \r\n    :columns=\"columns\" \r\n    :group=\"group\"\r\n    :rows=\"rows\"\r\n    :picker=\"picker\"\r\n    @cell-change=\"cellChangeEvent\">\r\n  </st-table>\r\n</div>\r\n</template>\r\n\r\n<script>\r\nimport { default as stTable, cellBase } from \"simter-vue-table\";\r\nimport testAttachData from \"./test-attach-data\"; // TODO : delete me\r\nimport columns from \"./columns\";\r\n\r\n/** 顶层事故文件夹的配置 */\r\nconst ACCIDENT_FOLDER = {\r\n  id: \"accident\",\r\n  name: \"accident\",\r\n  path: \"accident\"\r\n};\r\n\r\n/** 默认的事故附件分类 */\r\nconst ACCIDENT_DEFAULT_CATEGORIES = [\r\n  { name: \"现场照片\", path: \"XCZP\" },\r\n  { name: \"维修类\", path: \"WXL\" },\r\n  { name: \"教育类\", path: \"JYL\" },\r\n  { name: \"理赔类\", path: \"LPL\" }\r\n];\r\n\r\nexport default {\r\n  props: {\r\n    serverUrl: { type: String, required: true },\r\n    caseId: { type: Number, required: true },\r\n    caseName: { type: String, required: false, default: \"事故附件\" },\r\n    readonly: { type: Boolean, required: false, default: true },\r\n    defaultCategories: {\r\n      type: Array,\r\n      required: false,\r\n      default() {\r\n        return ACCIDENT_DEFAULT_CATEGORIES;\r\n      }\r\n    }\r\n  },\r\n  data() {\r\n    return {\r\n      tableClasses: {\r\n        table: \"ui-widget-content\",\r\n        headerRow: \"st-header-row\",\r\n        headerCell: \"st-header-cell\"\r\n      },\r\n      rows: []\r\n    };\r\n  },\r\n  computed: {\r\n    columns() {\r\n      return columns(this.readonly);\r\n    },\r\n    group() {\r\n      return {\r\n        id: \"name\",\r\n        predicate: row => row.type === \":d\",\r\n        colspan: 1,\r\n        cell: this.readonly\r\n          ? { component: \"st-cell-text\" }\r\n          : {\r\n              mutate: true,\r\n              component: \"st-cell-text-editor\",\r\n              classes: { text: \"ui-widget-content\" }\r\n            }\r\n      };\r\n    },\r\n    picker() {\r\n      return { label: \"序号\", width: \"4.5em\", component: \"st-cell-row-picker\" };\r\n    }\r\n  },\r\n  components: {\r\n    \"st-table\": stTable\r\n  },\r\n  created() {\r\n    this.loadAttachData();\r\n  },\r\n  methods: {\r\n    // 获取事故的附件列表\r\n    loadAttachData() {\r\n      // 服务器返回的数据结构是 ：（GET file/accident-{case-id}/descendent）\r\n      // [{CHILD_DATA}, ...]\r\n      // {CHILD_DATA}={id, name, path, type, size, modifyOn, modifier, children: [{CHILD_DATA}, ...]}\r\n      const url = `${this.serverUrl}/accident-${this.caseId}/descendent`;\r\n      console.log(\"TODO : load data from url=%s\", url);\r\n      this.rows = testAttachData; // TODO : 从服务器下载\r\n    },\r\n    // 新建分类\r\n    addNewCategory() {\r\n      console.log(\"TODO add by server\");\r\n      this.rows.push({ name: \"新建分类\", type: \":d\", children: [] });\r\n    },\r\n    // 下载附件\r\n    downloadAttach() {\r\n      if (this.$refs.table.selection.length === 0) {\r\n        alert(\"请先选择要要下载的附件！\");\r\n      } else {\r\n        console.log(\"TODO downloadAttach\");\r\n      }\r\n    },\r\n    // 在线查看附件\r\n    inlineAttach() {\r\n      if (this.$refs.table.selection.length === 0) {\r\n        alert(\"请先选择要查看的附件！\");\r\n      } else {\r\n        console.log(\"TODO inlineAttach\");\r\n      }\r\n    },\r\n    // 删除附件\r\n    deleteAttach() {\r\n      if (this.$refs.table.selection.length === 0) {\r\n        alert(\"请先选择要删除的附件！\");\r\n      } else {\r\n        console.log(\"TODO delete to server\");\r\n        this.$refs.table.deleteSelection();\r\n      }\r\n    },\r\n    // 添加财产案常用附件\r\n    addEstateAttach() {\r\n      console.log(\"TODO addEstateAttach\");\r\n    },\r\n    // 添加人伤案常用附件\r\n    addHurtAttach() {\r\n      console.log(\"TODO addHurtAttach\");\r\n    },\r\n    // 单元格值变动事件\r\n    cellChangeEvent($event) {\r\n      console.log(\"cellChangeEvent: $event=%o\", $event);\r\n    }\r\n  }\r\n};\r\n</script>\r\n\r\n<style>\r\n.accident-attachment .toolbar > a {\r\n  display: inline-block;\r\n  color: inherit;\r\n  margin: 0.25em 0 0.25em 0.25em;\r\n}\r\n\r\n/** table 列头字体不加粗 */\r\n.accident-attachment .st-table > thead > tr > th {\r\n  font-weight: normal;\r\n}\r\n\r\n/** 隐藏 table 的上、下、左边框 */\r\n.accident-attachment .st-table,\r\n.accident-attachment .st-table > tbody > tr > td:first-child {\r\n  border-left-width: 0;\r\n}\r\n.accident-attachment .st-table,\r\n.accident-attachment .st-table > tbody > tr:last-child,\r\n.accident-attachment .st-table > tbody > tr:last-child > td {\r\n  border-bottom-width: 0;\r\n}\r\n</style>"]}, media: undefined });

    };
    /* scoped */
    const __vue_scope_id__ = undefined;
    /* module identifier */
    const __vue_module_identifier__ = undefined;
    /* functional template */
    const __vue_is_functional_template__ = false;
    /* component normalizer */
    function __vue_normalize__(
      template, style, script$$1,
      scope, functional, moduleIdentifier,
      createInjector, createInjectorSSR
    ) {
      const component = (typeof script$$1 === 'function' ? script$$1.options : script$$1) || {};

      // For security concerns, we use only base name in production mode.
      component.__file = "D:\\work\\gitee-gftaxi\\gftaxi-traffic-accident\\gftaxi-traffic-accident-vue\\accident-attachment\\src\\attachment.vue";

      if (!component.render) {
        component.render = template.render;
        component.staticRenderFns = template.staticRenderFns;
        component._compiled = true;

        if (functional) component.functional = true;
      }

      component._scopeId = scope;

      {
        let hook;
        if (style) {
          hook = function(context) {
            style.call(this, createInjector(context));
          };
        }

        if (hook !== undefined) {
          if (component.functional) {
            // register for functional component in vue file
            const originalRender = component.render;
            component.render = function renderWithStyleInjection(h, context) {
              hook.call(context);
              return originalRender(h, context)
            };
          } else {
            // inject component registration as beforeCreate hook
            const existing = component.beforeCreate;
            component.beforeCreate = existing ? [].concat(existing, hook) : [hook];
          }
        }
      }

      return component
    }
    /* style inject */
    function __vue_create_injector__() {
      const head = document.head || document.getElementsByTagName('head')[0];
      const styles = __vue_create_injector__.styles || (__vue_create_injector__.styles = {});
      const isOldIE =
        typeof navigator !== 'undefined' &&
        /msie [6-9]\\b/.test(navigator.userAgent.toLowerCase());

      return function addStyle(id, css) {
        if (document.querySelector('style[data-vue-ssr-id~="' + id + '"]')) return // SSR styles are present.

        const group = isOldIE ? css.media || 'default' : id;
        const style = styles[group] || (styles[group] = { ids: [], parts: [], element: undefined });

        if (!style.ids.includes(id)) {
          let code = css.source;
          let index = style.ids.length;

          style.ids.push(id);

          if (isOldIE) {
            style.element = style.element || document.querySelector('style[data-group=' + group + ']');
          }

          if (!style.element) {
            const el = style.element = document.createElement('style');
            el.type = 'text/css';

            if (css.media) el.setAttribute('media', css.media);
            if (isOldIE) {
              el.setAttribute('data-group', group);
              el.setAttribute('data-next-index', '0');
            }

            head.appendChild(el);
          }

          if (isOldIE) {
            index = parseInt(style.element.getAttribute('data-next-index'));
            style.element.setAttribute('data-next-index', index + 1);
          }

          if (style.element.styleSheet) {
            style.parts.push(code);
            style.element.styleSheet.cssText = style.parts
              .filter(Boolean)
              .join('\n');
          } else {
            const textNode = document.createTextNode(code);
            const nodes = style.element.childNodes;
            if (nodes[index]) style.element.removeChild(nodes[index]);
            if (nodes.length) style.element.insertBefore(textNode, nodes[index]);
            else style.element.appendChild(textNode);
          }
        }
      }
    }
    /* style inject SSR */
    

    
    var attachment = __vue_normalize__(
      { render: __vue_render__, staticRenderFns: __vue_staticRenderFns__ },
      __vue_inject_styles__,
      __vue_script__,
      __vue_scope_id__,
      __vue_is_functional_template__,
      __vue_module_identifier__,
      __vue_create_injector__,
      undefined
    );

  exports.default = attachment;

  Object.defineProperty(exports, '__esModule', { value: true });

});

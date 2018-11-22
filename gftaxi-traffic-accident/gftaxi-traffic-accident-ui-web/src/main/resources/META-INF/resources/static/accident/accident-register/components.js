define(["accident-attachment"], function (accidentAttachment) {
  "use strict";
  const attachment = 'default' in accidentAttachment ? accidentAttachment['default'] : accidentAttachment;
  return {
    // 附件组件
    accidentAttachment: attachment,
    // 当事车辆表格列宽模板
    columnsCars: {
      template: `
        <colgroup>
          <col style="width: 3em">
          <col style="width: 4em">
          <col style="width: 7em">
          <col style="width: 7.9em">
          <col style="width: 6em">
          <col style="width: 6em">
          <col style="width: 6em">
          <col style="width: 7em">
          <col style="width: 7em">
          <col style="width: 7em">
          <col style="width: 5em">
          <col style="width: auto">
        </colgroup>`
    },
    // 当事人表格列宽模板
    columnsPeoples: {
      template: `
        <colgroup>
          <col style="width: 3em">
          <col style="width: 4em">
          <col style="width: 7em">
          <col style="width: 3em">
          <col style="width: 7em">
          <col style="width: 7em">
          <col style="width: 6em">
          <col style="width: 3em">
          <col style="width: 7em">
          <col style="width: 7em">
          <col style="width: 7em">
          <col style="width: 5em">
          <col style="width: auto">
        </colgroup>`
    },
    // 其他物品表格列宽模板
    columnsOthers: {
      template: `
        <colgroup>
          <col style="width: 3em">
          <col style="width: 4em">
          <col style="width: 15em">
          <col style="width: 9em">
          <col style="width: 7em">
          <col style="width: 7em">
          <col style="width: 8.9em">
          <col style="width: 7em">
          <col style="width: 5em">
          <col style="width: auto">
        </colgroup>`
    }
  };
});
define(["jquery", "vue"], function ($, Vue) {
  Vue.component('bt-widget', {
    props: ["type", "name", "label"],
    template: '#bt-widget'
  });

  Vue.component('bt-form', {
    props: ["form"],
    template: '#btForm'
  });

  function getSchema(profileData) {
    var fieldsInfo = profileData["alps"]["descriptors"][0]['descriptors'];
    var columnMeta = [];
    for (var k in fieldsInfo) {
      var fieldInfo = fieldsInfo[k];
      var column = {
        field: fieldInfo.name,
      };
      if (fieldInfo['doc'] == undefined) {
        column['title'] = fieldInfo.name;
      } else {
        column['title'] = fieldInfo.doc.value;
      }
      columnMeta.push(column);
    }
    return columnMeta;
  }

  function init(profile) {
    var modelSchema = getSchema(profile);
    var modelForm = new Vue({
      el: '#modelForm',
      data: {
        form: {
          title: "asdf",
          widgets: [
            {
              id: 1,
              label: 'name label',
              name: "twea",
              type: "input",
              help: "asdf"
            },
            {
              id: 2,
              label: '1name label',
              name: "twea",
              type: "input"
            },
            {
              id: 3,
              label: 'Submit',
              type: "btn"
            }
          ]
        }
      },
    });
  }

  return {
    init: init
  }
});
requirejs.config({
  "paths": {
    "bootstrap-table-editor": [
      "/webjars/bootstrap-table/dist/extensions/editable/bootstrap-table-editable"
    ]
  },
  "shim": {
    "bootstrap-table": [
      "jquery"
    ],
    "bootstrap-table-editor": [
      "bootstrap-table",
      "bootstrap-editable"
    ]
  }
});




require(["main"], function () {


});
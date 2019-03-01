define(["jquery", "axios", "bootstrap-table-editor"],
    function ($, axios) {
      var REST_API_BASE = window.APP_CONFIG['rest.api.base'];
      loadStyles("/webjars/bootstrap-table/dist/bootstrap-table.css");
      loadStyles("/webjars/x-editable-bootstrap3/css/bootstrap-editable.css");
      var selections = [];

      var NOT_EDITABLE_FIELD = ['id', 'createTime', 'updateTime'];

      function loadStyles(url) {
        var link = document.createElement("link");
        link.type = "text/css";
        link.rel = "stylesheet";
        link.href = url;
        document.getElementsByTagName("head")[0].appendChild(link);
      }

      function getIdSelections($table) {
        return $.map($table.bootstrapTable('getSelections'), function (row) {
          return row.id
        });
      }

      function operateFormatter(value, row, index) {
        return [
          '<a class="edit" href="javascript:void(0)" title="Edit">',
          '<i class="glyphicon glyphicon-edit"></i>',
          '</a>  ',
          '<a class="remove" href="javascript:void(0)" title="Remove">',
          '<i class="glyphicon glyphicon-remove"></i>',
          '</a>'
        ].join('');
      }

      function getOperate($table, baseApi) {
        return {
          'click .edit': function (e, value, row, index) {
            alert('You click edit action, row: ' + JSON.stringify(row));
          },
          'click .remove': function (e, value, row, index) {
            axios.delete(baseApi + "/" + row.id).then(function () {
              $table.bootstrapTable('refresh');
            });
          }
        };
      }

      function getTableSchema($table, baseApi, profileData, callback) {
        var fieldsInfo = profileData["alps"]["descriptors"][0]['descriptors'];
        var columnMeta = [];
        columnMeta.push({
          field: 'state',
          checkbox: true,
          align: 'center',
          valign: 'middle'
        });
        for (var k in fieldsInfo) {
          var fieldInfo = fieldsInfo[k];
          var column = {
            field: fieldInfo.name,
            sortable: true
          };
          if (fieldInfo['doc'] == undefined) {
            column['title'] = fieldInfo.name;
          } else {
            column['title'] = fieldInfo.doc.value;
          }

          if ($.inArray(fieldInfo.name, NOT_EDITABLE_FIELD) == -1) {
            column['editable'] = true;
          }
          columnMeta.push(column);
        }
        columnMeta.push({
          field: 'operate',
          title: 'Item Operate',
          align: 'center',
          events: getOperate($table, baseApi),
          formatter: operateFormatter
        });

        callback(columnMeta);
      }

      function init(tableId, baseApi, apiUrl, profileData) {
        var $table = $('#' + tableId);
        var restApi = REST_API_BASE + baseApi;
        var searchApi = REST_API_BASE + apiUrl;

        getTableSchema($table, restApi, profileData,
            function (columnMeta) {
              initTable(tableId, restApi, columnMeta, searchApi);
            });
      }

      function detailFormater(index, row, element) {
        var html = [];
        $.each(row, function (key, value) {
          if (key != 'state') {
            html.push('<p><b>' + key + ':</b> ' + value + '</p>');
          }
        });
        return html.join('');
      }

      function responseHandler(res) {
        var cres = {};
        var embed = res._embedded;
        cres["rows"] = embed[Object.keys(embed)[0]];
        cres["total"] = res.page.totalElements;
        $.each(cres["rows"], function (i, row) {
          delete row['_links'];
          row.state = $.inArray(row.id, selections) !== -1;
        });
        return cres;
      }

      // transform query params
      function queryParams(params) {
        //{?page,size,sort}
        // transform page params
        params['size'] = params['limit'];
        delete params['limit'];
        params['page'] = parseInt(params['offset'] / params['size']);
        delete params['offset'];

        // transfrom sort params
        var sort = params['sort'];
        if (sort != undefined) {
          var order = params['order'];
          params['sort'] = sort + "," + order;
          delete params['order'];
        }

        var search = params['search'];
        if (search != undefined && search.length > 0) {
          delete params['search'];
          params['name'] = search;
        } else {
          params['name'] = '';
        }

        return params;
      }

      function initTable(tableId, baseApi, columnMeta, api) {
        var $table = $('#' + tableId);
        var removeId = tableId + '-remove';
        var addId = tableId + '-add';
        var toolbarId = tableId + '-toolbar';
        $table.before('      <div id="' + toolbarId + '">\n'
            + '        <button id="' + removeId
            + '" class="btn btn-danger" disabled>\n'
            + '          <i class="glyphicon glyphicon-remove"></i> Delete\n'
            + '        </button>\n'
            + '        <button id="' + addId
            + '" class="btn btn-success">\n'
            + '          <i class="glyphicon glyphicon-plus"></i> Add\n'
            + '        </button>\n'
            + '      </div>');
        var $remove = $('#' + removeId);
        $table.bootstrapTable({
              columns: columnMeta,
              idField: "id",
              toolbar: "#" + toolbarId,
              sortable: true,
              search: true,
              showToggle: true,
              showColumns: true,
              pagination: true,
              detailView: true,
              showRefresh: true,
              sidePagination: "server",
              detailFormatter: detailFormater,
              striped: true,//逐行变色
              icons: {
                paginationSwitchDown: 'glyphicon-collapse-down icon-chevron-down',
                paginationSwitchUp: 'glyphicon-collapse-up icon-chevron-up',
                refresh: 'glyphicon-refresh icon-refresh',
                toggle: 'glyphicon-list-alt icon-list-alt',
                columns: 'glyphicon-th icon-th',
                detailOpen: 'glyphicon-plus icon-plus',
                detailClose: 'glyphicon-minus icon-minus'
              },
              queryParams: queryParams,
              responseHandler: responseHandler,
              url: api,
              onPostBody: function () {
                $.each($('#' + tableId).find("a[data-pk]"), function () {
                  var $el = $(this);
                  var pk = $el.attr("data-pk");
                  $el.editable({
                    type: 'text',
                    url: baseApi + "/" + pk,
                    params: function (params) {
                      console.log(params);
                      var obj = {};
                      obj[params['name']] = params['value'];
                      return JSON.stringify(obj);
                    },
                    ajaxOptions: {
                      contentType: 'application/json;charset=UTF-8',
                      type: 'PATCH',
                      dataType: 'json'
                    },
                    success: function () {
                      $table.bootstrapTable('refresh');
                    }
                  })
                });
              }
            }
        );
        $table.on("onPostBody")

        $table.on('check.bs.table uncheck.bs.table ' +
            'check-all.bs.table uncheck-all.bs.table', function () {
          $remove.prop('disabled',
              !$table.bootstrapTable('getSelections').length);
          selections = getIdSelections($table);
        });

        $remove.on('click', function () {
          var ajaxList = [];
          for (var k in selections) {
            var sid = selections[k];
            ajaxList.push(axios.delete(baseApi + "/" + sid));
          }

          axios.all(ajaxList).then(axios.spread(function () {
            $table.bootstrapTable('refresh');
            $remove.prop('disabled', true);
          }));
        });
      }

      return {
        init: init
      };
    });
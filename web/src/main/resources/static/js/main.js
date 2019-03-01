define(
    ["axios", "components/table", "components/btform"],
    function (axios, table, btform) {
      var REST_API_BASE = window.APP_CONFIG['rest.api.base'];
      axios.get(REST_API_BASE + "/profile/temp").then(
          function (response) {
            table.init(
                'table',
                "/temp",
                "/temp/search/name",
                response.data
            );
            // form init
            btform.init(response.data);
          }
      );
    }
);

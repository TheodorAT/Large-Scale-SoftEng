document.addEventListener("DOMContentLoaded", function (event) {
  baseLab.init(
    {
      h2Gui: "Admin",
      sqlSelect: 2,
      sqlQuery: 4,
      sqlBase: true,
      sqlSchema: true,
      restDefinition: 2,
      restVerb: 2,
      jerseyResource: 3,
      jerseyPath: "/rest/foo/all",
      jerseyExample: 3,
      jerseyPathParam: 1,
      e2eBackEnd: true,
    },
    "lab1.html",
  );
});

const h2GuiValidate = function (submitEvent) {
  submitEvent.preventDefault;
  const value = document.querySelector("#h2Gui input").value;
  baseLab.complete("h2Gui", value);
  return false;
};

const sqlBaseValidate = function (submitEvent) {
  submitEvent.preventDefault();
  const checked = Array.from(document.querySelectorAll("#sqlBase input")).map((i) => i.checked);
  baseLab.complete("sqlBase", checked[0] && !checked[1] && checked[2] && checked[3]);
  return false;
};

const sqlSchemaValidate = function (submitEvent) {
  submitEvent.preventDefault();
  const checked = Array.from(document.querySelectorAll("#sqlSchema input")).map((i) => i.checked);
  baseLab.complete("sqlSchema", checked[0] && !checked[1] && checked[2] && !checked[3]);
  return false;
};

const jerseyPathValidate = function (submitEvent) {
  submitEvent.preventDefault();
  let value = document.querySelector("#jerseyPath input").value;
  value = value.replace(/^\//, "").replace(/\/$/, "");
  const parser = document.createElement("a");
  parser.href = "/" + value;
  baseLab.complete("jerseyPath", parser.pathname);
  return false;
};

const e2eBackEndValidate = function () {
  let theFoo = -1;
  base.rest
    .addFoo({ payload: "test" })
    .then(function (foo) {
      if (foo.error) throw "Failed to add foo: " + foo.message;
      theFoo = foo.id;
      return fetch("/rest/foo/" + foo.id + "/total/" + 5, {
        credentials: "same-origin",
        method: "POST",
      });
    })
    .then(function (result) {
      if (!result.ok) throw "Failed to POST new foo: " + result.status;
      else return result;
    })
    .then(function () {
      return fetch("/rest/foo", { credentials: "same-origin" })
        .then((response) => response.json())
        .then(function (foos) {
          if (foos.error) throw "Failed to fetch foos: " + foos.message;
          const match = foos.filter((f) => f.id == theFoo)[0];
          if (match.total !== 5) {
            throw "Got wrong total when testing implementation: expected 5 but received " + match.total;
          }
        });
    })
    .then(function () {
      return fetch("/rest/foo/" + theFoo, {
        credentials: "same-origin",
        method: "DELETE",
      }).then(function (response) {
        if (!response.ok) {
          throw "Failed to delete on foo: " + total.message;
        }
        baseLab.complete("e2eBackEnd", true);
      });
    })
    .catch(function (error) {
      console.log(error);
      alert(error);
      baseLab.complete("e2eBackEnd", false);
    });
};

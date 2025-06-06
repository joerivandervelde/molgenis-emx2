<template>
  <Spinner v-if="!session" />
  <MessageWarning
    v-else-if="
      !session ||
      !session.roles ||
      !['Viewer'].some((r) => session.roles.includes(r))
    "
  >
    Schema doesn't exist or you don't have permission to view. Might you need to
    login?
  </MessageWarning>
  <div v-else>
    <router-link to="/">&lt; back to report list</router-link>
    <div v-if="edit">
      <h2>
        Edit report: {{ id }}<IconAction icon="eye" @click="edit = false" />
      </h2>
      <InputString
        id="reportId"
        v-model="id"
        label="id"
        :required="true"
        description="unique index"
      />
      <InputString
        id="reportDescription"
        v-model="description"
        label="description"
        description="human-readable description"
      />
      <InputText
        id="reportSql"
        v-model="sql"
        label="sql"
        description="You can type postgresql compatible SQL. You can also create parameters, e.g. ${name:string_array}, which will create a parameter 'name' that then internally is treated as a string_array (see column types). See the download link to understand how parameters are used in URL."
      />
      <MessageSuccess v-if="success">{{ success }}</MessageSuccess>
      <ButtonAction @click="save">Save</ButtonAction>
      <div class="mt-2">
        <h2>View report: {{ id }}</h2>
      </div>
    </div>
    <h2 v-else>
      View report id={{ id }}
      <IconAction v-if="canEdit" icon="pencil-alt" @click="edit = true" />
    </h2>
    <p v-if="description">Description: {{ description }}</p>
    <div v-if="parameterInputs">
      Please provide parameters:
      <FormInput
        v-for="input in parameterInputs"
        :id="input.name"
        :label="input.name"
        :name="input.name"
        :columnType="input.columnType"
        v-model="parameters[input.name]"
      />
      <ButtonAction @click="run">run</ButtonAction>
    </div>
    <MessageError v-if="error">{{ error }}</MessageError>
    <div v-if="rows">
      <Pagination v-if="count" v-model="page" :limit="limit" :count="count" />
      download as <a :href="downloadZip">zip</a>,
      <a :href="downloadExcel">excel</a> or
      <a :href="downloadJson">json</a>
      <TableSimple
        :columns="columns"
        :rows="rows"
        class="bg-white"
        :key="JSON.stringify(this.rows)"
      />
    </div>
    <div v-else-if="rows">No results found.</div>
  </div>
</template>

<script>
import {
  Client,
  TableSimple,
  ButtonAction,
  InputText,
  InputString,
  MessageError,
  MessageSuccess,
  Pagination,
  IconAction,
  Spinner,
  MessageWarning,
  FormInput,
} from "molgenis-components";
import { request } from "graphql-request";

export default {
  name: "EditQuery",
  components: {
    TableSimple,
    ButtonAction,
    InputText,
    MessageError,
    MessageSuccess,
    InputString,
    Pagination,
    IconAction,
    Spinner,
    MessageWarning,
    FormInput,
  },
  props: {
    session: Object,
    index: String,
    limit: { type: Number, default: 5 },
  },
  data() {
    return {
      rows: undefined,
      count: null,
      id: null,
      sql: 'select * from "Pet"',
      description: null,
      parameters: {},
      error: null,
      success: null,
      page: 1,
      edit: false,
    };
  },
  computed: {
    columns() {
      if (this.rows) {
        const names = [];
        this.rows.forEach((row) => {
          Object.keys(row).forEach((key) => {
            if (names.indexOf(key) === -1) {
              names.push(key);
            }
          });
        });
        return names;
      }
    },
    parameterKeyValueMap() {
      return Object.entries(this.parameters).map(([key, value]) => ({
        key: key,
        value: Array.isArray(value) ? value.join(",") : value,
      }));
    },
    canEdit() {
      return this.session?.roles?.includes("Manager");
    },
    parameterInputs() {
      const regex = /\${([^}]+)}/g;
      const matches = this.sql.match(regex);
      if (matches)
        return matches
          .map((match) => match.substr(2, match.length - 3))
          .map((match) =>
            match.includes(":")
              ? {
                  name: match.split(":")[0],
                  columnType: match.split(":")[1].toUpperCase(),
                }
              : { name: match, columnType: "STRING" }
          );
    },
    parametersQuery() {
      return Object.entries(this.parameters)
        .map((entry) =>
          Array.isArray(entry[1])
            ? "&" + entry[0] + "=" + entry[1].join(",")
            : "&" + entry[0] + "=" + entry[1]
        )
        .join("");
    },
    downloadZip() {
      return "../api/reports/zip?id=" + this.id + this.parametersQuery;
    },
    downloadExcel() {
      return "../api/reports/excel?id=" + this.id + this.parametersQuery;
    },
    downloadJson() {
      return "../api/reports/json?id=" + this.id + this.parametersQuery;
    },
  },
  methods: {
    async run() {
      this.error = null;
      const offset = this.limit * (this.page - 1);
      const result = await request(
        "graphql",
        `query report($parameters:[MolgenisSettingsInput]) {_reports(id:"${this.id}",parameters:$parameters,limit:${this.limit},offset:${offset}){data,count}}`,
        {
          parameters: this.parameterKeyValueMap,
        }
      ).catch((error) => {
        this.error = error.response.errors[0].message;
      });
      let parsedData = JSON.parse(result._reports.data);
      this.rows = Array.isArray(parsedData) ? parsedData : [parsedData];
      this.count = result._reports.count;
    },
    async save() {
      if (this.id == null) {
        this.error = "id is required";
        return;
      }
      if (this.sql == null) {
        this.error = "sql is required";
        return;
      }
      this.succes = null;
      this.error = null;
      const reports = await this.client.fetchSettingValue("reports");
      reports[this.index].id = this.id;
      reports[this.index].sql = this.sql;
      reports[this.index].description = this.description;
      this.client
        .saveSetting("reports", reports)
        .then((res) => {
          this.success = "Saved report " + this.id + " and refreshed query";
          this.run();
        })
        .catch((error) => (this.error = error));
    },
    async reload() {
      const reports = await this.client.fetchSettingValue("reports");
      if (reports[this.index]) {
        this.id = reports[this.index].id;
        this.sql = reports[this.index].sql;
        this.description = reports[this.index].description;
      } else {
        this.error = "report not found";
      }
      if (!this.sql.includes("${")) {
        await this.run();
      }
    },
  },
  watch: {
    page() {
      this.run();
    },
  },
  mounted() {
    this.client = Client.newClient();
    this.reload();
  },
};
</script>

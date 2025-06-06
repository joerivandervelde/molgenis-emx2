<template>
  <LayoutModal
    v-if="modalVisible === true"
    title="Edit column metadata"
    :isCloseButtonShown="false"
  >
    <template v-slot:body>
      <div class="row">
        <div class="column-scroll col">
          <Spinner v-if="loading" />
          <div v-else>
            <MessageWarning v-if="column.drop">
              Marked for deletion
            </MessageWarning>
            <MessageError v-if="error">{{ error }}</MessageError>
            <div class="row">
              <div class="col-4">
                <InputString
                  id="column_name"
                  v-model="column.name"
                  label="columnName"
                  :errorMessage="nameInvalid"
                />
              </div>
              <div class="col-4">
                <InputTextLocalized
                  id="column_label"
                  v-model="column.labels"
                  label="label"
                  :locales="locales"
                />
              </div>
              <div class="col-4">
                <div class="input-group">
                  <InputTextLocalized
                    id="column_description"
                    v-model="column.descriptions"
                    label="description"
                    :locales="locales"
                  />
                </div>
              </div>
            </div>
            <div class="row">
              <div class="col-4">
                <InputSelect
                  id="column_columnType"
                  v-model="column.columnType"
                  :options="columnTypes"
                  label="columnType"
                />
              </div>
              <div
                class="col-4"
                v-if="
                  column.columnType === 'REF' ||
                  column.columnType === 'REF_ARRAY' ||
                  column.columnType === 'REFBACK' ||
                  column.columnType === 'ONTOLOGY' ||
                  column.columnType === 'ONTOLOGY_ARRAY'
                "
              >
                <InputSelect
                  id="column_refTable"
                  v-model="column.refTableName"
                  :errorMessage="
                    column.refTableName === undefined || column.name === ''
                      ? 'Referenced table is required'
                      : undefined
                  "
                  :noOptionsProvidedMessage="
                    'No ' +
                    (column.columnType.includes('ONTOLOGY')
                      ? 'ontology table'
                      : 'data table') +
                    ' found in schema \'' +
                    (column.refSchemaName || schema.name) +
                    '\''
                  "
                  :options="tableNames"
                  label="refTable"
                />
                <InputSelect
                  id="column_refSchema"
                  v-model="column.refSchemaName"
                  :options="schemaNames"
                  @update:modelValue="loadRefSchema"
                  label="refSchema"
                  description="When you want to refer to table in another schema"
                />
                <InputString
                  id="column_refLabel"
                  v-model="column.refLabel"
                  label="refLabel"
                  description="(Optional) customize how ref values should be shown. E.g. '${name}' or '${firstName} ${lastName}'"
                />
              </div>
              <div class="col-4" v-if="column.columnType === 'REFBACK'">
                <InputSelect
                  id="column_refBack"
                  label="refBack"
                  v-model="column.refBackName"
                  :options="refBackCandidates(column.refTableName, table.name)"
                />
              </div>
              <div
                class="col-4"
                v-if="
                  column.refTableName &&
                  (column.columnType === 'REF' ||
                    column.columnType === 'REF_ARRAY')
                "
              >
                <InputSelect
                  v-if="refLinkCandidates.length > 0"
                  id="column_refLink"
                  v-model="column.refLinkName"
                  :options="refLinkCandidates"
                  label="refLink"
                  description="refLink enables to define overlapping references, e.g. 'patientId', 'sampleId' (where sample also overlaps with patientId)"
                />
              </div>
            </div>
            <div class="row">
              <div class="col-4" v-if="isEditable(column)">
                <InputRadio
                  id="column_required_radio"
                  label="required"
                  :options="[true, false, 'condition']"
                  v-model="requiredSelect"
                  description="Will give error unless field is filled in. Is not checked if not visible"
                  @update:modelValue="setRequired"
                />
                <InputString
                  id="column_required"
                  v-model="column.required"
                  v-if="requiredSelect === 'condition'"
                />
              </div>
              <div class="col-4" v-if="isEditable(column)">
                <InputBoolean
                  id="column_readonly"
                  v-model="column.readonly"
                  label="isReadonly"
                />
              </div>
              <div class="col-4" v-if="isEditable(column)">
                <InputText
                  id="column_default"
                  v-model="column.defaultValue"
                  label="defaultValue"
                />
              </div>
            </div>
            <div class="row">
              <div class="col-4" v-if="column.columnType !== 'CONSTANT'">
                <InputSelect
                  id="column_key"
                  v-model="column.key"
                  :options="[1, 2, 3, 4, 5, 6, 7, 8, 9, 10]"
                  label="key"
                />
              </div>
            </div>
            <div class="row">
              <div class="col">
                <ButtonAction
                  class="float-right"
                  v-if="!previewShow"
                  @click="previewShow = true"
                >
                  show form preview
                </ButtonAction>
              </div>
            </div>
            <div class="row">
              <div class="col-4" v-if="isEditable(column)">
                <InputText
                  id="column_validation"
                  v-model="column.validation"
                  label="validation"
                  description="When javascript expression returns 'false' the expression itself is shown. Example: name === 'John'. When javascript expression returns a string then this string is shown. Example if(name!=='John')'name should be John'. Is not checked if not visible."
                />
              </div>
              <div class="col-4" v-if="column.columnType !== AUTO_ID">
                <InputText
                  id="column_visible"
                  v-model="column.visible"
                  label="visible"
                  description="When set only show when javascript expression is !null or !false. Example: other > 5"
                />
              </div>
              <div class="col-4">
                <InputText
                  id="column_computed"
                  v-model="column.computed"
                  label="computed"
                  :description="
                    column.columnType == AUTO_ID
                      ? 'Use pattern like \'pre${mg_autoid}post\' to customize prefix/postfix of your auto id'
                      : 'When set only the input will be readonly and value computed using this formula'
                  "
                />
              </div>
            </div>
            <div class="row">
              <div class="col-4">
                <ArrayInput
                  id="column_semantics"
                  columnType="STRING_ARRAY"
                  v-model="column.semantics"
                  :list="true"
                  label="semantics"
                />
              </div>
            </div>

            <div class="row" v-if="subclassNames !== undefined">
              <div class="col">
                <InputSelect
                  :readonly="column.oldName !== undefined"
                  id="column_table"
                  v-model="column.table"
                  :options="subclassNames"
                  :list="true"
                  label="Available in subclass"
                  description="indicate this column is available in particular subclass only. Cannot be changed after creation. We hope to enable this in future version"
                />
              </div>
            </div>
          </div>
        </div>
        <div v-if="previewShow" class="col-4 bg-white column-scroll">
          <h4>
            Form preview
            <ButtonAlt @click="previewShow = false" class="pl-0 pr-0"
              >hide
            </ButtonAlt>
          </h4>
          <RowEdit
            id="form-edit"
            v-model="previewData"
            :schemaMetaData="schemaWithIdsLabelsAndDescriptions"
            :tableMetaData="tableWithIdsLabelsAndDescriptions"
            :tableId="tableWithIdsLabelsAndDescriptions.id"
            :key="JSON.stringify(table)"
            :applyDefaultValues="true"
            :errorPerColumn="rowErrors"
            @update:model-value="checkForErrors"
          />
          Values:
          {{ previewData }}
        </div>
      </div>
    </template>
    <template v-slot:footer>
      <ButtonAlt @click="cancel">Cancel</ButtonAlt>
      <ButtonAction @click="apply" :disabled="isDisabled">Apply</ButtonAction>
    </template>
  </LayoutModal>
  <IconAction
    v-else
    class="btn-sm hoverIcon"
    :icon="operation === 'add' ? 'plus' : 'pencil-alt'"
    :tooltip="tooltip"
    @click="showModal"
  />
</template>

<style>
.column-scroll {
  /** want to have columns not higher than modal allows so we get separate scroll bars for preview */
  max-height: calc(100vh - 240px);
  overflow-y: auto;
}
</style>

<script lang="ts">
import {
  constants,
  //@ts-ignore
  ButtonAction, //@ts-ignore
  ButtonAlt, //@ts-ignore
  Client, //@ts-ignore
  IconAction, //@ts-ignore
  InputBoolean, //@ts-ignore
  InputRadio, //@ts-ignore
  InputSelect, //@ts-ignore
  InputString, //@ts-ignore
  ArrayInput, //@ts-ignore
  InputText, //@ts-ignore
  InputTextLocalized, //@ts-ignore
  LayoutForm, //@ts-ignore
  LayoutModal, //@ts-ignore
  MessageError, //@ts-ignore
  MessageWarning, //@ts-ignore
  RowEdit, //@ts-ignore
  Spinner, //@ts-ignore
  deepClone, //@ts-ignore
  getRowErrors,
} from "molgenis-components";
import columnTypes from "../columnTypes.js";
import { addTableIdsLabelsDescription } from "../utils";

const AUTO_ID = "AUTO_ID";

export default {
  components: {
    LayoutForm,
    InputText,
    InputString,
    ArrayInput,
    InputBoolean,
    InputSelect,
    InputRadio,
    IconAction,
    InputTextLocalized,
    LayoutModal,
    ButtonAction,
    MessageWarning,
    MessageError,
    ButtonAlt,
    Spinner,
    RowEdit,
  },
  props: {
    /** Column metadata object entered as v-model in case of updates*/
    modelValue: {
      type: Object,
    },
    /** schema  column is part of, used for ref options*/
    tableName: {
      type: String,
    },
    /** schema column is part of, used for ref options*/
    schema: {
      type: Object,
      required: true,
    },
    /** list of schemas for externalSchema select */
    schemaNames: {
      type: Array,
      required: true,
    },
    /** can be set to 'add' */
    operation: {
      type: String,
      default: "edit",
    },
    /** Optional tooltip*/
    tooltip: {
      type: String,
      required: false,
    },
    locales: {
      type: Array,
    },
    columnIndex: {
      type: Number,
      required: true,
    },
  },
  data() {
    return {
      //if modal is visible
      modalVisible: false,
      // working value of the column (copy of the value)
      column: {} as Record<string, any>,
      requiredSelect: false as boolean | string,
      //in case a refSchema has to be used for the table lookup
      refSchema: undefined as undefined | any,
      error: undefined,
      client: null as any,
      loading: false,
      previewShow: false,
      previewData: {} as Record<string, any>,
      rowErrors: {} as Record<string, string>,
      columnTypes,
      AUTO_ID,
    };
  },
  computed: {
    //current table object unedited
    originalTable() {
      return this.schema.tables.find(
        (table: Record<string, any>) =>
          table.name === this.tableName ||
          table.name === this.column.table ||
          (table.subclasses && table.subclasses.includes(this.column.table))
      );
    },
    //current table object edited
    table() {
      const table = deepClone(this.originalTable);
      //replace column with current changes
      const index = table.columns.findIndex(
        (c: Record<string, any>) =>
          c.name == this.column.name || c.name == this.column.oldName
      );
      // or if new, we add it
      if (index === -1) {
        table.columns.splice(this.columnIndex, 0, this.column);
      } else {
        table.columns[index] = this.column;
      }
      return table;
    },
    //tableMetadata with the ids, labels, descriptions added (duplication of conversions normally done server side)
    tableWithIdsLabelsAndDescriptions() {
      return addTableIdsLabelsDescription(deepClone(this.originalTable));
    },
    //schema metadata with ids
    schemaWithIdsLabelsAndDescriptions() {
      const schema = deepClone(this.schema);
      schema.id = schema.name;
      schema.tables = schema.tables.map((table: Record<string, any>) => {
        return addTableIdsLabelsDescription(table);
      });
      return schema;
    },
    //listing of related subclasses, used to indicate if column is part of subclass
    subclassNames() {
      if (this.table?.subclasses) {
        return this.table?.subclasses.map(
          (subclass: Record<string, any>) => subclass.name
        );
      } else {
        return undefined;
      }
    },
    //listing of all tables, used for refs
    tableNames() {
      if (this.column.refSchemaName && this.refSchema.tables) {
        if (
          this.column.columnType === "ONTOLOGY" ||
          this.column.columnType === "ONTOLOGY_ARRAY"
        ) {
          return this.refSchema.tables
            .filter((t: Record<string, any>) => t.tableType === "ONTOLOGIES")
            .map((t: Record<string, any>) => t.name);
        } else {
          return this.refSchema.tables
            .filter((t: Record<string, any>) => t.tableType !== "ONTOLOGIES")
            .map((t: Record<string, any>) => t.name);
        }
      } else {
        if (
          this.column.columnType === "ONTOLOGY" ||
          this.column.columnType === "ONTOLOGY_ARRAY"
        ) {
          return this.schema.ontologies.map((t: Record<string, any>) => t.name);
        } else {
          return this.schema.tables.map((t: Record<string, any>) => t.name);
        }
      }
    },
    nameInvalid() {
      if (this.column.name === undefined || this.column.name === "") {
        return "Name is required";
      }
      if (!this.column.name.match(constants.COLUMN_NAME_REGEX)) {
        return "Name must start with a letter, followed by zero or more letters, numbers, spaces or underscores. A space immediately before or after an underscore is not allowed. The character limit is 63.";
      }
      if (
        (this.modelValue === undefined ||
          this.modelValue.name !== this.column.name) &&
        this.originalTable.columns?.filter(
          (c: Record<string, any>) => c.name === this.column.name
        ).length > 0
      ) {
        return "Name should be unique";
      } else {
        return undefined;
      }
    },
    isDisabled() {
      return this.operation !== "add" && this.nameInvalid;
    },
  },
  methods: {
    showModal() {
      this.modalVisible = true;
    },
    apply() {
      this.modalVisible = false;
      if (this.operation === "edit") {
        this.$emit("update:modelValue", this.column);
      } else {
        this.$emit("add", this.column);
        this.reset();
      }
    },
    cancel() {
      this.reset();
      this.modalVisible = false;
    },
    setRequired() {
      this.column.required = this.requiredSelect;
    },
    refLinkCandidates() {
      return this.table.columns
        .filter(
          (c: Record<string, any>) =>
            (c.columnType === "REF" || c.columnType === "REF_ARRAY") &&
            c.name !== this.modelValue?.name
        )
        .map((c: Record<string, any>) => c.name);
    },
    refBackCandidates(fromTable: string, toTable: Record<string, any>) {
      const schema =
        this.refSchema !== undefined ? this.refSchema : this.schema;
      const columns = getRefTableColumns(fromTable, schema.tables);
      return columns
        .filter(
          (column: Record<string, any>) => column.refTableName === toTable
        )
        .map((column: Record<string, any>) => column.name);
    },
    async loadRefSchema() {
      this.error = undefined;
      this.loading = true;
      if (this.column.refSchemaName) {
        //todo, don't use client here because we need 'names' not 'ids'
        this.client = Client.newClient(this.column.refSchemaName);
        const schema = await this.client
          .fetchSchemaMetaData()
          .catch((e: any) => {
            this.error = e;
          });
        this.refSchema = schema;
      } else {
        this.refSchema = {};
      }
      this.loading = false;
    },
    setupRequiredSelect() {
      if (this.column.required === "true" || this.column.required === true) {
        this.requiredSelect = true;
      } else if (
        this.column.required === "false" ||
        this.column.required === false ||
        this.column.required === undefined
      ) {
        this.requiredSelect = false;
      } else {
        this.requiredSelect = "condition";
      }
    },
    reset() {
      //deep copy so it doesn't update during edits
      if (this.modelValue) {
        this.column = deepClone(this.modelValue);
      } else {
        this.column = { table: this.tableName, columnType: "STRING" };
      }
      //if reference to external schema
      if (this.column.refSchemaName != undefined) {
        this.loadRefSchema();
      }
      this.setupRequiredSelect();
      this.modalVisible = false;
    },
    isEditable(column: Record<string, any>) {
      return (
        column.columnType !== "CONSTANT" &&
        !column.computed &&
        column.columnType !== AUTO_ID
      );
    },
    checkForErrors() {
      this.rowErrors = getRowErrors(this.table, this.previewData);
    },
  },
  created() {
    this.reset();
  },
  emits: ["add", "update:modelValue"],
};

function getRefTableColumns(
  fromTable: string,
  tables: Record<string, any>[]
): Record<string, any>[] {
  const table = tables.find(
    (table: Record<string, any>) => table.name === fromTable
  );
  if (table?.inheritName) {
    const inheritedTable = tables.find(
      (otherTable: Record<string, any>) => table.inheritName === otherTable.name
    );
    return [...inheritedTable?.columns, ...table?.columns];
  } else {
    return table?.columns || [];
  }
}
</script>

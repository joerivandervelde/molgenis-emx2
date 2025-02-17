<template>
  <Page>
    <PageHeader
      title="GDI Local Portal"
      subtitle="Search for datasets"
      imageSrc="img/bkg-datasets.jpg"
      titlePositionX="center"
      height="large"
    />
    <PageSection
      width="large"
      class="bg-gray-050"
      :horizontalPadding="2"
      aria-labelledby="datasets-title"
    >
      <h2 id="datasets-title" class="visually-hidden">Available Datasets</h2>
      <MessageBox v-if="noRemsHostFound" type="error">
        <p>
          Unable to retrieve local REMS URL. Make sure your REMS instance is
          configured with MOLGENIS. To check the status:
        </p>
        <ol>
          <li>navigate to your schema and then settings page</li>
          <li>Click the "Advanced Settings" tab</li>
          <li>
            In the settings table, confirm that there is a setting for
            <code>REMS_URL</code>
          </li>
        </ol>
        <p>
          If the setting <code>REMS_URL</code> is missing, you can add it
          manually by clicking on the plus icon. The "key" should be
          <code>REMS_URL</code> and the "setting value" should be your REMS
          host.
        </p>
      </MessageBox>
      <MessageBox v-else-if="noDatasetFound" type="error">
        <p>
          Unable to generate dataset view. It is likely that the datasets aren't
          defined or there is information missing.
        </p>
      </MessageBox>
      <div class="sidebar-layout" v-else>
        <aside class="sidebar-menu">
          <h3>Dataset selection</h3>
          <p>Select one or more datasets and add them to the cart.</p>
          <a :href="remsApplyUrl" type="button" class="btn btn-primary">
            Request Access {{ selection.length ? `(${selection.length})` : "" }}
            <ArrowTopRightOnSquareIcon
              class="heroicon"
              style="margin-top: -3px"
            />
          </a>
          <ButtonOutline @click="selectAll"> Select all </ButtonOutline>
          <ButtonAlt @click="clearAll"> Clear all </ButtonAlt>
        </aside>
        <div class="sidebar-main">
          <h3>Available Datasets</h3>
          <p>
            Review each dataset and select the ones of interest by clicking the
            add "+" button. Click the button again to remove from the dataset
            from the cart. When you are satisfied with your selection, click the
            "Request Access" button to start the application process in REMS.
          </p>
          <div class="dataset-card" v-for="dataset in datasets">
            <div class="dataset-info">
              <h3 class="dataset-title">{{ dataset.title }}</h3>
              <p class="dataset-description" v-if="dataset.description">
                {{ dataset.description }}
              </p>
              <ul class="dataset-tags">
                <li class="tag-type" v-if="dataset.type">
                  {{ dataset.type }}
                </li>
                <li class="tag-file-count" v-if="dataset.fileCount">
                  {{
                    dataset.fileCount > 1
                      ? `${dataset.fileCount} files`
                      : `${dataset.fileCount} file`
                  }}
                </li>
                <li
                  class="tag-format"
                  v-if="dataset.fileFormats?.length"
                  v-for="format in dataset.fileFormats"
                >
                  {{ format }}
                </li>
              </ul>
            </div>
            <div class="dataset-selector">
              <input
                :id="dataset.id"
                type="checkbox"
                class="input visually-hidden"
                name="rems-selections"
                v-model="selection"
                :value="dataset.id"
                :ref="() => setRefs"
                @change="applySelectionStyle"
              />
              <label :for="dataset.id" class="btn btn-outline-primary">
                <span class="visually-hidden">
                  {{ selection.includes(dataset.id) ? "Remove" : "Add" }}
                </span>
                <PlusIcon class="heroicon" />
              </label>
            </div>
          </div>
        </div>
      </div>
    </PageSection>
  </Page>
</template>

<script setup lang="ts">
import gql from "graphql-tag";
import { request } from "graphql-request";
import { ref, watch, onBeforeMount } from "vue";
import { PlusIcon, ArrowTopRightOnSquareIcon } from "@heroicons/vue/24/outline";
import { cleanUrl } from "../utils/index";

// @ts-ignore
import { Page, PageHeader, PageSection, MessageBox } from "molgenis-viz";
// @ts-ignore
import { ButtonAlt, ButtonOutline } from "molgenis-components";

import type {
  ISettingsApiResponse,
  IDatasetSchema,
  IDatasetRef,
  IDatasetApiResponse,
  IDistributionFilesSchema,
  IDatasetDistributionSchema,
  IKeyValue,
} from "../interfaces/datasets";

let datasets = ref<IDatasetRef[]>([]);
let selection = ref<string[]>([]);
let checkboxes = ref<string[]>([]);
let remsHost = ref<string>("");
let remsApplyUrl = ref<string>("");
let noRemsHostFound = ref<boolean>(false);
let noDatasetFound = ref<boolean>(false);

async function getDatasets() {
  const query = gql`
    {
      Dataset {
        id
        title
        description
        distribution {
          name
          description
          type {
            name
          }
          files {
            identifier
            format {
              name
            }
            name
          }
        }
      }
    }
  `;
  const response: IDatasetApiResponse = await request("../api/graphql", query);
  const data = response.Dataset.map((dataset: IDatasetSchema) => {
    const distribution = dataset.distribution[0] as IDatasetDistributionSchema;
    const formats = distribution?.files?.map((file: IDistributionFilesSchema) =>
      file.format?.name.toLowerCase()
    );
    return {
      id: dataset.id,
      title: dataset.title,
      description: dataset.description,
      type: distribution?.type?.name,
      fileCount: distribution?.files?.length || 0,
      fileFormats: [...Array.from(new Set(formats))] || [],
    };
  });

  datasets.value = data as IDatasetRef[];
}

async function setRemsHost() {
  const query = gql`
    {
      _settings {
        key
        value
      }
    }
  `;
  const response: ISettingsApiResponse = await request("../api/graphql", query);
  const settings = response._settings;
  const keys = settings.map((row) => row.key);

  if (!keys.includes("REMS_URL")) {
    throw new Error("Setting for REM_URL not found");
  }

  const url = settings.filter((row: IKeyValue) => row.key === "REMS_URL")[0]
    .value as string;
  remsHost.value = cleanUrl(url);
}

function setUrl() {
  const resources = selection.value.map((item) => `resource=${item}`);
  remsApplyUrl.value = `${remsHost.value}/apply-for?${resources.join("&")}`;
}

function clearAll() {
  selection.value = [];
  remsApplyUrl.value = remsHost.value;
  const cards = document.querySelectorAll("div.dataset-card");
  cards.forEach((card) => card.classList.remove("card-selected"));
}

function setRefs(value: HTMLFormElement) {
  if (value !== null) {
    checkboxes.value.push(value?._value);
  }
}

function selectAll() {
  checkboxes.value.forEach((value: string) => {
    if (selection.value.indexOf(value) === -1) {
      selection.value.push(value);
    }
  });
  setUrl();
  const cards = document.querySelectorAll("div.dataset-card");
  cards.forEach((card) => card.classList.add("card-selected"));
}

function applySelectionStyle(event: Event) {
  const eventTarget = event.target as HTMLInputElement;
  const value = eventTarget?.value;
  const parent = eventTarget?.parentNode?.parentNode as HTMLElement;
  if (selection.value.includes(value)) {
    parent.classList.add("card-selected");
  } else {
    parent.classList.remove("card-selected");
  }
}

onBeforeMount(() => {
  setRemsHost()
    .then(() => {
      noRemsHostFound.value = false;
    })
    .catch(() => (noRemsHostFound.value = true));

  getDatasets().catch(() => (noDatasetFound.value = true));
});

watch([selection], setUrl);
</script>

<style lang="scss">
.dataset-card {
  display: grid;
  grid-template-columns: 1.5fr 0.5fr;
  background-color: $gray-000;
  box-sizing: content-box;
  padding: 2em;
  border-bottom: 1px solid $gdi-brand-purple;

  @media screen and (min-width: 724px) {
    grid-template-columns: "info button";
  }

  .dataset-info {
    h3 {
      @include textTransform(bold);
    }

    .dataset-tags {
      list-style: none;
      display: flex;
      flex-direction: row;
      flex-wrap: wrap;
      margin: 0;
      padding: 0;
      gap: 1em;

      li {
        padding: 0.15em 0.8em;
        border-radius: 24px;
        color: $blue-800;
        background-color: $gray-050;
        font-size: 11pt;
      }
    }
  }

  .dataset-selector {
    @include flexCenterAll;

    .heroicon {
      width: 18px;
      height: 18px;
    }
  }

  .btn {
    border-width: 2px;

    .heroicon {
      path {
        stroke-width: 2px;
      }
    }
  }

  &.card-selected {
    background-color: $gdi-brand-purple-light;

    .heroicon {
      path {
        transform: rotate(-45deg);
        transform-origin: center;
      }
    }

    p {
      color: $gdi-brand-purple;
    }

    .dataset-tags {
      li {
        background-color: $gdi-brand-purple;
        color: $gray-000;
      }
    }
  }
}
</style>

<script setup lang="ts">
import type { IResource, IMgError } from "~/interfaces/types";
const route = useRoute();
const query = `
  query Resources($id: String) {
    Resources(filter: { id: { equals: [$id] } }) {
      acronym
      name
      website
      contactEmail
      description
      logo {
        url
      }
    }
  }
`;

interface IResponse {
  data: {
    Resources: IResource[];
  };
}

const variables = { id: route.params.catalogue };

const { data, error } = await useFetch<IResponse, IMgError>(
  `/${route.params.schema}/graphql`,
  {
    method: "POST",
    body: { query, variables },
  }
);

const network = computed(() => {
  return data.value?.data.Resources[0];
});

let crumbs: Record<string, string> = {};
crumbs[
  route.params.catalogue.toString()
] = `/${route.params.schema}/ssr-catalogue/${route.params.catalogue}`;
crumbs[
  "about"
] = `/${route.params.schema}/ssr-catalogue/${route.params.catalogue}/about-catalogue`;
</script>

<template>
  <LayoutsDetailPage>
    <template #header>
      <PageHeader :title="network?.name" :description="network?.description">
        <template #prefix>
          <BreadCrumbs :crumbs="crumbs" />
        </template>
        <!-- <template #title-suffix>
          <IconButton icon="star" label="Favorite" />
        </template> -->
      </PageHeader>
    </template>
    <template #main>
      <ContentBlocks>
        <ContentBlockIntro
          id="intro"
          v-if="network?.logo?.url || network?.website || network?.contactEmail"
          :image="network?.logo?.url"
          :link="network?.website"
          :contact="network?.contactEmail"
        />
        <ContentBlock
          v-if="network?.description"
          id="description"
          title="Description"
          :description="network?.description"
        />
      </ContentBlocks>
    </template>
  </LayoutsDetailPage>
</template>

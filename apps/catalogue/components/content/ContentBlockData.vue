<script setup lang="ts">
import type { ICollectionEvents } from "~/interfaces/catalogue";
import { defineProps } from "vue";
import { buildTree } from "../../utils/ontologyUtils";

const { collectionEvents } = defineProps<{
  title: string;
  description?: string;
  collectionEvents?: ICollectionEvents[];
}>();

const dataCategories = collectionEvents
  ?.flatMap((c) => c.dataCategories)
  .filter((e) => e !== undefined);
const sampleCategories = collectionEvents
  ?.flatMap((c) => c.sampleCategories)
  .filter((e) => e !== undefined);
const areasOfInformation = collectionEvents
  ?.flatMap((c) => c.areasOfInformation)
  .filter((e) => e !== undefined);
</script>

<template>
  <ContentBlock
    :title="title"
    :description="description"
    v-if="
      collectionEvents &&
      (dataCategories?.length ||
        sampleCategories?.length ||
        areasOfInformation?.length)
    "
  >
    <div class="grid gap-[45px] mt-7.5">
      <ListCollapsible
        v-if="dataCategories?.length"
        title="Data categories"
        :collapse-all="false"
      >
        <ContentOntology
          :tree="buildTree(dataCategories)"
          :inverted="true"
        ></ContentOntology>
      </ListCollapsible>
      <ListCollapsible
        v-if="sampleCategories?.length"
        title="Sample categories"
        :collapse-all="false"
      >
        <ContentOntology
          :tree="buildTree(sampleCategories)"
          :inverted="true"
        ></ContentOntology>
      </ListCollapsible>
      <ListCollapsible
        v-if="areasOfInformation?.length"
        title="Areas of information"
        :collapse-all="false"
      >
        <ContentOntology
          :tree="buildTree(areasOfInformation)"
          :inverted="true"
        ></ContentOntology>
      </ListCollapsible>
    </div>
  </ContentBlock>
</template>

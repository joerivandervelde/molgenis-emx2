<script setup lang="ts">
import type { IManifestResponse, IMgError } from "~~/interfaces/types";

const { data } = await useGqlFetch<IManifestResponse, IMgError>(
  ` 
    query manifest{
      _manifest {
        ImplementationVersion
        SpecificationVersion
        DatabaseVersion
      }
    }`,
  { key: "manifest" }
);

const manifest = computed(() => data.value?.data._manifest);
</script>

<template>
  <div class="mb-0 text-center lg:pb-5 text-title text-body-lg">
    <span v-if="manifest?.SpecificationVersion">
      Software version: {{ manifest.SpecificationVersion }}
    </span>
  </div>
</template>

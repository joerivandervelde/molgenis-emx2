<script setup>
defineProps({
  crumbs: {
    type: Object,
    default: [],
    required: false,
  },
  current: {
    type: String,
    required: false,
  },
});
</script>

<template>
  <div class="flex justify-between xl:hidden text-menu">
    <NuxtLink :to="Object.values(crumbs).slice(-1)[0]">
      <span class="sr-only">Go up one level</span>
      <BaseIcon name="arrow-left" />
    </NuxtLink>
    <!-- <a href="#">
      <span class="sr-only">Favorite</span>
      <BaseIcon name="star" />
    </a> -->
  </div>
  <nav
    class="items-center justify-center hidden gap-3 tracking-widest xl:flex font-display text-heading-lg"
  >
    <template v-for="(url, label, index) in crumbs" :key="label">
      <NuxtLink :to="url" class="text-breadcrumb hover:underline">{{
        label
      }}</NuxtLink>
      <span
        v-if="index < Object.keys(crumbs).length - 1"
        class="text-breadcrumb-arrow"
      >
        <BaseIcon name="caret-right" :width="12" />
      </span>
    </template>
    <template v-if="current">
      <BaseIcon
        v-if="Object.keys(crumbs).length > 0"
        name="caret-right"
        :width="12"
        class="text-breadcrumb-arrow"
      /><a class="text-breadcrumb"> {{ current }}</a>
    </template>
  </nav>
</template>

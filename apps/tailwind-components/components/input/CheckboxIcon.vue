<template>
  <svg
    width="20"
    height="20"
    view-box="0 0 20 20"
    xmlns="http://www.w3.org/2000/svg"
    class="hover:cursor-pointer w-[20px] mr-2.5"
    :data-checked="checked"
    :data-indeterminate="indeterminate"
  >
    <rect
      width="20"
      height="20"
      :style="{ 'stroke-width': invalid ? 4 : 1 }"
      :class="{
        'fill-input': !checked && !indeterminate,
        'fill-input-checked': checked || indeterminate,
        'fill-invalid': checked && invalid,
        'fill-valid': checked && valid,
        'stroke-current': !disabled,
        'stroke-valid': valid,
        'stroke-invalid': invalid,
      }"
    />
    <path
      v-if="checked"
      d="M5,10 L 9,14 L 16,6"
      class="stroke-check"
      stroke-width="2"
      stroke-linecap="round"
      stroke-linejoin="round"
      fill="none"
    />
    <path
      v-if="indeterminate"
      d="M5,10 L 15,10"
      class="stroke-gray-900"
      stroke-width="2"
      stroke-linecap="round"
      stroke-linejoin="round"
      fill="none"
    />
  </svg>
</template>

<script lang="ts" setup>
defineProps<{
  checked?: boolean;
  indeterminate?: boolean;
  invalid?: boolean;
  valid?: boolean;
  disabled?: boolean;
}>();
</script>

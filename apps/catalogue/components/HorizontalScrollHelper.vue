<script setup lang="ts">
import { ref } from "vue";

defineProps({
  addFade: {
    type: Boolean,
  },
  addScrollButton: {
    type: Boolean,
  },
});

let scrollAtStart = ref(true);
let scrollAtEnd = ref(false);
let scrollElement = ref();

function scroll(event: Event) {
  let target = event.target as HTMLInputElement;
  scrollAtStart.value = target.scrollLeft === 0;
  scrollAtEnd.value =
    target.scrollLeft + target.offsetWidth === target.scrollWidth;
}

function smoothScroll(value: number) {
  scrollElement.value.scrollTo({
    left: scrollElement.value.scrollLeft - value,
    behavior: "smooth",
  });
}
</script>

<template>
  <div class="relative">
    <div
      :class="addFade && scrollAtStart ? 'opacity-0' : 'opacity-100'"
      class="flex items-center justify-items-start absolute z-10 left-0 inset-y-0 w-10 bg-gradient-to-r from-white to-transparent pointer-events-none transition-opacity"
    >
      <div
        class="relative right-6 flex items-center justify-center pointer-events-auto bg-white hover:bg-gray-100 rounded-full border w-10 h-10"
        @click="smoothScroll(200)"
      >
        <BaseIcon name="caret-left" :width="26" />
      </div>
    </div>

    <div
      :class="addFade && scrollAtEnd ? 'opacity-0' : 'opacity-100'"
      class="flex items-center justify-items-end absolute z-10 right-0 inset-y-0 w-10 bg-gradient-to-r from-transparent to-white pointer-events-none transition-opacity"
    >
      <div
        class="relative left-6 flex items-center justify-center pointer-events-auto bg-white hover:bg-gray-100 rounded-full border w-10 h-10"
        @click="smoothScroll(-200)"
      >
        <BaseIcon name="caret-right" :width="26" />
      </div>
    </div>
    <div class="overflow-x-auto" @scroll.passive="scroll" ref="scrollElement">
      <slot></slot>
    </div>
  </div>
</template>

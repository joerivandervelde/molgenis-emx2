<script setup lang="ts">
import type { columnValue } from "../../../metadata-utils/src/types";

const props = withDefaults(
  defineProps<{
    id: string;
    label?: string;
    value?: string;
    placeholder?: string;
    disabled?: boolean;
    required?: boolean;
    valid?: boolean;
    hasError?: boolean;
  }>(),
  {
    disabled: false,
    required: false,
    hasError: false,
    valid: false,
  }
);

const emit = defineEmits(["focus", "input", "error", "update:modelValue"]);
defineExpose({ validate });

const modelValue = ref("");

function validate(value: columnValue) {
  if (props.required && value === "") {
    emit("error", [
      { message: `${props.label || props.id} required to complete the form` },
    ]);
  } else {
    emit("error", []);
  }
}
</script>

<template>
  <textarea
    :id="id"
    :required="required"
    @focus="$emit('focus')"
    @input="$emit('input')"
    :placeholder="placeholder"
    :disabled="disabled"
    class="w-full pr-16 font-sans text-black text-gray-300 outline-none rounded-textarea-input h-60 pl-3 shadow-search-input focus:shadow-search-input hover:shadow-search-input search-input-mobile border py-2"
    :class="{
      'border-invalid text-invalid': hasError,
      'border-valid text-valid': valid,
      'border-disabled text-disabled bg-disabled': disabled,
      'bg-white': !disabled,
    }"
    v-model="modelValue"
  />
</template>

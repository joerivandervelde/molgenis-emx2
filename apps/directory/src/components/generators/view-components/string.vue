<template>
  <tr v-if="attribute && attribute.value && attribute.value.length">
    <th scope="row" class="pr-1 align-top text-nowrap">
      {{ displayName(attribute) }}
    </th>
    <td>
      <span>
        {{ attribute.value }}
      </span>

      <tooltip-component
        class="ml-2 copy-item"
        v-if="attribute.linkValue"
        text="Copy to clipboard"
        @click.prevent="copyToClipboard(attribute.linkValue)"
      >
        <span id="copy-icon" class="fa fa-clipboard"> </span>
      </tooltip-component>
    </td>
  </tr>
</template>

<script>
import { TooltipComponent } from "molgenis-components";

export default {
  components: {
    TooltipComponent,
  },
  props: {
    attribute: {
      type: Object,
    },
  },
  methods: {
    displayName(item) {
      return item.label || item.name || item.id;
    },
    copyToClipboard(link) {
      navigator.clipboard.writeText(link);
    },
  },
};
</script>

<style scoped>
.copy-item {
  display: inline-block;
  width: 1rem;
}

.fa-clipboard {
  position: relative;
  font-size: large;
}

.fa-clipboard:hover {
  cursor: pointer;
}

.fa-external-link {
  top: 1px;
  position: relative;
}

.fa-external-link:hover {
  cursor: pointer;
}
</style>

<template>
  <aside class="sidebar">
    <h2>Dashboards</h2>
    <nav>
      <ul class="navlinks">
        <li>
          <router-link :to="{ name: 'home' }"> Your center </router-link>
        </li>
        <li>
          <Accordion
            id="cranio-nav"
            title="Craniosynostosis"
            :isOpenByDefault="true"
          >
            <p>All Centers</p>
            <ul>
              <li>
                <router-link :to="{ name: 'provider-cs-all-general' }">
                  General overview
                </router-link>
              </li>
              <li>
                <router-link :to="{ name: 'provider-cs-all-surgical' }">
                  Surgical Overview
                </router-link>
              </li>
            </ul>
            <p>Your Center</p>
            <ul>
              <li>
                <router-link :to="{ name: 'provider-cs-center-overview' }">
                  General overview
                </router-link>
              </li>
              <li>
                <router-link :to="{ name: 'provider-cs-center-surgical' }">
                  Surgical overview
                </router-link>
              </li>
            </ul>
          </Accordion>
        </li>
        <li>
          <Accordion
            id="clp-nav"
            title="Cleft lip and palate"
            :isOpenByDefault="true"
          >
            <ul>
              <li>
                <router-link :to="{ name: 'provider-clp-your-center' }">
                  Your center's overview
                </router-link>
              </li>
              <li>
                <router-link :to="{ name: 'provider-clp-all-centers' }">
                  All centers
                </router-link>
              </li>
            </ul>
          </Accordion>
        </li>
        <li>
          <router-link :to="{ name: 'provider-genetic-deafness' }">
            Genetic Deafness
          </router-link>
        </li>
        <li>
          <router-link :to="{ name: 'provider-larnyxcleft' }">
            Larynxcleft
          </router-link>
        </li>
      </ul>
    </nav>
    <a id="btnSubmitPatient" href="../tables/#/Subject">
      <span>Submit patient</span>
      <ArrowUpTrayIcon />
    </a>
  </aside>
</template>

<script setup lang="ts">
// @ts-ignore
import { Accordion } from "molgenis-viz";
import { ArrowUpTrayIcon } from "@heroicons/vue/24/outline";
</script>

<style lang="scss">
.sidebar {
  background-color: $gray-000;
  box-sizing: content-box;
  padding: 2em 1.5em;
  box-shadow: $box-shadow;
  border-radius: 8px;

  nav {
    padding: 0;
    margin-top: 1em;
    margin-bottom: 1.5em;

    $link-font-size: 12pt;

    ul,
    ul li ul {
      list-style: none;
      padding: 0;
      margin: 0;
      width: 100%;
    }

    .accordion {
      margin: 0;
      border: none;

      .accordion-heading {
        padding: 0;
        background-color: transparent;

        .toggle-label {
          font-size: $link-font-size;
        }
      }
      .accordion-content {
        padding: 0;
      }
    }

    li {
      margin: 0.6em 0;
      border-left: 3px solid transparent;
      padding-left: 1em;

      a,
      p {
        font-size: $link-font-size;
        margin: 0;
        line-height: 1;
      }

      p {
        @include textTransform;
        margin-top: 24px;
        padding-left: 1.5em;
        color: $gray-400;
      }

      a {
        color: currentColor;

        &:hover,
        &:focus {
          color: $cranio-primary;
        }
      }

      &.link-selected {
        border-left-color: $cranio-primary;
        a {
          color: $cranio-primary;
        }
      }
    }
  }
}

#btnSubmitPatient {
  @include buttonLink;
  @include textTransform;

  color: $cranio-orange-050;
  background-color: $cranio-orange;

  span {
    letter-spacing: 0.15em;
    border-bottom: 2px solid transparent;
  }
  &:hover,
  &:focus {
    text-decoration: none;
    filter: brightness(110%);
  }

  svg {
    position: relative;
    width: 18px;
    margin-left: 4px;
    margin-top: -4px;
    path {
      stroke-width: 2;
    }
  }
}
</style>

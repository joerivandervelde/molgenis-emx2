<template>
  <PageFooter>
    <div class="footer-container width-full">
      <div class="footer-content width-medium">
        <div class="footer-column footer-links">
          <p><strong>Recon4imd</strong></p>
          <UnorderedList listType="none">
            <li>
              <router-link :to="{ name: 'home' }"> Home </router-link>
            </li>
            <li>
              <router-link :to="{ name: 'about' }"> About </router-link>
            </li>
            <li>
              <router-link :to="{ name: 'dashboard' }"> Dashboard </router-link>
            </li>
          </UnorderedList>
        </div>
        <div class="footer-column footer-links">
          <p><strong>For Members</strong></p>
          <UnorderedList listType="none">
            <li>
              <router-link :to="{ name: 'request-access' }">
                Request access
              </router-link>
            </li>
            <li>
              <router-link :to="{ name: 'documents' }"> Documents </router-link>
            </li>
          </UnorderedList>
        </div>
        <div class="footer-column footer-logos">
          <UnorderedList listType="none">
            <li id="project-logo-link">
              <a href="http://recon4imd.org">
                <img
                  src="/img/recon4imd.png"
                  alt="Reconstruction and Computational Modelling for Inherited Metabolic Diseases"
                />
              </a>
            </li>
            <li>
              <a href="https://www.molgenis.org">
                <span class="visually-hidden">
                  visit the molgenis website to learn more
                </span>
                <li>
                  <MolgenisLogo />
                </li>
              </a>
            </li>
          </UnorderedList>
        </div>
      </div>
    </div>
  </PageFooter>
</template>

<script setup lang="ts">
// @ts-ignore
import { PageFooter, UnorderedList, MolgenisLogo } from "molgenis-viz";
</script>

<style lang="scss">
.page-footer {
  background-color: $gray-000;

  .footer-container {
    color: $gray-800;
    background-color: $gray-050;
  }

  .footer-content {
    box-sizing: padding-box;
    padding: 2em 5em;
    display: flex;
    justify-content: flex-start;
    align-items: flex-start;
    flex-direction: row;
    flex-wrap: wrap;
    gap: 2em;
    margin: 0 auto;
  }

  .footer-links {
    position: relative;
    flex-grow: 2;

    a {
      @include linkTextTransform;
      text-decoration: none;
      padding-bottom: 2px;
      border-bottom: 2px solid transparent;

      &:hover,
      &:focus {
        border-bottom-color: currentColor;
      }
    }
    .heroicons {
      position: absolute;
      top: 0;
      margin-left: 3px;
      $size: 15px;
      width: $size;
      height: $size;
    }
  }

  .footer-logos {
    ul {
      gap: 1em;
    }
  }

  .page-footer-meta-links {
    box-sizing: padding-box;
    padding: 0.8em;
    background-color: $blue-200;

    a {
      @include textTransform;
      text-decoration: none;
      padding-bottom: 2px;
      border-bottom: 2px solid transparent;

      &:hover,
      &:focus {
        border-bottom-color: currentColor;
      }
    }
  }

  .molgenis-citation {
    background-color: $gray-000;
  }
}
</style>

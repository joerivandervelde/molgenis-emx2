import { createRouter, createWebHashHistory } from "vue-router";
import { useSettingsStore } from "../stores/settingsStore";
import AboutView from "../views/AboutView.vue";
import BiobankReport from "../views/BiobankReport.vue";
import CollectionReport from "../views/CollectionReport.vue";
import ConfigurationScreen from "../views/ConfigurationScreen.vue";
import HomeView from "../views/HomeView.vue";
import Landingpage from "../views/Landingpage.vue";
import NetworkReport from "../views/NetworkReport.vue";
import ServiceReport from "../views/ServiceReport.vue";
import StudyReport from "../views/StudyReport.vue";

const router = createRouter({
  history: createWebHashHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: "/catalogue",
      name: "catalogue",
      component: HomeView,
    },
    {
      path: "/about",
      name: "about",
      component: AboutView,
    },
    {
      path: "/collection/:id",
      name: "collectiondetails",
      component: CollectionReport,
    },
    {
      path: "/service/:id",
      name: "servicedetails",
      component: ServiceReport,
    },
    {
      path: "/biobank/:id",
      name: "biobankdetails",
      component: BiobankReport,
    },
    { path: "/network/:id", name: "networkdetails", component: NetworkReport },
    {
      path: "/study/:id",
      name: "studydetails",
      component: StudyReport,
    },
    {
      path: "/configuration",
      component: ConfigurationScreen,
      beforeEnter: (_to, _from, next) => {
        const settingsStore = useSettingsStore();
        if (settingsStore.showSettings) {
          next();
        } else {
          next("/");
        }
      },
    },
    {
      path: "/",
      component: Landingpage,
      beforeEnter: async (to, _from, next) => {
        const settingsStore = useSettingsStore();
        await settingsStore.initializeConfig();
        if (
          settingsStore.config.landingpage.enabled &&
          !Object.keys(to.query).length
        ) {
          next();
        } else {
          next({ path: "/catalogue", query: to.query });
        }
      },
    },
  ],
  scrollBehavior() {
    return { top: 0 };
  },
});

export default router;

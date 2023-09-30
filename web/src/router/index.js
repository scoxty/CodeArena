import { createRouter, createWebHistory } from 'vue-router'
import NotFoundView from '../views/error/NotFoundView.vue'
import PkIndexView from '../views/pk/PkIndexView.vue'
import RankListIndexView from '../views/ranklist/RankListIndexView.vue'
import RecordIndexView from '../views/record/RecordIndexView.vue'
import UserBotIndexView from '../views/user/bot/UserBotIndexView.vue'


const routes = [
  {
    path: "/",
    name: "home",
    redirect: "/pk",
  },
  {
    path: "/pk",
    name: "pk_index",
    component: PkIndexView,
  },
  {
    path: "/ranklist",
    name: "ranklist_index",
    component: RankListIndexView,
  },
  {
    path: "/record",
    name: "record_index",
    component: RecordIndexView,
  },
  {
    path: "/user/bot",
    name: "userbot_index",
    component: UserBotIndexView,
  },
  {
    path: "/404",
    name: "404",
    component: NotFoundView,
  },
  {
    path: "/:catchAll(.*)",
    redirect: "/404",
  },
  /*
    从上至下匹配，若都没匹配上则重定向到404
  */
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router

import { createRouter, createWebHistory } from 'vue-router'
import NotFoundView from '../views/error/NotFoundView.vue'
import PkIndexView from '../views/pk/PkIndexView.vue'
import RankListIndexView from '../views/ranklist/RankListIndexView.vue'
import RecordIndexView from '../views/record/RecordIndexView.vue'
import RecordContentView from '../views/record/RecordContentView.vue'
import UserBotIndexView from '../views/user/bot/UserBotIndexView.vue'
import UserAccountRegisterView from '../views/user/account/UserAccountRegisterView.vue'
import UserAccountLoginView from '../views/user/account/UserAccountLoginView.vue'
import IntroductionView from '../views/introduction/IntroductionView.vue'
import store from '../store/index'

const routes = [
  {
    path: "/user/account/login",
    name: "user_account_login",
    component: UserAccountLoginView,
    meta: {
      requestAuth: false,
    },
  },
  {
    path: "/user/account/register",
    name: "user_account_register",
    component: UserAccountRegisterView,
    meta: {
      requestAuth: false,
    },
  },
  {
    path: "/",
    name: "home",
    redirect: "/pk",
    meta: {
      requestAuth: true,
    },
  },
  {
    path: "/pk",
    name: "pk_index",
    component: PkIndexView,
    meta: {
      requestAuth: true,
    },
  },
  {
    path: "/ranklist",
    name: "ranklist_index",
    component: RankListIndexView,
    meta: {
      requestAuth: true,
    },
  },
  {
    path: "/record",
    name: "record_index",
    component: RecordIndexView,
    meta: {
      requestAuth: true,
    },
  },
  {
    path: "/introduction",
    name: "introduction_index",
    component: IntroductionView,
    meta: {
      requestAuth: true,
    }
  },
  {
    path: "/record/:recordId",
    name: "record_content",
    component: RecordContentView,
    meta: {
      requestAuth: true,
    },
  },
  {
    path: "/user/bot",
    name: "userbot_index",
    component: UserBotIndexView,
    meta: {
      requestAuth: true,
    },
  },
  {
    path: "/404",
    name: "404",
    component: NotFoundView,
    meta: {
      requestAuth: false,
    },
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

router.beforeEach((to, from, next) => {
  if (to.meta.requestAuth && !store.state.user.is_login) {
    next({ name: "user_account_login" });
  } else {
    next();
  }
})

export default router

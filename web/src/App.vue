<template>
  <div class="app-container" v-if="!is404">
    <NavBar />
    <div class="content-container">
      <router-view />
    </div>
    <BottomInformation />
  </div>
  <div class="404" v-else>
    <router-view />
  </div>
</template>

<script>
import NavBar from './components/NavBar.vue'
import BottomInformation from './components/BottomInformation.vue'
import "bootstrap/dist/css/bootstrap.min.css"
import "bootstrap/dist/js/bootstrap"

export default {
  components: {
    NavBar,
    BottomInformation,
  },
  computed: {
    is404() {
      // 通过路由的 meta 属性判断是否显示导航栏
      return this.$route.name === '404';
    }
  }
}
</script>

<style>
.app-container {
  /*
    @表示当前项目的根目录
  */
  background-image: url("@/assets/images/background.png");
  background-size: cover;
  /* 或者指定具体尺寸 */
  background-attachment: fixed;

  display: flex;
  flex-direction: column;
  min-height: 100vh;
  /* 使得app-container至少和视窗一样高 */
}

.content-container {
  flex: 1;
  /* 这将确保content-container填满除了NavBar和BottomInformation之外的空间 */
  margin-bottom: 60px;
  /* 为BottomInformation留出空间 */
}
</style>

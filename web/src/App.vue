<template>
  <div class="app-container">
    <NavBar />
    <div class="content-container">
      <router-view />
    </div>
    <BottomInformation />
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
  }
}
</script>

<style>
body {
  /*
    @表示当前项目的根目录
  */
  background-image: url("@/assets/images/background.png");
  background-size: cover;
  /* 或者指定具体尺寸 */
  background-attachment: fixed;
}

.app-container {
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

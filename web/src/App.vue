<template>
  <div>
    <div>Bot的昵称: {{ bot_name }}</div>
    <div>Bot的战力: {{ bot_rating }}</div>
  </div>
  <router-view />
</template>

<script>
import $ from 'jquery';
import { ref } from 'vue';

export default {
  name: "App",
  setup: () => {
    let bot_name = ref("");
    let bot_rating = ref("");

    $.ajax({
      url: "http://127.0.0.1:3000/pk/getbotinfo",
      type: "get",
      success: resp => {
        bot_name.value = resp.name;
        bot_rating.value = resp.rating;
      }
    });

    return {
      bot_name,
      bot_rating
    }
  }
}
</script>

<style>
body {
  /*
    @表示当前项目的根目录
  */
  background-image: url("@/assets/background.png");
  background-size: cover;
}
</style>

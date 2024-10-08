<template>
    <div></div>
</template>

<script>
import router from '@/router/index';
import { useRoute } from 'vue-router';
import { useStore } from 'vuex';
import $ from 'jquery'

export default {
    setup() {
        const myRoute = useRoute();
        const store = useStore();

        $.ajax({
            url: "https://www.scoxty.com/codearena/api/user/account/qq/web/receive_code",
            type: "GET",
            data: {
                code: myRoute.query.code,
                state: myRoute.query.state,
            },
            success: resp => {
                if (resp.result === "success") {
                    localStorage.setItem("jwt_token", resp.jwt_token);
                    localStorage.setItem("jwt_refresh_token", resp.jwt_refresh_token);
                    store.commit("updateToken", resp.jwt_token);
                    store.commit("updateRefreshToken", resp.jwt_refresh_token);
                    router.push({ name: "home" });
                    store.commit("updatePullingInfo", false);
                } else {
                    router.push({ name: "user_account_login" });
                }
            }
        })
    }
}
</script>

<style scoped></style>
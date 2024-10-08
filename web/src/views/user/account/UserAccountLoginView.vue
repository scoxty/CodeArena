<template>
    <ContentField v-if="!$store.state.user.pulling_info">
        <div class="row justify-content-md-center">
            <div class="col-3">
                <form @submit.prevent="login">
                    <div class="mb-3">
                        <label for="username" class="form-label">用户名:</label>
                        <input v-model="username" type="text" class="form-control" id="username" placeholder="请输入用户名">
                    </div>
                    <div class="mb-3">
                        <label for="password" class="form-label">密码:</label>
                        <input v-model="password" type="password" class="form-control" id="password"
                            placeholder="请输入密码">
                    </div>
                    <div class="error_msg"> {{ error_msg }} </div>
                    <button type="submit" class="btn btn-primary">登录</button>
                </form>
                <div style="text-align: center; margin-top: 20px; cursor: pointer;" @click="qq_login">
                    <img width="30" src="../../../assets/images/qq_logo.png">
                    <br>
                    QQ一键登录
                </div>
            </div>
        </div>
    </ContentField>
</template>

<script>
import ContentField from '../../../components/ContentField.vue'
import { useStore } from 'vuex'
import { ref } from 'vue'
import router from '../../../router/index'
import $ from 'jquery'

export default {
    components: {
        ContentField
    },
    setup() {
        const store = useStore();
        let username = ref('');
        let password = ref('');
        let error_msg = ref('');

        const jwt_token = localStorage.getItem("jwt_token");
        const jwt_refresh_token = localStorage.getItem("jwt_refresh_token");
        if (jwt_token != "" && jwt_refresh_token != "") {
            store.commit("updateToken", jwt_token);
            store.commit("updateRefreshToken", jwt_refresh_token);
            store.dispatch("getinfo", {
                success() {
                    router.push({ name: "home" });
                    store.commit("updatePullingInfo", false);
                },
                error(resp) {
                    console.log(resp);
                    store.commit("updatePullingInfo", false);
                }
            })
        } else {
            store.commit("updatePullingInfo", false);
        }

        const login = () => {
            error_msg.value = "";
            store.dispatch("login", {
                username: username.value,
                password: password.value,
                success() {
                    store.dispatch("getinfo", {
                        success() {
                            router.push({ name: 'home' });
                        },
                        error(resp) {
                            console.log(resp);
                        }
                    })
                },
                error() {
                    error_msg.value = "用户名或密码错误!"
                }
            })
        };

        const qq_login = () => {
            $.ajax({
                url: "https://www.scoxty.com/codearena/api/user/account/qq/web/apply_code",
                type: "GET",
                success: resp => {
                    if (resp.result === "success") {
                        window.location.replace(resp.apply_code_url);
                    }
                }
            })
        }

        return {
            username,
            password,
            error_msg,
            login,
            qq_login,
        }
    }
}
</script>

<style scoped>
button {
    width: 100%;
}

div.error_msg {
    color: red;
    text-align: center;
}
</style>
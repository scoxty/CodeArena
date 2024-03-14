import $ from 'jquery'

export default {
    state: {
        id: "",
        username: "",
        photo: "",
        token: "",
        refresh_token: "",
        is_login: false,
        refresh_token_interval: null,
        pulling_info: true, // 是否正在拉去信息
    },
    getters: {
    },
    mutations: {
        updateUser(state, user) {
            state.id = user.id;
            state.username = user.username;
            state.photo = user.photo;
            state.is_login = user.is_login;
        },
        updateToken(state, token) {
            state.token = token;
        },
        updateRefreshToken(state, refresh_token) {
            state.refresh_token = refresh_token;
        },
        logout(state) {
            state.id = "";
            state.username = "";
            state.photo = "";
            state.token = "";
            state.refresh_token = "";
            state.is_login = false;
        },
        updatePullingInfo(state, pulling_info) {
            state.pulling_info = pulling_info;
        },
        clearRefreshTokenInterval(state) {
            state.refresh_token_interval = null;
        },
    },
    actions: {
        login(context, data) {
            $.ajax({
                url: "https://www.scoxty.com/api/user/account/token",
                type: "post",
                data: {
                    username: data.username,
                    password: data.password,
                },
                success(resp) {
                    if (resp.error_msg === "success") {
                        localStorage.setItem("jwt_token", resp.token);
                        localStorage.setItem("jwt_refresh_token", resp.refresh_token);
                        context.commit("updateToken", resp.token);
                        context.commit("updateRefreshToken", resp.refresh_token);
                        data.success(resp);

                        context.state.refresh_token_interval = setInterval(() => {
                            $.ajax({
                                url: "https://www.scoxty.com/api/user/account/refresh_token",
                                type: "POST",
                                data: {
                                    refresh_token: context.state.refresh_token,
                                },
                                success(resp) {
                                    if (resp.error_msg === "success") {
                                        localStorage.setItem("jwt_token", resp.token);
                                        localStorage.setItem("jwt_refresh_token", resp.refresh_token);
                                        context.commit("updateToken", resp.token);
                                        context.commit("updateRefreshToken", resp.refresh_token);
                                    } else {
                                        localStorage.removeItem("jwt_token");
                                        localStorage.removeItem("jwt_refresh_token");
                                        context.commit("logout");
                                        clearInterval(context.state.refresh_token_interval);
                                        context.commit("clearRefreshTokenInterval");
                                    }
                                }
                            })
                        }, 23 * 60 * 60 * 1000);

                    } else {
                        data.error(resp);
                    }
                },
                error(resp) {
                    data.error(resp);
                }
            });
        },
        getinfo(context, data) {
            $.ajax({
                url: "https://www.scoxty.com/api/user/account/info",
                type: "get",
                headers: {
                    Authorization: "Bearer " + context.state.token,
                },
                success(resp) {
                    if (resp.error_msg === "success") {
                        context.commit("updateUser", {
                            ...resp,
                            is_login: true,
                        })
                        data.success();
                    } else {
                        data.error(resp);
                    }
                },
                error(resp) {
                    data.error(resp);
                }
            })
        },
        logout(context) {
            localStorage.removeItem("jwt_token");
            localStorage.removeItem("jwt_refresh_token");
            clearInterval(context.state.refresh_token_interval);
            context.commit("clearRefreshTokenInterval");
            context.commit("logout");
        }
    },
    modules: {
    }
}
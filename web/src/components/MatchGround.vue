<template>
    <div class="matchground">
        <div class="row">
            <div class="col-4">
                <div class="user-photo">
                    <img :src="$store.state.user.photo" alt="">
                </div>
                <div class="user-username">
                    {{ $store.state.user.username }}
                </div>
            </div>
            <div class="col-4">
                <div class="user-select-bot">
                    <select v-model="select_bot" class="form-select" aria-label="Default select example">
                        <option value="-1" selected>亲自上阵</option>
                        <option v-for="bot in bots" :key="bot.id" :value="bot.id">
                            {{ bot.title }}
                        </option>
                    </select>
                </div>
            </div>
            <div class="col-4">
                <div class="user-photo">
                    <img :src="$store.state.pk.opponent_photo" alt="">
                </div>
                <div class="user-username">
                    {{ $store.state.pk.opponent_username }}
                </div>
            </div>
            <div class="col-12 button">
                <button type="button" class="btn btn-secondary btn-lg" @click="click_match_btn">{{ match_btn_info
                }}</button>
            </div>
            <div class="col-12 loaing-board">
                <div>
                    <p class="matchingTime" v-if="match_btn_info === '取消'">已匹配: {{ matchingTime }} 秒</p>
                </div>
                <div class="loader" v-if="match_btn_info === '取消'"></div>
            </div>
            <div class="col-12 tip" v-if="match_btn_info === '取消' && matchingTime > 15">
                <p>当前匹配玩家中与你分值相近的较少，请耐心等待...</p>
            </div>
        </div>
    </div>
</template>

<script>
import store from '@/store';
import { ref, onUnmounted } from 'vue';
import $ from 'jquery'

export default {
    setup() {
        let match_btn_info = ref("开始匹配");
        let bots = ref([]);
        let select_bot = ref("-1");

        // 匹配时间状态
        const matchingTime = ref(0);
        // 定时器ID
        let intervalId = null;

        const refresh_bots = () => {
            $.ajax({
                url: "https://www.scoxty.com/api/user/bot/getlist",
                type: "get",
                headers: {
                    Authorization: "Bearer " + store.state.user.token,
                },
                success(resp) {
                    bots.value = resp;
                }
            })
        }

        refresh_bots();

        const click_match_btn = () => {
            if (match_btn_info.value === "开始匹配") {
                match_btn_info.value = "取消";
                if (store.state.pk.socket === null) {
                    console.log("websocket连接为空!")
                }
                store.state.pk.socket.send(JSON.stringify({
                    event: "start-matching",
                    bot_id: select_bot.value,
                }));
                // 重置匹配时间并开始计时
                matchingTime.value = 0;
                clearInterval(intervalId);
                intervalId = setInterval(() => {
                    matchingTime.value++;
                }, 1000);
            } else {
                match_btn_info.value = "开始匹配";
                store.state.pk.socket.send(JSON.stringify({
                    event: "stop-matching",
                }));
                // 停止计时
                clearInterval(intervalId);
            }
        }

        // 组件卸载时清除定时器
        onUnmounted(() => {
            clearInterval(intervalId);
        });

        return {
            match_btn_info,
            click_match_btn,
            bots,
            select_bot,
            matchingTime,
        }
    }
}
</script>

<style scoped>
div.matchground {
    width: 60vw;
    height: 70vh;
    margin: 40px auto;
    background-color: rgba(50, 50, 50, 0.5);
}

div.user-photo {
    text-align: center;
    padding-top: 10vh;
}

div.user-photo>img {
    border-radius: 50%;
    width: 20vh;
}

div.user-username {
    text-align: center;
    font-size: 24px;
    font-weight: 600;
    color: white;
    padding-top: 2vh;
}

div.button {
    text-align: center;
    padding-top: 15vh;
}

div.user-select-bot {
    padding-top: 20vh;
}

div.user-select-bot>select {
    width: 60%;
    margin: 0 auto;
}

div.loaing-board {
    display: flex;
    justify-content: center;
}

p.matchingTime {
    text-align: right;
    font-size: 15px;
    color: white;
    padding-top: 1vh;
}

div.tip {
    text-align: center;
    font-size: 15px;
    color: white;
}

div.loader {
    width: 15px;
    height: 15px;
    border: 2px solid white;
    border-top-color: transparent;
    border-radius: 100%;
    margin-top: 10px;
    margin-left: 1vh;

    animation: circle infinite 0.75s linear;
}

@keyframes circle {
    0% {
        transform: rotate(0);
    }

    100% {
        transform: rotate(360deg);
    }
}
</style>
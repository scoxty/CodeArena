<template>
    <PlayGround v-if="$store.state.pk.status === 'playing'" />
    <MatchGround v-if="$store.state.pk.status === 'matching'" />
    <ResultBoard v-if="$store.state.pk.loser !== 'none'" />
</template>

<script>
import PlayGround from '../../components/PlayGround.vue'
import MatchGround from '../../components/MatchGround.vue'
import ResultBoard from '../../components/ResultBoard.vue'
import { onMounted, onUnmounted } from 'vue'
import { useStore } from 'vuex'

export default {
    components: {
        PlayGround,
        MatchGround,
        ResultBoard,
    },
    setup() {
        const store = new useStore();
        const socketUrl = `ws://127.0.0.1:3000/websocket/${store.state.user.token}`;

        store.commit("updateIsRecord", false);

        let socket = null;
        onMounted(() => {
            store.commit("updateLoser", "none");

            store.commit("updateOpponent", {
                username: "我的对手",
                photo: "https://ts1.cn.mm.bing.net/th/id/R-C.ef3c5a1355076a8fb4984e04390e701a?rik=z7L2%2bzjyoKOr3Q&riu=http%3a%2f%2ficon.chrafz.com%2fuploads%2fallimg%2f160421%2f1-1604211630040-L.png&ehk=5Tex5iR2ZMOgoGge4XuHOBHlxxecnTlVi9kv5ojWhqg%3d&risl=&pid=ImgRaw&r=0",
            })

            socket = new WebSocket(socketUrl);

            socket.onopen = () => {
                console.log("connected!");
                store.commit("updateSocket", socket)
            }

            socket.onmessage = msg => {
                const data = JSON.parse(msg.data);
                if (data.event === "start-matching") { // 匹配成功
                    store.commit("updateOpponent", {
                        username: data.opponent_username,
                        photo: data.opponent_photo,
                    });
                    store.commit("updateGame", data.game);
                    setTimeout(() => {
                        store.commit("updateStatus", "playing");
                    }, 500);
                } else if (data.event === "move") {
                    const game = store.state.pk.gameObject;
                    const [snake0, snake1] = game.snakes;
                    snake0.set_direction(data.a_direction);
                    snake1.set_direction(data.b_direction);
                } else if (data.event === "result") {
                    const game = store.state.pk.gameObject;
                    const [snake0, snake1] = game.snakes;

                    if (data.loser === "all" || data.loser === "A") {
                        snake0.status = "die";
                    }

                    if (data.loser === "all" || data.loser === "B") {
                        snake1.status = "die";
                    }

                    store.commit("updateLoser", data.loser);
                }
            }

            socket.onclose = () => {
                console.log("disconnected!");
            }
        });

        onUnmounted(() => {
            socket.close();
            store.commit("updateStatus", "matching");
        })
    }
}
</script>

<style scoped></style>
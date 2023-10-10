<template>
    <div class="result-borad">
        <div class="result-board-text" v-if="$store.state.pk.loser === 'all'">
            Draw
        </div>
        <div class="result-board-text"
            v-else-if="$store.state.pk.loser === 'A' && $store.state.pk.a_id == parseInt($store.state.user.id)">
            Lose
        </div>
        <div class="result-board-text"
            v-else-if="$store.state.pk.loser === 'B' && $store.state.pk.b_id == parseInt($store.state.user.id)">
            Lose
        </div>
        <div class="result-board-text" v-else>
            Win
        </div>
        <div class="result-board-btn">
            <button @click="restart" type="button" class="btn btn-warning btn-lg">
                再来一局!
            </button>
        </div>
    </div>
</template>

<script>
import { useStore } from 'vuex'

export default {
    setup() {
        const store = useStore();

        const restart = () => {
            store.commit("updateStatus", "matching");
            store.commit("updateLoser", "none");
            store.commit("updateOpponent", {
                username: "我的对手",
                photo: "https://ts1.cn.mm.bing.net/th/id/R-C.ef3c5a1355076a8fb4984e04390e701a?rik=z7L2%2bzjyoKOr3Q&riu=http%3a%2f%2ficon.chrafz.com%2fuploads%2fallimg%2f160421%2f1-1604211630040-L.png&ehk=5Tex5iR2ZMOgoGge4XuHOBHlxxecnTlVi9kv5ojWhqg%3d&risl=&pid=ImgRaw&r=0",
            })
        }

        return {
            restart,
        };
    }
}
</script>

<style scoped>
div.result-borad {
    height: 30vh;
    width: 30vw;
    background-color: rgba(50, 50, 50, 0.5);
    position: absolute;
    top: 30vh;
    left: 35vw;
}

div.result-board-text {
    text-align: center;
    color: white;
    font-size: 50px;
    font-weight: 600;
    font-style: italic;
    padding-top: 5vh;
}

div.result-board-btn {
    padding-top: 7vh;
    text-align: center;
}
</style>
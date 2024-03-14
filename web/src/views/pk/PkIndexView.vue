<template>
    <PlayGround v-if="$store.state.pk.status === 'playing'" />
    <MatchGround v-if="$store.state.pk.status === 'matching'" />
    <ResultBoard v-if="$store.state.pk.loser !== 'none'" />
    <div class="user-color"
        v-if="$store.state.pk.status === 'playing' && parseInt($store.state.user.id) === parseInt($store.state.pk.a_id)">
        你在左下角
    </div>
    <div class="user-color"
        v-if="$store.state.pk.status === 'playing' && parseInt($store.state.user.id) === parseInt($store.state.pk.b_id)">
        你在右上角
    </div>
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
        const store = useStore();
        const socketUrl = `wss://www.scoxty.com/websocket/${store.state.user.token}`;
        let socket = null;
        // 心跳检测机制以及断线重连机制
        let heartbeatInterval = null; // 发送心跳包周期函数
        let heartbeatTimeout = null; // 心跳超时检测
        let shouldReconnect = false; // 心跳检测失败时重连
        let shouldSynchronize = false; // 重连后进行数据同步
        let reconnectDelayed = null; // 重连尝试延迟
        let reconnectAttempts = 0; // 重连尝试次数
        const MAX_RECONNECT_COUNT = 5; // 最大重连尝试次数
        const heartbeatMsg = JSON.stringify({ event: "ping" });
        const synchronizeMsg = JSON.stringify({ event: "synchronize-data" });


        store.commit("updateIsRecord", false);

        function initializeSocket() {
            // 避免重复连接
            if (socket !== null && (socket.readyState === WebSocket.OPEN || socket.readyState === WebSocket.CONNECTING)) {
                console.log("WebSocket is already open or connecting. Skipping new connection attempt.");
                return;
            }

            socket = new WebSocket(socketUrl);

            socket.onopen = () => {
                console.log("WebSocket connected!");
                store.commit("updateSocket", socket);
                if (reconnectDelayed) { // 如果重连成功则清除周期函数。
                    clearTimeout(reconnectDelayed);
                    reconnectDelayed = null;
                    shouldSynchronize = true;
                }
                reconnectAttempts = 0;
                sendHeartbeat(); // 立刻发一个心跳
                scheduleHeartbeat(); // 开启定时心跳检测
            };

            socket.onmessage = (msg) => {
                const data = JSON.parse(msg.data);

                if (data.event === "pong") {
                    clearTimeout(heartbeatTimeout); // 如果收到服务端的心跳回复则清除计时器
                    if (shouldSynchronize) { // 重连并确保双方能正常通信，则尝试恢复数据
                        socket.send(synchronizeMsg);
                        shouldSynchronize = false;
                    }
                } else if (data.event === "synchronize-data") {
                    store.commit("updateGame", data.game);
                    store.commit("updateStatus", "playing");
                } else if (data.event === "start-matching") { // 匹配成功
                    store.commit("updateOpponent", {
                        username: data.opponent_username,
                        photo: data.opponent_photo,
                    });
                    store.commit("updateGame", data.game);
                    setTimeout(() => {
                        store.commit("updateStatus", "playing");
                    }, 200);
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
            };

            socket.onerror = (error) => {
                console.error("WebSocket error:", error);
            };

            socket.onclose = () => {
                console.log("WebSocket disconnected!");
                if (shouldReconnect) {
                    if (reconnectAttempts < MAX_RECONNECT_COUNT) {
                        scheduleReconnect();
                    } else {
                        console.log("Max reconnect attempts reached. Stopping reconnection.");
                    }
                }
            };
        }

        function scheduleHeartbeat() {
            clearInterval(heartbeatInterval);
            heartbeatInterval = setInterval(sendHeartbeat, 30000); // 每30秒发送一次心跳
        }

        function sendHeartbeat() {
            if (socket.readyState === WebSocket.OPEN) {
                socket.send(heartbeatMsg);
                setHeartbeatTimeout();
            }
        }

        function setHeartbeatTimeout() {
            clearTimeout(heartbeatTimeout);
            heartbeatTimeout = setTimeout(() => {
                console.log("Heartbeat timeout. Closing socket to trigger reconnect.");
                // 触发重连
                shouldReconnect = true;
                socket.close();
            }, 5000); // 5秒内没收到服务端的心跳响应则触发重连
        }

        function scheduleReconnect() {
            clearTimeout(reconnectDelayed);
            if (reconnectAttempts < MAX_RECONNECT_COUNT) {
                reconnectDelayed = setTimeout(() => {
                    console.log(`Attempting to reconnect (Attempt ${reconnectAttempts + 1})...`);
                    initializeSocket();
                    reconnectAttempts++;
                }, calculateReconnectDelay(reconnectAttempts));
            } else {
                console.log("Stopped reconnection attempts.");
            }
        }

        function calculateReconnectDelay(attempts) {
            return Math.min(30, (Math.pow(2, attempts) - 1)) * 1000; // 退避算法，重连上限延迟是30秒
        }

        onMounted(() => {
            store.commit("updateLoser", "none");
            store.commit("updateOpponent", {
                username: "我的对手",
                photo: "https://ts1.cn.mm.bing.net/th/id/R-C.ef3c5a1355076a8fb4984e04390e701a?rik=z7L2%2bzjyoKOr3Q&riu=http%3a%2f%2ficon.chrafz.com%2fuploads%2fallimg%2f160421%2f1-1604211630040-L.png&ehk=5Tex5iR2ZMOgoGge4XuHOBHlxxecnTlVi9kv5ojWhqg%3d&risl=&pid=ImgRaw&r=0",
            })

            shouldReconnect = true;
            initializeSocket(); // 组件挂载时发起WebSocket连接
            window.addEventListener('online', () => { // 监听网络变化情况，网络恢复时进行重连
                console.log("Network online. Attempting to reconnect if necessary.");
                initializeSocket();
            });
        });

        onUnmounted(() => {
            // 组件取消挂载时关闭WebSocket连接
            shouldReconnect = false;
            if (socket) socket.close();
            clearInterval(heartbeatInterval);
            clearTimeout(heartbeatTimeout);
            clearTimeout(reconnectDelayed);

            store.commit("updateStatus", "matching");
        });
    }
}
</script>

<style scoped>
div.user-color {
    text-align: center;
    color: white;
    font-size: 30px;
    font-weight: 600;
}
</style>
<template>
    <nav class="navbar navbar-expand-lg navbar-dark bg-dark">
        <div class="container">
            <!-- 使用<router-link>代替<a>可以让每次请求不必刷新浏览器 -->
            <!-- <a class="navbar-brand active" href="/">Kob</a> -->
            <router-link class="navbar-brand active" :to="{ name: 'home' }">Kob</router-link>
            <div class="collapse navbar-collapse" id="navbarText">
                <ul class="navbar-nav me-auto mb-2 mb-lg-0">
                    <li class="nav-item">
                        <router-link :class="route_name == 'pk_index' ? 'nav-link native' : 'nav-link'"
                            :to="{ name: 'pk_index' }">对战</router-link>
                    </li>
                    <li class="nav-item">
                        <router-link :class="route_name == 'record_index' ? 'nav-link native' : 'nav-link'"
                            :to="{ name: 'record_index' }">对局列表</router-link>
                    </li>
                    <li class="nav-item">
                        <router-link :class="route_name == 'ranklist_index' ? 'nav-link native' : 'nav-link'"
                            :to="{ name: 'ranklist_index' }">排行榜</router-link>
                    </li>
                </ul>
                <ul class="navbar-nav">
                    <li class="nav-item dropdown">
                        <a class="nav-link dropdown-toggle" href="#" role="button" data-bs-toggle="dropdown"
                            aria-expanded="false">
                            scoxty
                        </a>
                        <ul class="dropdown-menu">
                            <li>
                                <router-link class="dropdown-item" :to="{ name: 'userbot_index' }">我的Bots</router-link>
                            </li>
                            <li>
                                <hr class="dropdown-divider">
                            </li>
                            <li>
                                <a class="dropdown-item" href="#">退出</a>
                            </li>
                        </ul>
                    </li>
                </ul>
            </div>
        </div>
    </nav>
</template>

<script>
import { useRoute } from 'vue-router'
import { computed } from 'vue';

export default {
    setup() {
        const route = useRoute();
        let route_name = computed(() => route.name);
        return {
            route_name
        }
    }
}

</script>

<style scoped></style>
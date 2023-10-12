<template>
    <div class="record">
        <div class="record-title">
            对局回放
        </div>
    </div>
    <PlayGround />
</template>

<script>
import PlayGround from '../../components/PlayGround.vue'

export default {
    components: {
        PlayGround,
    },
    setup() {
    }
}
</script>

<style scoped>
div.record {
    box-sizing: border-box;
}

div.record-title {
    padding-top: 2vh;
    text-align: center;
    font-size: 24px;
    font-weight: 600;
    color: white;
}
</style>
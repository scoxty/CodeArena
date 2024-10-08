<!--
 * @Description: 渲染markdown文件
 -->
<template>
    <ContentField>
        <div id="my-markdown" class="markdown-body">
            <MarkdownIt :source="md"></MarkdownIt>
        </div>
    </ContentField>
</template>

<script>
import ContentField from '../../components/ContentField.vue'
import MarkdownIt from 'vue3-markdown-it';
import { ref } from 'vue';
import { useStore } from 'vuex';
import $ from 'jquery'

export default {
    components: {
        MarkdownIt,
        ContentField,
    },
    setup() {
        const store = useStore();
        const md = ref("");

        $.ajax({
            url: "https://www.scoxty.com/codearena/api/introduction",
            type: "get",
            headers: {
                Authorization: "Bearer " + store.state.user.token,
            },
            success(resp) {
                md.value = resp;
            },
        });

        return {
            md,
        }
    }
};
</script>

<style>
@import 'github-markdown-css/github-markdown.css';

.markdown-body {
    box-sizing: border-box;
    margin: 0 auto;
    padding: 0 40px;
}
</style>
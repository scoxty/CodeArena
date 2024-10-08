<template>
    <div class="error-page">
        <div class="error-content">
            <h1>404</h1>
            <p>Oops! The page you're looking for doesn't exist.</p>
            <router-link :to="{ name: 'home' }" class="btn-home">Go back Home</router-link>
        </div>
        <div class="background-animation"></div>
    </div>
</template>

<script>
export default {
    name: "NotFound",
};
</script>

<style scoped>
.error-page {
    display: flex;
    justify-content: center;
    align-items: center;
    height: 100vh;
    background: linear-gradient(135deg, #89f7fe 0%, #66a6ff 100%);
    font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
}

.error-content {
    text-align: center;
    color: #fff;
    z-index: 10;
}

.error-content h1 {
    font-size: 8rem;
    margin: 0;
    animation: bounce 2s infinite;
}

.error-content p {
    font-size: 1.5rem;
    margin: 10px 0 30px;
}

.btn-home {
    text-decoration: none;
    padding: 10px 20px;
    background-color: #fff;
    color: #66a6ff;
    border-radius: 5px;
    transition: background-color 0.3s ease;
}

.btn-home:hover {
    background-color: #66a6ff;
    color: #fff;
}

.background-animation {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background: url('@/assets/images/404-background.jpg') no-repeat center center;
    background-size: cover;
    opacity: 0.1;
}

@keyframes bounce {

    0%,
    20%,
    50%,
    80%,
    100% {
        transform: translateY(0);
    }

    40% {
        transform: translateY(-30px);
    }

    60% {
        transform: translateY(-15px);
    }
}
</style>
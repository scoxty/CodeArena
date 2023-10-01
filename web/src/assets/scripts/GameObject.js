const GAME_OBJECTS = []; // 所有游戏对象

export class GameObject { // 游戏对象的基类
    constructor() {
        GAME_OBJECTS.push(this);
        this.timedelta = 0; // 当前帧距离上一帧的时间间隔
        this.has_called_start = false;
    }

    start() { // 只执行一次

    }

    update() { // 每帧执行一次，出第一次外

    }

    on_destroy() { // 删除前执行

    }

    destroy() {
        this.on_destroy();

        for (let i in GAME_OBJECTS) {
            const obj = GAME_OBJECTS[i];
            if (obj === this) {
                GAME_OBJECTS.splice(i);
                break;
            }
        }
    }
}

// 每帧执行一遍操作
let last_timestamp;
const step = timestamp => {
    for (let obj of GAME_OBJECTS) {
        if (!obj.has_called_start) {
            obj.has_called_start = true;
            obj.start();
        } else {
            obj.timedelta = timestamp - last_timestamp;
            obj.update();
        }
    }

    last_timestamp = timestamp;
    requestAnimationFrame(step) // 迭代
}

requestAnimationFrame(step) // 在下一次浏览器渲染之前执行一遍


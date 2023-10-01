import { GameObject } from "./GameObject";
import { Cell } from "./Ceil";

export class Snake extends GameObject {
    constructor(info, gamemap) {
        super();

        this.id = info.id;
        this.color = info.color;
        this.gamemap = gamemap;

        this.cells = [new Cell(info.r, info.c)]; // 存放蛇的身体，cells[0]存放蛇头
        this.next_cell = null; // 下一步的目的地

        this.speed = 5; // 蛇每秒走5个格子
        this.direction = -1; // -1表示没有指令，0、1、2、3分别表示上、右、下、左。
        this.status = "idle"; // idle表示静止，move表示正在移动，die表示死亡

        this.dr = [-1, 0, 1, 0]; // 行方向的偏移量
        this.dc = [0, 1, 0, -1]; // 列方向的偏移量

        this.step = 0; // 回合数
        this.eps = 1e-2; // 误差

        // 左下角的蛇初始朝上，右上角的蛇初始朝下。
        this.eye_direction = 0;
        if (this.id === 1) {
            this.eye_direction = 2;
        }
        this.eye_dx = [ // 蛇眼睛上右下左的x的偏移量
            [-1, 1],
            [1, 1],
            [1, -1],
            [-1, -1],
        ];
        this.eye_dy = [ // 蛇眼睛上右下左的y的偏移量
            [-1, -1],
            [-1, 1],
            [1, 1],
            [-1, 1],
        ];
    }

    start() {

    }

    set_direction(d) {
        this.direction = d;
    }

    next_step() {
        // 更新蛇的状态为：移动
        const d = this.direction;
        this.eye_direction = d;
        this.next_cell = new Cell(this.cells[0].r + this.dr[d], this.cells[0].c + this.dc[d]);
        this.direction = -1;
        this.status = "move";
        this.step++;

        // 中间不动，移动头部和尾部
        const k = this.cells.length;
        for (let i = k; i > 0; i--) {
            this.cells[i] = JSON.parse(JSON.stringify(this.cells[i - 1]));
        }

        // 若到达非法位置，则更新蛇的状态为：死亡
        if (!this.gamemap.check_valid(this.next_cell)) {
            this.status = "die";
        }
    }

    check_tail_increasing() { // 检测到前蛇的长度是否增加
        // 前10回合会逐渐变长，后面每隔3步变长
        if (this.step <= 10) {
            return true;
        }

        if (this.step % 3 === 1) {
            return true;
        }

        return false;
    }

    update_move() {
        // 每两帧之间走的距离,即两点间的实际距离
        const move_distance = this.speed * this.timedelta / 1000;

        const dx = this.next_cell.x - this.cells[0].x;
        const dy = this.next_cell.y - this.cells[0].y;
        const distance = Math.sqrt(dx * dx + dy * dy);

        if (distance < this.eps) { // 移到目标点
            this.cells[0] = this.next_cell; // 更新蛇头
            this.status = "idle";
            this.next_cell = null;

            if (!this.check_tail_increasing()) { // 剔除蛇尾
                this.cells.pop();
            }

        } else {
            this.cells[0].x += move_distance * dx / distance;
            this.cells[0].y += move_distance * dy / distance;

            if (!this.check_tail_increasing()) { // 蛇长增加时蛇尾不动，不增加时移动。
                const k = this.cells.length;
                const tail = this.cells[k - 1], tail_target = this.cells[k - 2];
                const tail_dx = tail_target.x - tail.x;
                const tail_dy = tail_target.y - tail.y;
                tail.x += move_distance * tail_dx / distance;
                tail.y += move_distance * tail_dy / distance;
            }
        }
    }

    update() {
        if (this.status === 'move') {
            this.update_move();
        }

        this.render();
    }

    render() {
        const L = this.gamemap.L;
        const ctx = this.gamemap.ctx;

        ctx.fillStyle = this.color;
        if (this.status === "die") {
            ctx.fillStyle = "white";
        }

        // 蛇头蛇尾
        for (const cell of this.cells) {
            ctx.beginPath();
            ctx.arc(cell.x * L, cell.y * L, L / 2 * 0.8, 0, 2 * Math.PI);
            ctx.fill();
        }

        // 蛇身
        for (let i = 1; i < this.cells.length; i++) {
            const a = this.cells[i - 1], b = this.cells[i];
            // 重合
            if (Math.abs(a.x - b.x) < this.eps && Math.abs(a.y - b.y) < this.eps) {
                continue;
            }
            // 竖直方向
            if (Math.abs(a.x - b.x) < this.eps) {
                ctx.fillRect((a.x - 0.4) * L, Math.min(a.y, b.y) * L, L * 0.8, Math.abs(a.y - b.y) * L); // 为了蛇身不那么胖，宽L乘以0.8
            } else { // 水平方向
                ctx.fillRect(Math.min(a.x, b.x) * L, (a.y - 0.4) * L, Math.abs(a.x - b.x) * L, L * 0.8);
            }
        }

        // 蛇眼
        ctx.fillStyle = "black";
        for (let i = 0; i < 2; i++) {
            const eye_x = (this.cells[0].x + this.eye_dx[this.eye_direction][i] * 0.15) * L;
            const eye_y = (this.cells[0].y + this.eye_dy[this.eye_direction][i] * 0.15) * L;
            ctx.beginPath();
            ctx.arc(eye_x, eye_y, L * 0.05, 0, Math.PI * 2);
            ctx.fill();
        }

    }
}
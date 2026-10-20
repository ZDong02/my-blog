#!/bin/bash
# =====================================================
# 本地开发环境一键启动
# =====================================================
# 用法：
#   ./start-dev.sh          仅启动前端（Astro，默认）
#   ./start-dev.sh all      同时启动后端 + 前端
#   ./start-dev.sh backend  仅启动后端
#   ./start-dev.sh frontend 仅启动前端
#   ./start-dev.sh stop     停止由本脚本启动的进程
#
# 访问地址：http://localhost:4321
# 管理员账号：admin / admin123
# =====================================================
set -e

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BACKEND_DIR="$ROOT_DIR/blog-backend"
FRONTEND_DIR="$ROOT_DIR/social-factory-blog"
LOG_DIR="$ROOT_DIR/.dev-logs"

mkdir -p "$LOG_DIR"

GREEN='\033[0;32m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
RED='\033[0;31m'
NC='\033[0m'

log_info()  { echo -e "${GREEN}[INFO]${NC} $1"; }
log_warn()  { echo -e "${YELLOW}[WARN]${NC} $1"; }
log_error() { echo -e "${RED}[ERROR]${NC} $1"; }
log_ok()    { echo -e "${CYAN}[OK]${NC} $1"; }

# ---------- 端口检查 ----------
check_port() {
    local port=$1 name=$2
    if netstat -ano 2>/dev/null | grep -q "LISTENING" && \
       netstat -ano 2>/dev/null | grep ":$port" | grep -q "LISTENING"; then
        log_ok "$name 端口 $port 已在监听（可能已启动）"
        return 0
    fi
    return 1
}

# ---------- 启动后端 ----------
start_backend() {
    log_info "启动后端 (Spring Boot, 端口 8080)..."

    if check_port 8080 "后端"; then
        log_warn "8080 已被占用，跳过启动。若非本项目请先停止该进程。"
        return 0
    fi

    cd "$BACKEND_DIR"
    nohup mvn spring-boot:run \
        > "$LOG_DIR/backend.log" 2>&1 &

    log_info "后端启动中，日志：$LOG_DIR/backend.log"
    log_info "等待数据库连接与 Spring 初始化（约 20-40 秒）..."

    for i in $(seq 1 60); do
        sleep 2
        if curl -s -m 3 -o /dev/null "http://localhost:8080/api/captcha/generate" 2>/dev/null; then
            log_ok "后端就绪 → http://localhost:8080/api"
            return 0
        fi
        if ! netstat -ano 2>/dev/null | grep ":8080" | grep -q LISTENING; then
            if grep -q "BUILD FAILURE\|Application run failed" "$LOG_DIR/backend.log" 2>/dev/null; then
                log_error "后端启动失败，请查看：$LOG_DIR/backend.log"
                tail -20 "$LOG_DIR/backend.log"
                return 1
            fi
        fi
    done

    log_warn "等待超时，请手动检查：tail -f $LOG_DIR/backend.log"
    return 1
}

# ---------- 启动前端 ----------
start_frontend() {
    log_info "启动前端 (Astro dev, 端口 4321)..."

    if check_port 4321 "前端"; then
        log_warn "4321 已被占用。若为本项目的旧进程，请先执行：./start-dev.sh stop"
        return 0
    fi

    cd "$FRONTEND_DIR"
    # 本地站点地址设为 localhost，使 SEO/canonical/sitemap 指向本机
    SITE_URL="http://localhost:4321" nohup npm run dev \
        > "$LOG_DIR/frontend.log" 2>&1 &

    log_info "前端启动中，日志：$LOG_DIR/frontend.log"

    for i in $(seq 1 30); do
        sleep 2
        if curl -s -m 3 -o /dev/null "http://localhost:4321/" 2>/dev/null; then
            log_ok "前端就绪 → http://localhost:4321"
            return 0
        fi
    done

    log_warn "等待超时，请手动检查：tail -f $LOG_DIR/frontend.log"
    return 1
}

# ---------- 停止 ----------
stop_all() {
    log_info "停止前端 (4321)..."
    PID=$(netstat -ano 2>/dev/null | grep ":4321" | grep LISTENING | awk '{print $5}' | head -1)
    [ -n "$PID" ] && taskkill //F //PID "$PID" 2>/dev/null && log_ok "前端已停止"

    log_info "停止后端 (8080)..."
    PID=$(netstat -ano 2>/dev/null | grep ":8080" | grep LISTENING | awk '{print $5}' | head -1)
    [ -n "$PID" ] && taskkill //F //PID "$PID" 2>/dev/null && log_ok "后端已停止"
}

# ---------- 依赖检查 ----------
check_deps() {
    command -v java >/dev/null 2>&1 || { log_error "未找到 java，请安装 JDK 17"; exit 1; }
    command -v mvn  >/dev/null 2>&1 || { log_error "未找到 mvn，请安装 Maven"; exit 1; }
    command -v node >/dev/null 2>&1 || { log_error "未找到 node，请安装 Node.js 18+"; exit 1; }
    command -v npm  >/dev/null 2>&1 || { log_error "未找到 npm"; exit 1; }
    log_ok "依赖检查通过"
}

# ---------- 数据库检查 ----------
check_db() {
    log_info "检查 MySQL (localhost:3306)..."
    if netstat -ano 2>/dev/null | grep ":3306" | grep -q LISTENING; then
        log_ok "MySQL 端口 3306 正常监听"
    else
        log_warn "未检测到 MySQL 3306 端口，请确认 MySQL 已启动"
        log_warn "若数据库不存在，请执行 schema.sql（见 README）"
    fi
}

case "${1:-frontend}" in
    all)
        check_deps
        check_db
        echo ""
        start_backend && echo "" && start_frontend
        ;;
    backend)
        check_deps
        check_db
        start_backend
        ;;
    frontend)
        check_deps
        start_frontend
        ;;
    stop)
        stop_all
        ;;
    *)
        echo "用法: ./start-dev.sh [all|backend|frontend|stop]"
        exit 1
        ;;
esac

echo ""
log_info "常用地址："
echo "  前端页面   http://localhost:4321"
echo "  注册页     http://localhost:4321/register"
echo "  后端接口   http://localhost:8080/api"
echo "  管理员     admin / admin123"
echo "  日志目录   $LOG_DIR"

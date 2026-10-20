import { defineConfig } from 'astro/config';
import tailwind from '@astrojs/tailwind';
import rehypePrettyCode from 'rehype-pretty-code';

// 站点地址：默认取下方 site；可用环境变量 SITE_URL 覆盖
// 本地开发时设为 http://localhost:4321，SEO/canonical/sitemap 即指向本机
const SITE_URL = process.env.SITE_URL;

export default defineConfig({
  integrations: [tailwind()],
  output: 'static',
  vite: {
    server: {
      port: 4321,
      // 本地开发：/api 转发到本机后端，前端因此无需处理跨域
      // 用 127.0.0.1 而非 localhost：Node 解析 localhost 会优先取 IPv6 的 ::1，
      // 而 Spring Boot 默认只监听 IPv4，导致代理报 10061 连接被拒绝
      proxy: {
        '/api': 'http://127.0.0.1:8080',
      },
    },
  },
  markdown: {
    syntaxHighlight: 'shiki',
    rehypePlugins: [[rehypePrettyCode, {
      theme: 'one-dark-pro',
      keepBackground: true
    }]],
  },
  // 站点地址：默认指向本机；部署到线上时用环境变量 SITE_URL 覆盖
  site: SITE_URL || 'http://localhost:4321',
});

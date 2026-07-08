import { defineConfig } from 'astro/config';
import tailwind from '@astrojs/tailwind';
import rehypePrettyCode from 'rehype-pretty-code';

export default defineConfig({
  integrations: [tailwind()],
  output: 'static',
  vite: {
    server: {
      proxy: {
        '/api': 'http://localhost:8080',
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
  site: 'https://zdong01.com', // 改成你的域名
});

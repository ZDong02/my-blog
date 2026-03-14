import { defineConfig } from 'astro/config';
import tailwind from '@astrojs/tailwind';
import rehypePrettyCode from 'rehype-pretty-code';

export default defineConfig({
  integrations: [tailwind()],
  markdown: {
    syntaxHighlight: 'shiki',
    rehypePlugins: [[rehypePrettyCode, {
      theme: 'one-dark-pro',
      keepBackground: true
    }]],
  },
  site: 'https://zdong01.com', // 改成你的域名
});

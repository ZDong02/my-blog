import type { APIRoute } from 'astro';

// 站点地址统一取自 astro.config.mjs 的 site 配置
const SITE_URL = import.meta.env.SITE;

export const GET: APIRoute = () => {
  const robotsTxt = `User-agent: *
Allow: /

# Disallow admin and api routes
Disallow: /admin/
Disallow: /admin2026
Disallow: /api/
Disallow: /dashboard
Disallow: /profile
Disallow: /my-likes
Disallow: /my-bookmarks
Disallow: /my-comments

# Sitemap
Sitemap: ${SITE_URL}/sitemap.xml

# Crawl-delay for polite crawling
Crawl-delay: 1`;

  return new Response(robotsTxt, {
    headers: {
      'Content-Type': 'text/plain',
      'Cache-Control': 'public, max-age=86400',
    },
  });
};

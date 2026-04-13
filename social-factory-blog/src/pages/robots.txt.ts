import type { APIRoute } from 'astro';

const SITE_URL = 'https://zdong01.com';

export const GET: APIRoute = () => {
  const robotsTxt = `User-agent: *
Allow: /

# Disallow admin and api routes
Disallow: /admin/
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

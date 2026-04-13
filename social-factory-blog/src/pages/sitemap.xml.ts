import type { APIRoute } from 'astro';

const SITE_URL = 'https://zdong01.com';

// Static pages
const staticPages = [
  { url: '/', priority: '1.0', changefreq: 'daily' },
  { url: '/blog', priority: '0.9', changefreq: 'daily' },
  { url: '/about', priority: '0.7', changefreq: 'monthly' },
  { url: '/roadmap', priority: '0.6', changefreq: 'monthly' },
  { url: '/faq', priority: '0.6', changefreq: 'monthly' },
  { url: '/contact', priority: '0.5', changefreq: 'yearly' },
  { url: '/search', priority: '0.5', changefreq: 'weekly' },
  { url: '/tags', priority: '0.7', changefreq: 'weekly' },
  { url: '/login', priority: '0.3', changefreq: 'yearly' },
  { url: '/register', priority: '0.3', changefreq: 'yearly' },
];

const generateSitemap = () => {
  const urls = staticPages.map(page => `
  <url>
    <loc>${SITE_URL}${page.url}</loc>
    <lastmod>${new Date().toISOString().split('T')[0]}</lastmod>
    <changefreq>${page.changefreq}</changefreq>
    <priority>${page.priority}</priority>
  </url>`).join('');

  return `<?xml version="1.0" encoding="UTF-8"?>
<urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9"
        xmlns:news="http://www.google.com/schemas/sitemap-news/0.9"
        xmlns:image="http://www.google.com/schemas/sitemap-image/1.1">
${urls}
</urlset>`;
};

export const GET: APIRoute = () => {
  const sitemap = generateSitemap();

  return new Response(sitemap, {
    headers: {
      'Content-Type': 'application/xml',
      'Cache-Control': 'public, max-age=3600',
    },
  });
};

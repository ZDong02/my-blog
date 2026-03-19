import rss from '@astrojs/rss';

// RSS feed now uses static data since blog posts are loaded via API
export async function GET(context: { site: string }) {
  return rss({
    title: 'Social Factory',
    description: 'No Favouritism',
    site: context.site,
    items: [
      // Items will be loaded from API at runtime
      // This is a placeholder for the RSS feed structure
    ],
  });
}

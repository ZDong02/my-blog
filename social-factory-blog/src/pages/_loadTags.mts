// Tag loading utilities
import { apiClient } from '../lib/api';

export const renderTags = (tags: any[]) => {
  const container = document.getElementById('tagsGrid');
  if (!container) return;

  let html = '';
  tags.forEach(tag => {
    const postCount = tag.postCount || 0;
    const sizeClass = postCount > 10 ? 'text-lg font-semibold' : postCount > 5 ? 'text-base' : 'text-sm';
    const colorClass = postCount > 10 ? 'bg-red-100 dark:bg-red-900/30 text-red-800 dark:text-red-200' :
                      postCount > 5 ? 'bg-orange-100 dark:bg-orange-900/30 text-orange-800 dark:text-orange-200' :
                      'bg-gray-100 dark:bg-zinc-800 text-gray-700 dark:text-gray-300';

    html += `
      <a href="/tag/${tag.slug}" class="block p-4 rounded-lg hover:shadow-md transition ${colorClass}">
        <div class="font-medium">#${tag.name}</div>
        <div class="text-xs opacity-75 mt-1">${postCount} 篇文章</div>
      </a>
    `;
  });

  container.innerHTML = html;
};

export const renderHotTags = (tags: any[]) => {
  const container = document.getElementById('hotTags');
  if (!container) return;

  let html = '';
  tags.forEach((tag, index) => {
    const postCount = tag.postCount || 0;
    html += `
      <a href="/tag/${tag.slug}" class="px-4 py-2 bg-red-100 dark:bg-red-900/30 text-red-700 dark:text-red-300 rounded-full hover:bg-red-200 dark:hover:bg-red-900/50 transition text-sm">
        #${tag.name}
      </a>
    `;
  });

  container.innerHTML = html;
};

export const loadTags = async () => {
  const loading = document.getElementById('loading');
  const tagsGrid = document.getElementById('tagsGrid');
  const hotTags = document.getElementById('hotTags');

  try {
    const [tagsResponse, hotTagsResponse] = await Promise.all([
      apiClient.getTags(),
      apiClient.getHotTags(15)
    ]);

    if (loading) loading.classList.add('hidden');

    if (tagsResponse.success && tagsResponse.data) {
      renderTags(tagsResponse.data);
    }

    if (hotTagsResponse.success && hotTagsResponse.data) {
      renderHotTags(hotTagsResponse.data);
    }
  } catch (error) {
    console.error('Failed to load tags:', error);
    if (loading) loading.classList.add('hidden');
  }
};

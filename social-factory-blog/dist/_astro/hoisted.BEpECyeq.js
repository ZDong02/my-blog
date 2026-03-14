import"./hoisted.DWgf2xLV.js";import"./Toast.astro_astro_type_script_index_0_lang.DEjscZYH.js";const c=async()=>{const e=document.getElementById("loading"),o=document.getElementById("dashboardContent"),n=document.getElementById("notAuthenticated");if(window.authManager.isLoading){setTimeout(c,100);return}if(!window.authManager.isAuthenticated){e.classList.add("hidden"),n.classList.remove("hidden");return}e.classList.add("hidden"),o.classList.remove("hidden");const t=window.authManager.currentUser;document.getElementById("userDisplayName").textContent=t.nickname||t.username,document.getElementById("userEmail").textContent=t.email,document.getElementById("userRole").textContent=t.role,document.getElementById("userAvatar").src=t.avatar||"/images/default-avatar.png";try{const s=await apiClient.getDashboardStats();if(s.success){const a=s.data;document.getElementById("postsCount").textContent=a.postsCount||0,document.getElementById("likesCount").textContent=a.likesCount||0,document.getElementById("bookmarksCount").textContent=a.bookmarksCount||0,document.getElementById("commentsCount").textContent=a.commentsCount||0}const r=await apiClient.getRecentActivity();r.success&&i(r.data)}catch(s){console.error("Failed to load dashboard data:",s)}document.getElementById("logoutBtn").addEventListener("click",async()=>{await window.authManager.logout(),window.location.href="/"})},i=e=>{const o=document.getElementById("recentActivity");let n="";e.recentLikes&&e.recentLikes.length>0&&e.recentLikes.slice(0,5).forEach(t=>{n+=`
            <div class="flex items-start gap-3 p-3 bg-gray-50 dark:bg-zinc-800 rounded-lg">
              <svg class="h-5 w-5 text-red-600 mt-0.5 flex-shrink-0" fill="currentColor" viewBox="0 0 24 24">
                <path d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z"/>
              </svg>
              <div class="flex-1 min-w-0">
                <p class="text-sm text-gray-900 dark:text-white truncate">Liked post: <strong>${t.post?.title||"Unknown Post"}</strong></p>
                <p class="text-xs text-gray-500 dark:text-gray-400">${new Date(t.createdAt).toLocaleDateString()}</p>
              </div>
            </div>
          `}),e.recentBookmarks&&e.recentBookmarks.length>0&&e.recentBookmarks.slice(0,5).forEach(t=>{n+=`
            <div class="flex items-start gap-3 p-3 bg-gray-50 dark:bg-zinc-800 rounded-lg">
              <svg class="h-5 w-5 text-yellow-600 mt-0.5 flex-shrink-0" fill="currentColor" viewBox="0 0 24 24">
                <path d="M5 5a2 2 0 012-2h10a2 2 0 012 2v16l-7-3.5L5 21V5z"/>
              </svg>
              <div class="flex-1 min-w-0">
                <p class="text-sm text-gray-900 dark:text-white truncate">Bookmarked: <strong>${t.post?.title||"Unknown Post"}</strong></p>
                <p class="text-xs text-gray-500 dark:text-gray-400">${new Date(t.createdAt).toLocaleDateString()}</p>
              </div>
            </div>
          `}),e.recentComments&&e.recentComments.length>0&&e.recentComments.slice(0,5).forEach(t=>{n+=`
            <div class="flex items-start gap-3 p-3 bg-gray-50 dark:bg-zinc-800 rounded-lg">
              <svg class="h-5 w-5 text-green-600 mt-0.5 flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z"/>
              </svg>
              <div class="flex-1 min-w-0">
                <p class="text-sm text-gray-900 dark:text-white truncate">Commented: <em class="line-clamp-1">${t.content}</em></p>
                <p class="text-xs text-gray-500 dark:text-gray-400">${new Date(t.createdAt).toLocaleDateString()}</p>
              </div>
            </div>
          `}),n||(n='<p class="text-gray-500 dark:text-gray-400 text-center py-4">No recent activity yet</p>'),o.innerHTML=n};document.addEventListener("DOMContentLoaded",c);

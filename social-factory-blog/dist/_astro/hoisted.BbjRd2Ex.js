import{a as l}from"./api.C1xNrEEc.js";import"./AdminLayout.astro_astro_type_script_index_0_lang.ZsBwA2go.js";import"./Toast.astro_astro_type_script_index_0_lang.DEjscZYH.js";const d=async()=>{if(window.authManager.isLoading){setTimeout(d,100);return}const t=document.getElementById("loading"),e=document.getElementById("notAuthenticated"),s=document.getElementById("dashboardContent");if(!window.authManager.isAuthenticated){t.classList.add("hidden"),e.classList.remove("hidden");return}if(window.authManager.currentUser.role!=="ADMIN"){window.location.href="/dashboard",showWarning("Access denied. Admin only.");return}t.classList.add("hidden"),s.classList.remove("hidden"),c()},c=async()=>{try{const t=await l.request("/posts/my-posts");if(t.success&&t.data){const e=t.data,s=e.length,n=e.filter(a=>a.status==="PUBLISHED").length,o=e.filter(a=>a.status==="DRAFT").length,r=e.reduce((a,i)=>a+(i.viewCount||0),0);document.getElementById("totalPosts").textContent=s,document.getElementById("publishedPosts").textContent=n,document.getElementById("draftPosts").textContent=o,document.getElementById("totalViews").textContent=r.toLocaleString(),u(e.slice(0,5))}}catch(t){console.error("Failed to load dashboard data:",t)}},u=t=>{const e=document.getElementById("recentPosts");if(t.length===0){e.innerHTML='<p class="text-gray-500 dark:text-gray-400 text-center py-4">No posts yet. Create your first post!</p>';return}let s="";t.forEach(n=>{const o=n.status==="PUBLISHED"?"bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200":"bg-yellow-100 text-yellow-800 dark:bg-yellow-900 dark:text-yellow-200";s+=`
          <div class="flex items-center justify-between p-3 bg-gray-50 dark:bg-zinc-800 rounded-lg">
            <div class="flex items-center gap-3 flex-1 min-w-0">
              ${n.featuredImage?`
                <div class="w-12 h-12 rounded overflow-hidden flex-shrink-0">
                  <img src="${n.featuredImage}" class="w-full h-full object-cover" />
                </div>
              `:`
                <div class="w-12 h-12 bg-gray-200 dark:bg-zinc-700 rounded flex items-center justify-center flex-shrink-0">
                  <svg class="h-6 w-6 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 20H5a2 2 0 01-2-2V6a2 2 0 012-2h10a2 2 0 012 2v1m2 13a2 2 0 01-2-2V7m2 13a2 2 0 002-2V9a2 2 0 00-2-2h-2m-4-3H9M7 16h6M7 8h6v4H7V8z"/>
                  </svg>
                </div>
              `}
              <div class="flex-1 min-w-0">
                <h4 class="text-sm font-medium text-gray-900 dark:text-white truncate">${n.title}</h4>
                <p class="text-xs text-gray-500 dark:text-gray-400">${new Date(n.createdAt).toLocaleDateString()} • ${n.viewCount||0} views</p>
              </div>
            </div>
            <span class="px-2.5 py-0.5 rounded-full text-xs font-medium ${o}">${n.status}</span>
          </div>
        `}),e.innerHTML=s};document.addEventListener("DOMContentLoaded",d);

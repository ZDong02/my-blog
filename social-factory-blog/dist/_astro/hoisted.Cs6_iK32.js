import{a as g}from"./api.C1xNrEEc.js";import"./AdminLayout.astro_astro_type_script_index_0_lang.ZsBwA2go.js";import"./Toast.astro_astro_type_script_index_0_lang.DEjscZYH.js";let d=[];const l=async()=>{if(window.authManager.isLoading){setTimeout(l,100);return}if(!window.authManager.isAuthenticated){window.location.href="/login";return}if(window.authManager.currentUser.role!=="ADMIN"){window.location.href="/dashboard",showWarning("Access denied. Admin only.");return}h()},h=async()=>{try{const t=await g.request("/posts/my-posts");t.success&&t.data&&(d=t.data,c(d))}catch(t){console.error("Failed to load posts:",t),showError("Failed to load posts")}},c=t=>{const n=document.getElementById("postsTable"),a=document.getElementById("postsTableBody"),s=document.getElementById("loading"),r=document.getElementById("noPosts"),o=document.getElementById("pagination");if(s.classList.add("hidden"),t.length===0){r.classList.remove("hidden"),n.classList.add("hidden"),o.classList.add("hidden");return}r.classList.add("hidden"),n.classList.remove("hidden"),o.classList.remove("hidden");let i="";t.forEach(e=>{const u=e.status==="PUBLISHED"?"bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200":"bg-yellow-100 text-yellow-800 dark:bg-yellow-900 dark:text-yellow-200";i+=`
          <tr class="hover:bg-gray-50 dark:hover:bg-zinc-800 transition">
            <td class="px-6 py-4">
              <div class="flex items-center">
                <div class="flex-shrink-0 h-10 w-16 bg-gray-200 dark:bg-zinc-700 rounded overflow-hidden">
                  ${e.featuredImage?`<img src="${e.featuredImage}" class="h-full w-full object-cover" />`:""}
                </div>
                <div class="ml-4">
                  <div class="text-sm font-medium text-gray-900 dark:text-white">${e.title}</div>
                  <div class="text-sm text-gray-500 dark:text-gray-400">By ${e.author?.username}</div>
                </div>
              </div>
            </td>
            <td class="px-6 py-4 whitespace-nowrap">
              <span class="text-sm text-gray-600 dark:text-gray-400">${e.category?.name||"Uncategorized"}</span>
            </td>
            <td class="px-6 py-4 whitespace-nowrap">
              <span class="px-2.5 py-0.5 rounded-full text-xs font-medium ${u}">${e.status}</span>
            </td>
            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500 dark:text-gray-400">
              <span title="Views">${e.viewCount||0}</span> ·
              <span title="Likes">${e.likeCount||0}</span> ·
              <span title="Comments">${e.commentCount||0}</span>
            </td>
            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500 dark:text-gray-400">
              ${new Date(e.createdAt).toLocaleDateString()}
            </td>
            <td class="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
              <a href="/blog/${e.id}" class="text-blue-600 hover:text-blue-900 dark:text-blue-400 dark:hover:text-blue-300 mr-3">View</a>
              <a href="/admin/posts/edit/${e.id}" class="text-green-600 hover:text-green-900 dark:text-green-400 dark:hover:text-green-300 mr-3">Edit</a>
              <button onclick="deletePost(${e.id})" class="text-red-600 hover:text-red-900 dark:text-red-400 dark:hover:text-red-300">Delete</button>
            </td>
          </tr>
        `}),a.innerHTML=i},m=()=>{const t=document.getElementById("searchInput").value.toLowerCase(),n=document.getElementById("statusFilter").value;let a=d;t&&(a=a.filter(s=>s.title.toLowerCase().includes(t)||s.summary&&s.summary.toLowerCase().includes(t))),n&&(a=a.filter(s=>s.status===n)),c(a)};document.getElementById("searchInput")?.addEventListener("input",m);document.getElementById("statusFilter")?.addEventListener("change",m);document.addEventListener("DOMContentLoaded",l);

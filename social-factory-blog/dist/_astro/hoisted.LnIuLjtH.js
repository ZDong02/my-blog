import{a as v}from"./api.C1xNrEEc.js";import"./hoisted.DWgf2xLV.js";import"./Toast.astro_astro_type_script_index_0_lang.DEjscZYH.js";let t=1,d=1;const m=async()=>{if(window.authManager.isLoading){setTimeout(m,100);return}const s=document.getElementById("loading"),n=document.getElementById("notAuthenticated");if(!window.authManager.isAuthenticated){s.classList.add("hidden"),n.classList.remove("hidden");return}s.classList.add("hidden"),i()},i=async()=>{const s=document.getElementById("bookmarksList"),n=document.getElementById("noBookmarks"),l=document.getElementById("pagination");try{const a=await v.getMyBookmarks(t,10);if(a.success&&a.data){const{content:r,total:g,size:u,number:h}=a.data;if(t=h,d=Math.ceil(g/u),r.length===0){s.classList.add("hidden"),l.classList.add("hidden"),n.classList.remove("hidden");return}n.classList.add("hidden"),s.classList.remove("hidden"),l.classList.remove("hidden");let c="";r.forEach(o=>{const e=o.post;e&&(c+=`
              <div class="bg-white dark:bg-zinc-900 shadow rounded-lg overflow-hidden">
                <div class="flex">
                  ${e.featuredImage?`
                    <div class="w-48 h-32 flex-shrink-0">
                      <img src="${e.featuredImage}" class="w-full h-full object-cover" />
                    </div>
                  `:""}
                  <div class="flex-1 p-4">
                    <div class="flex items-start justify-between">
                      <div class="flex-1">
                        <div class="text-red-600 text-xs uppercase mb-2">${e.category?.name||"Uncategorized"}</div>
                        <a href="/blog/${e.id}" class="text-lg font-medium text-gray-900 dark:text-white hover:text-red-600 line-clamp-1">${e.title}</a>
                        <p class="text-sm text-gray-600 dark:text-gray-400 line-clamp-2 mt-2">${e.summary||e.content?.substring(0,150)||""}</p>
                        <div class="flex items-center gap-4 mt-4 text-xs text-gray-500 dark:text-gray-400">
                          <span>${new Date(o.createdAt).toLocaleDateString()}</span>
                          <span class="flex items-center gap-1">
                            <svg class="h-4 w-4" fill="currentColor" viewBox="0 0 24 24">
                              <path d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z"/>
                            </svg>
                            ${e.likeCount||0}
                          </span>
                          <span class="flex items-center gap-1">
                            <svg class="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z"/>
                            </svg>
                            ${e.commentCount||0}
                          </span>
                        </div>
                      </div>
                      <button onclick="removeBookmark(${o.id}, ${e.id})" class="ml-4 text-gray-400 hover:text-red-600 transition p-2">
                        <svg class="h-5 w-5" fill="currentColor" viewBox="0 0 24 24">
                          <path d="M6 2h12a2 2 0 012 2v18l-8-4-8 4V4a2 2 0 012-2z"/>
                        </svg>
                      </button>
                    </div>
                  </div>
                </div>
              </div>
            `)}),s.innerHTML=c,document.getElementById("pageInfo").textContent=`Page ${t} of ${d}`,document.getElementById("prevBtn").disabled=t<=1,document.getElementById("nextBtn").disabled=t>=d}}catch(a){console.error("Failed to load bookmarks:",a),showError("Failed to load bookmarks")}};document.getElementById("prevBtn")?.addEventListener("click",()=>{t>1&&(t--,i())});document.getElementById("nextBtn")?.addEventListener("click",()=>{t<d&&(t++,i())});document.addEventListener("DOMContentLoaded",m);

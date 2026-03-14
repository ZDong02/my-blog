import{a as p}from"./api.C1xNrEEc.js";import"./hoisted.DWgf2xLV.js";import"./Toast.astro_astro_type_script_index_0_lang.DEjscZYH.js";let t=1,d=1;const r=async()=>{if(window.authManager.isLoading){setTimeout(r,100);return}const n=document.getElementById("loading"),a=document.getElementById("notAuthenticated");if(!window.authManager.isAuthenticated){n.classList.add("hidden"),a.classList.remove("hidden");return}n.classList.add("hidden"),i()},i=async()=>{const n=document.getElementById("likesList"),a=document.getElementById("noLikes"),o=document.getElementById("pagination");try{const s=await p.getMyLikes(t,10);if(s.success&&s.data){const{content:l,total:m,size:g,number:h}=s.data;if(t=h,d=Math.ceil(m/g),l.length===0){n.classList.add("hidden"),o.classList.add("hidden"),a.classList.remove("hidden");return}a.classList.add("hidden"),n.classList.remove("hidden"),o.classList.remove("hidden");let c="";l.forEach(u=>{const e=u.post;e&&(c+=`
              <a href="/blog/${e.id}" class="block bg-white dark:bg-zinc-900 shadow rounded-lg overflow-hidden hover:shadow-lg transition">
                <div class="flex">
                  ${e.featuredImage?`
                    <div class="w-48 h-32 flex-shrink-0">
                      <img src="${e.featuredImage}" class="w-full h-full object-cover" />
                    </div>
                  `:""}
                  <div class="flex-1 p-4">
                    <div class="text-red-600 text-xs uppercase mb-2">${e.category?.name||"Uncategorized"}</div>
                    <h3 class="text-lg font-medium text-gray-900 dark:text-white line-clamp-1">${e.title}</h3>
                    <p class="text-sm text-gray-600 dark:text-gray-400 line-clamp-2 mt-2">${e.summary||e.content?.substring(0,150)||""}</p>
                    <div class="flex items-center gap-4 mt-4 text-xs text-gray-500 dark:text-gray-400">
                      <span>${new Date(e.createdAt).toLocaleDateString()}</span>
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
                </div>
              </a>
            `)}),n.innerHTML=c,document.getElementById("pageInfo").textContent=`Page ${t} of ${d}`,document.getElementById("prevBtn").disabled=t<=1,document.getElementById("nextBtn").disabled=t>=d}}catch(s){console.error("Failed to load likes:",s),showError("Failed to load liked posts")}};document.getElementById("prevBtn")?.addEventListener("click",()=>{t>1&&(t--,i())});document.getElementById("nextBtn")?.addEventListener("click",()=>{t<d&&(t++,i())});document.addEventListener("DOMContentLoaded",r);

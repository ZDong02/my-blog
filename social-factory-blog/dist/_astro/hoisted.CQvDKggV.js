import{a as x}from"./api.C1xNrEEc.js";import"./hoisted.DWgf2xLV.js";import"./Toast.astro_astro_type_script_index_0_lang.DEjscZYH.js";let e=1,o=1;const g=async()=>{if(window.authManager.isLoading){setTimeout(g,100);return}const n=document.getElementById("loading"),a=document.getElementById("notAuthenticated");if(!window.authManager.isAuthenticated){n.classList.add("hidden"),a.classList.remove("hidden");return}n.classList.add("hidden"),r()},r=async()=>{const n=document.getElementById("commentsList"),a=document.getElementById("noComments"),c=document.getElementById("pagination");try{const s=await x.getMyComments(e,10);if(s.success&&s.data){const{content:l,total:u,size:h,number:p}=s.data;if(e=p,o=Math.ceil(u/h),l.length===0){n.classList.add("hidden"),c.classList.add("hidden"),a.classList.remove("hidden");return}a.classList.add("hidden"),n.classList.remove("hidden"),c.classList.remove("hidden");let m="";l.forEach(t=>{const d=t.post;m+=`
              <div class="bg-white dark:bg-zinc-900 shadow rounded-lg p-6">
                <div class="flex items-start justify-between mb-3">
                  <div class="flex items-center gap-2">
                    <span class="text-xs text-gray-500 dark:text-gray-400">
                      ${new Date(t.createdAt).toLocaleDateString("zh-TW",{year:"numeric",month:"long",day:"numeric",hour:"2-digit",minute:"2-digit"})}
                    </span>
                  </div>
                  <a href="/blog/${d?.id}#comment-${t.id}" class="text-xs text-red-600 hover:text-red-700 flex items-center gap-1">
                    View Post
                    <svg class="h-3 w-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7"/>
                    </svg>
                  </a>
                </div>

                ${d?`
                  <div class="mb-3">
                    <a href="/blog/${d.id}" class="text-sm font-medium text-gray-700 dark:text-gray-300 hover:text-red-600">
                      ${d.title}
                    </a>
                  </div>
                `:""}

                <div class="border-l-4 border-red-200 dark:border-red-800 pl-4 py-2">
                  <p class="text-gray-700 dark:text-gray-300">${t.content}</p>
                </div>

                ${t.replies&&t.replies.length>0?`
                  <div class="mt-4 pl-4 border-l-2 border-gray-200 dark:border-zinc-700 space-y-3">
                    ${t.replies.map(i=>`
                      <div class="text-sm">
                        <span class="text-gray-500 dark:text-gray-400">${i.user?.nickname||i.user?.username}</span>
                        <span class="mx-2 text-gray-400">·</span>
                        <span class="text-xs text-gray-500">${new Date(i.createdAt).toLocaleDateString()}</span>
                        <p class="mt-1 text-gray-700 dark:text-gray-300">${i.content}</p>
                      </div>
                    `).join("")}
                  </div>
                `:""}
              </div>
            `}),n.innerHTML=m,document.getElementById("pageInfo").textContent=`Page ${e} of ${o}`,document.getElementById("prevBtn").disabled=e<=1,document.getElementById("nextBtn").disabled=e>=o}}catch(s){console.error("Failed to load comments:",s),showError("Failed to load comments")}};document.getElementById("prevBtn")?.addEventListener("click",()=>{e>1&&(e--,r())});document.getElementById("nextBtn")?.addEventListener("click",()=>{e<o&&(e++,r())});document.addEventListener("DOMContentLoaded",g);

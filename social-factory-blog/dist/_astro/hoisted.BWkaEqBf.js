import{a as p}from"./api.C1xNrEEc.js";import"./hoisted.DWgf2xLV.js";import"./Toast.astro_astro_type_script_index_0_lang.DEjscZYH.js";let s=1,u=1,a="",v=null;const c=async(e,i=1)=>{if(!e||e.trim().length===0){x();return}a=e.trim();const l=document.getElementById("loading"),t=document.getElementById("initialState"),m=document.getElementById("noResults"),r=document.getElementById("searchResults"),d=document.getElementById("pagination");t.classList.add("hidden"),m.classList.add("hidden"),r.classList.add("hidden"),d.classList.add("hidden"),l.classList.remove("hidden");try{const n=await p.getPosts(i,20);if(n.success&&n.data){const{content:g,total:f,size:E,number:L}=n.data;s=L,u=Math.ceil(f/E);const y=g.filter(o=>o.title.toLowerCase().includes(a.toLowerCase())||o.summary&&o.summary.toLowerCase().includes(a.toLowerCase())||o.content&&o.content.toLowerCase().includes(a.toLowerCase()));if(l.classList.add("hidden"),y.length===0){m.classList.remove("hidden"),document.getElementById("searchTerm").textContent=a;return}r.classList.remove("hidden"),d.classList.remove("hidden"),w(y),document.getElementById("pageInfo").textContent=`Page ${s} of ${u}`,document.getElementById("prevBtn").disabled=s<=1,document.getElementById("nextBtn").disabled=s>=u}}catch(n){console.error("Search failed:",n),l.classList.add("hidden"),showError("Search failed. Please try again.")}},w=e=>{const i=document.getElementById("searchResults");let l="";e.forEach(t=>{const m=a.split(" ").filter(n=>n.length>0);let r=t.title,d=t.summary||t.content?.substring(0,200)||"";m.forEach(n=>{const g=new RegExp(`(${n})`,"gi");r=r.replace(g,'<mark class="bg-yellow-200 dark:bg-yellow-800 px-1 rounded">$1</mark>'),d=d.replace(g,'<mark class="bg-yellow-200 dark:bg-yellow-800 px-1 rounded">$1</mark>')}),l+=`
          <a href="/blog/${t.id}" class="block bg-white dark:bg-zinc-900 shadow rounded-lg p-6 hover:shadow-lg transition">
            <div class="flex gap-4">
              ${t.featuredImage?`
                <div class="w-32 h-24 flex-shrink-0 rounded-lg overflow-hidden">
                  <img src="${t.featuredImage}" class="w-full h-full object-cover" />
                </div>
              `:""}
              <div class="flex-1 min-w-0">
                <div class="text-red-600 text-xs uppercase mb-2">${t.category?.name||"Uncategorized"}</div>
                <h3 class="text-xl font-semibold text-gray-900 dark:text-white line-clamp-1">${r}</h3>
                <p class="text-sm text-gray-600 dark:text-gray-400 line-clamp-2 mt-2">${d}${d.length>=200?"...":""}</p>
                <div class="flex items-center gap-4 mt-3 text-xs text-gray-500 dark:text-gray-400">
                  <span>${new Date(t.createdAt).toLocaleDateString()}</span>
                  <span class="flex items-center gap-1">
                    <svg class="h-3 w-3" fill="currentColor" viewBox="0 0 24 24">
                      <path d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z"/>
                    </svg>
                    ${t.likeCount||0}
                  </span>
                  <span class="flex items-center gap-1">
                    <svg class="h-3 w-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z"/>
                    </svg>
                    ${t.commentCount||0}
                  </span>
                </div>
              </div>
            </div>
          </a>
        `}),i.innerHTML=l},x=()=>{document.getElementById("initialState").classList.remove("hidden"),document.getElementById("loading").classList.add("hidden"),document.getElementById("searchResults").classList.add("hidden"),document.getElementById("noResults").classList.add("hidden"),document.getElementById("pagination").classList.add("hidden")};document.getElementById("searchInput")?.addEventListener("input",e=>{clearTimeout(v),v=setTimeout(()=>{c(e.target.value,1)},500)});document.getElementById("searchBtn")?.addEventListener("click",()=>{const e=document.getElementById("searchInput").value;c(e,1)});document.getElementById("searchInput")?.addEventListener("keypress",e=>{if(e.key==="Enter"){const i=e.target.value;c(i,1)}});document.getElementById("prevBtn")?.addEventListener("click",()=>{s>1&&c(a,s-1)});document.getElementById("nextBtn")?.addEventListener("click",()=>{s<u&&c(a,s+1)});const I=new URLSearchParams(window.location.search),h=I.get("q");h&&(document.getElementById("searchInput").value=h,c(h,1));

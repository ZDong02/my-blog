import{a as x}from"./api.C1xNrEEc.js";import"./hoisted.DWgf2xLV.js";import"./Toast.astro_astro_type_script_index_0_lang.DEjscZYH.js";let t=1,i=1,g=new Set;const c=async(o=1)=>{const n=document.getElementById("loading"),d=document.getElementById("posts"),s=document.getElementById("noPosts"),l=document.getElementById("pagination");try{n.classList.remove("hidden"),d.classList.add("hidden"),s.classList.add("hidden"),l.classList.add("hidden");const a=await x.getPosts(o,10);if(a.success&&a.data){const{content:r,total:h,size:u,number:v}=a.data;if(t=v,i=Math.ceil(h/u),r.length===0){n.classList.add("hidden"),s.classList.remove("hidden");return}let m="";r.forEach(e=>{const f=new Date(e.createdAt).toLocaleDateString("zh-TW"),y=Math.ceil((e.content?.length||0)/200);e.category?.name&&g.add(e.category.name),m+=`
              <a href="/blog/${e.id}" class="block group">
                <div class="flex gap-8">
                  <div class="w-40 h-28 bg-zinc-200 dark:bg-zinc-800 rounded overflow-hidden flex-shrink-0">
                    ${e.featuredImage?`<img src="${e.featuredImage}" class="w-full h-full object-cover" />`:""}
                  </div>
                  <div class="flex-1">
                    <div class="text-red-600 text-xs uppercase">${e.category?.name||"Uncategorized"}</div>
                    <h2 class="text-3xl font-medium group-hover:text-red-600 transition">${e.title}</h2>
                    <p class="text-zinc-500 mt-3 line-clamp-2">${e.summary||e.content?.substring(0,200)||""}</p>
                    <div class="mt-4 text-xs text-zinc-400">
                      ${f} • ${y} min read
                    </div>
                  </div>
                </div>
              </a>
            `}),d.innerHTML=m,n.classList.add("hidden"),d.classList.remove("hidden"),l.classList.remove("hidden"),document.getElementById("pageInfo").textContent=`Page ${t} of ${i}`,document.getElementById("prevBtn").disabled=t<=1,document.getElementById("nextBtn").disabled=t>=i,L()}}catch(a){console.error("Failed to load posts:",a),n.classList.add("hidden"),s.classList.remove("hidden"),s.querySelector("p").textContent="Failed to load posts. Please try again later."}},L=()=>{const o=document.getElementById("categories");let n="";g.forEach(d=>{n+=`
          <button onclick="filterByCategory('${d.replace(/'/g,"\\'")}')"
                  class="px-4 py-1.5 bg-zinc-100 dark:bg-zinc-800 rounded-full text-sm hover:bg-red-100 dark:hover:bg-red-900 transition">
            ${d}
          </button>
        `}),o.innerHTML=n};document.getElementById("prevBtn")?.addEventListener("click",()=>{t>1&&c(t-1)});document.getElementById("nextBtn")?.addEventListener("click",()=>{t<i&&c(t+1)});document.addEventListener("DOMContentLoaded",()=>{c(1)});

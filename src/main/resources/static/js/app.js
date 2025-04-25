document.addEventListener("DOMContentLoaded", () => {
    // header scroll effect
    const header = document.getElementById("site-header");
    window.addEventListener("scroll", () => {
        header.classList.toggle("bg-white", window.scrollY > 10);
        header.classList.toggle("backdrop-blur-sm", window.scrollY > 10);
        header.classList.toggle("shadow-sm", window.scrollY > 10);
    });

    // mobile nav toggle
    document.getElementById("mobile-menu-btn").addEventListener("click", () => {
        document.getElementById("mobile-menu").classList.toggle("hidden");
    });

    // back to top
    const backBtn = document.getElementById("back-to-top");
    window.addEventListener("scroll", () => {
        backBtn.classList.toggle("visible", window.scrollY > 300);
    });
    backBtn.addEventListener("click", () => window.scrollTo({ top: 0, behavior: "smooth" }));

    // document search
    const searchInput = document.getElementById("searchInput");
    const grid = document.getElementById("documentGrid");
    if (searchInput && grid) {
        searchInput.addEventListener("input", () => {
            const q = searchInput.value.trim().toLowerCase();
            grid.querySelectorAll(".document-card").forEach(card => {
                const t = card.querySelector("h3").textContent.toLowerCase();
                const i = card.querySelector(".flex span").textContent.toLowerCase();
                card.style.display = (t.includes(q) || i.includes(q)) ? "" : "none";
            });
        });
    }

    // file upload
    const dz = document.getElementById("file-drop-zone");
    const fi = document.getElementById("fileInput");
    const br = document.getElementById("browseBtn");
    const MAX = 10*1024*1024;
    const ALLOWED = ["application/pdf","application/msword","application/vnd.openxmlformats-officedocument.wordprocessingml.document","application/vnd.ms-excel","application/vnd.openxmlformats-officedocument.spreadsheetml.sheet","image/jpeg","image/png"];

    function validate(f) {
        if (f.size > MAX) { alert("File too large"); return false; }
        if (!ALLOWED.includes(f.type)) { alert("Invalid type"); return false; }
        return true;
    }
    function handleFiles(files){
        if (!files.length) return;
        const f=files[0]; if (!validate(f)) return;
        console.log("Ready to upload",f.name);
    }
    dz.addEventListener("dragover",e=>{e.preventDefault(); dz.classList.add("drag-over");});
    dz.addEventListener("dragleave",e=>{dz.classList.remove("drag-over");});
    dz.addEventListener("drop",e=>{e.preventDefault(); dz.classList.remove("drag-over"); handleFiles(e.dataTransfer.files);});
    br.addEventListener("click",()=>fi.click());
    fi.addEventListener("change",e=>handleFiles(e.target.files));

    // simple FAQ accordion
    window.toggleAccordion = id => {
        const el = document.getElementById(id);
        if (el) el.classList.toggle("hidden");
    };
});

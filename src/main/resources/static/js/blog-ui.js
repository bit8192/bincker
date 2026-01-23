(function () {
    function ready(fn) {
        if (document.readyState === "loading") {
            document.addEventListener("DOMContentLoaded", fn, { once: true });
        } else {
            fn();
        }
    }

    function initReveal() {
        const targets = Array.from(document.querySelectorAll("[data-animate]"));
        if (!targets.length) return;

        const io = new IntersectionObserver(
            (entries) => {
                for (const entry of entries) {
                    if (entry.isIntersecting) {
                        entry.target.classList.add("is-in");
                        io.unobserve(entry.target);
                    }
                }
            },
            { rootMargin: "80px 0px -10% 0px", threshold: 0.01 }
        );

        for (const el of targets) io.observe(el);
    }

    function initProgress() {
        const bar = document.getElementById("blog-progress-bar");
        if (!bar) return;

        function update() {
            const doc = document.documentElement;
            const max = doc.scrollHeight - doc.clientHeight;
            const ratio = max <= 0 ? 0 : Math.min(1, Math.max(0, doc.scrollTop / max));
            bar.style.transform = "scaleX(" + ratio.toFixed(4) + ")";
        }

        update();
        window.addEventListener("scroll", update, { passive: true });
        window.addEventListener("resize", update);
    }

    function initToc() {
        const article = document.getElementById("blog-article");
        const toc = document.getElementById("blog-toc");
        if (!article || !toc) return;

        const headings = Array.from(article.querySelectorAll("h2, h3"));
        if (!headings.length) {
            const wrap = toc.closest(".blog-toc");
            if (wrap) wrap.remove();
            return;
        }

        const items = headings.map((h, idx) => {
            if (!h.id) h.id = "h-" + (idx + 1);
            const a = document.createElement("a");
            a.href = "#" + h.id;
            a.textContent = (h.textContent || "").trim() || ("标题 " + (idx + 1));
            a.dataset.target = h.id;
            if (h.tagName.toLowerCase() === "h3") {
                a.style.paddingLeft = "1.15rem";
                a.style.opacity = "0.92";
            }
            return a;
        });

        toc.replaceChildren(...items);

        toc.addEventListener("click", (e) => {
            const a = e.target.closest("a");
            if (!a) return;
            const id = a.dataset.target;
            const el = id ? document.getElementById(id) : null;
            if (!el) return;
            e.preventDefault();
            el.scrollIntoView({ behavior: "smooth", block: "start" });
            history.replaceState(null, "", "#" + id);
        });

        const offsets = headings.map((h) => ({
            id: h.id,
            get top() {
                return h.getBoundingClientRect().top + window.scrollY;
            },
        }));

        function setActive(id) {
            for (const a of items) a.classList.toggle("is-active", a.dataset.target === id);
        }

        function updateActive() {
            const y = window.scrollY + 120;
            let active = offsets[0].id;
            for (const it of offsets) {
                if (it.top <= y) active = it.id;
                else break;
            }
            setActive(active);
        }

        updateActive();
        window.addEventListener("scroll", updateActive, { passive: true });
        window.addEventListener("resize", updateActive);
    }

    ready(function () {
        initReveal();
        initProgress();
        initToc();
    });
})();

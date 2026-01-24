(function(){
    document.addEventListener("DOMContentLoaded", function () {
        const path = window.location.pathname || "/";
        let navKey = "";
        if (path === "/" || path.indexOf("/post/") === 0) {
            navKey = "home";
        } else if (path.indexOf("/works") === 0) {
            navKey = "works";
        } else if (path.indexOf("/about") === 0) {
            navKey = "about";
        }
        if (navKey) {
            const active = document.querySelector('[data-nav="' + navKey + '"]');
            if (active) {
                active.classList.add("active");
            }
        }
    })
})()
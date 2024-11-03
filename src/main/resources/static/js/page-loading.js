(function(){
    function createContainer(){
        const container = document.createElement("div");
        Object.assign(container.style, {
            display: "inline-block",
            position: "fixed",
            zIndex: 1000,
            top: 0,
            width: "100vw",
            height: "100vh",
            backgroundColor: "rgb(43, 48, 53)",
            opacity: "1",
            transition: "opacity 1s",
            overflow: "hidden",
        });
        const content = document.createElement("div");
        Object.assign(content.style, {
            width: "15%",
            margin: "calc(100vh * 0.6) auto 0"
        });
        content.append(createLoadingLogo());
        container.append(content);
        document.documentElement.append(container);
        return container;
    }
    function createLoadingLogo() {
        const container = document.createElement("div");
        Object.assign(container.style, {textAlign: "center"});
        const logo = document.createElement("div");
        Object.assign(logo.style, {
            display: "inline-block",
            height: "0.8rem",
            width: "100%",
            backgroundColor: "rgb(60,88,125)",
            backgroundImage: "linear-gradient(45deg,rgba(255,255,255,.15) 25%,transparent 25%,transparent 50%,rgba(255,255,255,.15) 50%,rgba(255,255,255,.15) 75%,transparent 75%,transparent)",
            backgroundSize: "1rem 1rem",
            borderRadius: ".5rem",
            animation: "1s linear infinite progress-bar-stripes",
        });
        const msg = document.createElement("span");
        msg.textContent = "加载中...";
        Object.assign(msg.style, {
            color: "rgb(94,95,99)",
        });
        container.append(logo);
        container.append(msg);
        return container;
    }
    const loading = createContainer();
    window.addEventListener("load", ()=>{
        loading.style.opacity = "0";
        loading.addEventListener("transitionend", loading.remove);
    })
    window.addEventListener("unload", ()=>{
        document.documentElement.append(loading);
        loading.style.opacity = "1";
    });
})();
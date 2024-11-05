(function(){
    function createStyle(){
        const style = document.createElement("style");
        // language=CSS
        style.innerHTML = `
            @keyframes page-loading {
                0% {
                    background-position-x: 1rem;
                }
            }
            #page-loading {
                display: inline-block;
                position: fixed;
                z-index: 1000;
                top: 0;
                width: 100vw;
                height: 100vh;
                background-color: #212529;
                opacity: 1;
                transition: opacity .4s;
                overflow: hidden;
            }
            #page-loading-container{
                width: 15%;
                margin: calc(100vh * 0.6) auto 0;
                color: rgb(94,95,99);
            }
            #page-loading-container>div{
                text-align: center;
            }
            #page-loading-logo{
                display: inline-block;
                height: 0.8rem;
                width: 100%;
                background-color: rgb(60,88,125);
                background-image: linear-gradient(45deg,rgba(255,255,255,.15) 25%,transparent 25%,transparent 50%,rgba(255,255,255,.15) 50%,rgba(255,255,255,.15) 75%,transparent 75%,transparent);
                background-size: 1rem 1rem;
                border-radius: .5rem;
                animation: 1s linear infinite page-loading;
            }
        `;
        document.head.appendChild(style);
        return style;
    }
    function createContainer(){
        const pageLoading = document.createElement("div");
        pageLoading.id = "page-loading";
        const container = document.createElement("div");
        container.id = "page-loading-container";
        container.append(createLoadingLogo());
        pageLoading.append(container);
        document.documentElement.append(pageLoading);
        return pageLoading;
    }
    function createLoadingLogo() {
        const container = document.createElement("div");
        const logo = document.createElement("div");
        logo.id = "page-loading-logo"
        const msg = document.createElement("span");
        msg.textContent = "加载中...";
        container.append(logo);
        container.append(msg);
        return container;
    }
    const style = createStyle();
    const loading = createContainer();
    async function loaded() {
        await new Promise(resolve => setTimeout(resolve, 500));
        loading.style.opacity = "0";
        loading.addEventListener("transitionend", ()=>{
            loading.remove();
            style.remove();
        });
        window.removeEventListener("load", loaded);
    }
    window.addEventListener("load",loaded);
})();
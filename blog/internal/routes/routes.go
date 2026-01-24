package routes

import (
	"html/template"
	"log"
	"net/http"
	"net/url"
	"path"
	"path/filepath"
	"strings"

	"blog/internal/content"
	"blog/internal/handlers"

	"github.com/gin-contrib/multitemplate"
	"github.com/gin-gonic/gin"
	"gorm.io/gorm"
)

type MyRouter struct {
	router  *gin.Engine
	render  *multitemplate.Renderer
	funcMap template.FuncMap
}

// Register 注册路由与模板
func Register(router *gin.Engine, store *content.Store, database *gorm.DB) {
	router.SetFuncMap(template.FuncMap{
		"safeHTML": func(value string) template.HTML {
			return template.HTML(value)
		},
	})
	render := multitemplate.NewRenderer()
	myRouter := MyRouter{router, &render, template.FuncMap{
		"safeHTML": func(value string) template.HTML {
			return template.HTML(value)
		},
	}}

	handler := handlers.Handler{Store: store, Db: database}
	myRouter.registerPage("/", handler.Home, "index.html")
	myRouter.registerPage("/about", handler.About, "about.html")
	myRouter.registerPage("/works", handler.WorksList, "works.html")
	myRouter.registerPage("/works/:slug", handler.WorkDetail, "work_detail.html")
	myRouter.registerPage("/post/*slug", staticHandler("./post", handler.PostDetail), "post.html")
	router.HTMLRender = render

	router.GET("/static/*filepath", staticHandler("./static", handlers.NotFound))
	router.HEAD("/static/*filepath", staticHandler("./static", handlers.NotFound))
}

func (m MyRouter) registerPage(relativePath string, handler gin.HandlerFunc, template ...string) {
	m.router.GET(relativePath, handler)
	if len(template) < 1 {
		log.Fatal("must provide at least one template")
	}
	templates := []string{
		"templates/layout.html",
		"templates/page_transition.html",
	}
	for _, t := range template {
		templates = append(templates, "templates/"+t)
	}
	(*m.render).AddFromFilesFuncs(template[0], m.funcMap, templates...)
}

func staticHandler(root string, f gin.HandlerFunc) gin.HandlerFunc {
	allowedExt := map[string]struct{}{
		".html": {},
		".jpg":  {},
		".png":  {},
		".svg":  {},
		".css":  {},
		".js":   {},
		".gz":   {},
		".xz":   {},
		".zip":  {},
	}

	return func(c *gin.Context) {
		if c.Request.Method != http.MethodGet && c.Request.Method != http.MethodHead {
			f(c)
			return
		}

		requestPath := strings.TrimPrefix(c.Request.URL.Path, strings.TrimPrefix(root, "."))
		cleaned := path.Clean("/" + requestPath)
		ext := strings.ToLower(path.Ext(cleaned))
		if _, ok := allowedExt[ext]; !ok {
			f(c)
			return
		}

		if !allowSameSite(c.Request.Host, c.Request.Referer(), c.Request.Header.Get("Origin")) {
			c.Status(http.StatusForbidden)
			return
		}

		c.Header("Cross-Origin-Resource-Policy", "same-origin")
		c.Header("X-Content-Type-Options", "nosniff")

		target := filepath.Join(root, filepath.FromSlash(strings.TrimPrefix(cleaned, "/")))
		if !strings.HasPrefix(target, filepath.Clean(root)+string(filepath.Separator)) {
			f(c)
			return
		}

		c.File(target)
	}
}

func allowSameSite(host, referer, origin string) bool {
	if referer != "" {
		if u, err := url.Parse(referer); err == nil {
			if !sameHost(host, u.Host) {
				return false
			}
		}
	}
	if origin != "" {
		if u, err := url.Parse(origin); err == nil {
			if !sameHost(host, u.Host) {
				return false
			}
		}
	}
	return true
}

func sameHost(expected, got string) bool {
	expectedHost := stripPort(expected)
	gotHost := stripPort(got)
	return strings.EqualFold(expectedHost, gotHost)
}

func stripPort(host string) string {
	if idx := strings.Index(host, ":"); idx != -1 {
		return host[:idx]
	}
	return host
}

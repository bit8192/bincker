package routes

import (
	"html/template"
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
	myRouter.registerPage("/", "index.html", handler.Home)
	myRouter.registerPage("/about", "about.html", handler.About)
	myRouter.registerPage("/works", "works.html", handler.WorksList)
	myRouter.registerPage("/works/:slug", "work_detail.html", handler.WorkDetail)
	myRouter.registerPage("/post/*slug", "post.html", staticHandler("./post", handler.PostDetail))
	router.HTMLRender = render

	router.GET("/static/*filepath", staticHandler("./static", handlers.NotFound))
	router.HEAD("/static/*filepath", staticHandler("./static", handlers.NotFound))
}

func (m MyRouter) registerPage(relativePath string, template string, handler gin.HandlerFunc) {
	m.router.GET(relativePath, handler)
	(*m.render).AddFromFilesFuncs(template, m.funcMap, "templates/layout.html", "templates/"+template)
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
	}

	return func(c *gin.Context) {
		if c.Request.Method != http.MethodGet && c.Request.Method != http.MethodHead {
			f(c)
			return
		}

		requestPath := strings.TrimPrefix(c.Request.URL.Path, "/static")
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

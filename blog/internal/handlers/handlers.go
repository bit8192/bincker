package handlers

import (
	"net/http"
	"strings"
	"time"

	"blog/internal/content"

	"github.com/gin-gonic/gin"
	"gorm.io/gorm"
)

// Handler 处理器依赖
type Handler struct {
	Store *content.Store
	Db    *gorm.DB
}

// PageData 模板数据
type PageData struct {
	Title       string
	Description string
	Posts       []content.Post
	Post        content.Post
	Now         time.Time
}

// Home 主页文章列表
func (h Handler) Home(c *gin.Context) {
	posts := make([]content.Post, 0, len(h.Store.Posts))
	for _, post := range h.Store.Posts {
		if !post.Private {
			posts = append(posts, post)
		}
	}
	c.HTML(http.StatusOK, "index.html", PageData{
		Title:       "首页",
		Description: "欢迎来到 Bincker 的个人博客，这里分享编程、技术和生活的点滴。",
		Posts:       posts,
		Now:         time.Now(),
	})
}

// PostDetail 文章详情页
func (h Handler) PostDetail(c *gin.Context) {
	slug := strings.TrimPrefix(c.Param("slug"), "/")
	post, ok := h.Store.ByKey[slug]
	if !ok {
		c.String(http.StatusNotFound, "文章不存在")
		return
	}

	c.HTML(http.StatusOK, "post.html", PageData{
		Title:       post.Title,
		Description: post.Summary,
		Post:        post,
		Now:         time.Now(),
	})
}

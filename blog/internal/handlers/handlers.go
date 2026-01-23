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

// Work 作品数据
type Work struct {
	Slug     string
	Title    string
	Summary  string
	Category string
	Year     string
	Tags     []string
	Role     string
}

// PageData 模板数据
type PageData struct {
	Title       string
	Description string
	Posts       []content.Post
	Post        content.Post
	Works       []Work
	Work        Work
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

// About 关于页面
func (h Handler) About(c *gin.Context) {
	c.HTML(http.StatusOK, "about.html", PageData{
		Title:       "关于",
		Description: "关于Bincker博客系统的设计理念与实现方式。",
		Now:         time.Now(),
	})
}

// WorksList 作品列表
func (h Handler) WorksList(c *gin.Context) {
	works := sampleWorks()
	c.HTML(http.StatusOK, "works.html", PageData{
		Title:       "作品",
		Description: "作品列表，展示系统设计与交互探索。",
		Works:       works,
		Now:         time.Now(),
	})
}

// WorkDetail 作品详情
func (h Handler) WorkDetail(c *gin.Context) {
	slug := strings.TrimPrefix(c.Param("slug"), "/")
	works := sampleWorks()
	for _, work := range works {
		if work.Slug == slug {
			c.HTML(http.StatusOK, "work_detail.html", PageData{
				Title:       work.Title,
				Description: work.Summary,
				Work:        work,
				Works:       works,
				Now:         time.Now(),
			})
			return
		}
	}
	c.String(http.StatusNotFound, "作品不存在")
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

func sampleWorks() []Work {
	return []Work{
		{
			Slug:     "nebula-notes",
			Title:    "Nebula Notes",
			Summary:  "以模块化写作流程为核心的笔记系统，强调节奏与视觉层级。",
			Category: "内容系统",
			Year:     "2024",
			Role:     "产品设计 / 交互",
			Tags:     []string{"写作流", "结构化", "暗色界面"},
		},
		{
			Slug:     "quiet-archive",
			Title:    "Quiet Archive",
			Summary:  "为长文档构建的归档体验，重点在可检索与时间轴。",
			Category: "信息架构",
			Year:     "2023",
			Role:     "体验策略 / 视觉",
			Tags:     []string{"档案", "时间轴", "检索"},
		},
		{
			Slug:     "signal-dashboard",
			Title:    "Signal Dashboard",
			Summary:  "面向个人系统的决策看板，强调信号与行动。",
			Category: "数据呈现",
			Year:     "2023",
			Role:     "数据设计",
			Tags:     []string{"指标", "节奏", "洞察"},
		},
	}
}

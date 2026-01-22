package main

import (
	"blog/internal/db"
	"blog/internal/middleware"
	"log"

	"blog/internal/content"
	"blog/internal/routes"

	"github.com/gin-gonic/gin"
)

func main() {
	database, err := db.InitSQLite("blog.db")
	if err != nil {
		log.Fatalf("初始化数据库失败: %v", err)
	}

	store, err := content.LoadFromDir("post")
	if err != nil {
		log.Fatalf("加载文章失败: %v", err)
	}

	router := gin.Default()
	err = router.SetTrustedProxies(nil)
	if err != nil {
		log.Fatal("设置信任代理失败", err)
	}

	router.Use(middleware.AuthPrivatePath)

	routes.Register(router, store, database)

	if err := router.Run(); err != nil {
		log.Fatalf("启动服务失败: %v", err)
	}
}

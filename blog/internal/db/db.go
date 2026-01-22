package db

import (
	"os"
	"path/filepath"

	"blog/internal/models"

	"gorm.io/driver/sqlite"
	"gorm.io/gorm"
)

// InitSQLite 初始化 SQLite 数据库并完成迁移
func InitSQLite(path string) (*gorm.DB, error) {
	if err := os.MkdirAll(filepath.Dir(path), 0o755); err != nil {
		return nil, err
	}

	database, err := gorm.Open(sqlite.Open(path+"?_fk=1"), &gorm.Config{})
	if err != nil {
		return nil, err
	}

	if err := database.AutoMigrate(&models.Post{}); err != nil {
		return nil, err
	}

	return database, nil
}

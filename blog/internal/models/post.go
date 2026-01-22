package models

import "time"

// Post 文章模型
type Post struct {
	ID              uint   `gorm:"primaryKey"`
	Title           string `gorm:"size:200;not null"`
	Slug            string `gorm:"size:200;uniqueIndex"`
	Summary         string `gorm:"size:300"`
	ContentMarkdown string `gorm:"type:text;not null"`
	ContentHTML     string `gorm:"type:text;not null"`
	CreatedAt       time.Time
	UpdatedAt       time.Time
}

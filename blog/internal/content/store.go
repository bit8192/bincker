package content

import (
	"blog/internal/middleware"
	"io/fs"
	"os"
	"path/filepath"
	"sort"
	"strings"
	"time"

	"blog/internal/services"
)

// Post 文章内容
type Post struct {
	Title       string
	Path        string
	Summary     string
	ContentHTML string
	ModifiedAt  time.Time
	Private     bool
}

// Store 内存文章仓库
type Store struct {
	Posts []Post
	ByKey map[string]Post
}

// LoadFromDir 扫描目录加载 Markdown 文章
func LoadFromDir(root string) (*Store, error) {
	var posts []Post
	byKey := make(map[string]Post)

	err := filepath.WalkDir(root, func(path string, entry fs.DirEntry, walkErr error) error {
		if walkErr != nil {
			return walkErr
		}
		if entry.IsDir() {
			return nil
		}
		if filepath.Ext(entry.Name()) != ".md" {
			return nil
		}

		info, err := entry.Info()
		if err != nil {
			return err
		}

		markdownBytes, err := os.ReadFile(path)
		if err != nil {
			return err
		}

		markdown := string(markdownBytes)
		title, summary := extractMeta(markdown, entry.Name())
		html, err := services.RenderMarkdown(markdown)
		if err != nil {
			return err
		}

		relative, err := filepath.Rel(root, path)
		if err != nil {
			return err
		}
		key := strings.TrimSuffix(filepath.ToSlash(relative), ".md")

		post := Post{
			Title:       title,
			Path:        key,
			Summary:     summary,
			ContentHTML: html,
			ModifiedAt:  info.ModTime(),
			Private:     middleware.MathPrivatePath(path),
		}
		posts = append(posts, post)
		byKey[key] = post
		return nil
	})
	if err != nil {
		return nil, err
	}

	sort.Slice(posts, func(i, j int) bool {
		return posts[i].ModifiedAt.After(posts[j].ModifiedAt)
	})

	return &Store{
		Posts: posts,
		ByKey: byKey,
	}, nil
}

func extractMeta(markdown, fallback string) (string, string) {
	lines := strings.Split(markdown, "\n")
	title := ""
	summaryLines := []string{}
	capturing := false

	for _, line := range lines {
		trimmed := strings.TrimSpace(line)
		if strings.HasPrefix(trimmed, "# ") && title == "" {
			title = strings.TrimSpace(strings.TrimPrefix(trimmed, "# "))
			capturing = true
			continue
		}
		if strings.HasPrefix(trimmed, "## ") && capturing {
			break
		}
		if capturing && trimmed != "" {
			summaryLines = append(summaryLines, trimmed)
		}
	}

	if title == "" {
		title = strings.TrimSuffix(fallback, filepath.Ext(fallback))
	}

	summary := strings.Join(summaryLines, " ")
	summary = stripMarkdown(summary)
	summary = strings.TrimSpace(summary)
	runes := []rune(summary)
	if len(runes) > 120 {
		summary = string(runes[:120]) + "..."
	}
	return title, summary
}

func stripMarkdown(input string) string {
	replacer := strings.NewReplacer("*", "", "_", "", "`", "", ">", "", "#", "")
	return replacer.Replace(input)
}

package services

import (
	"bytes"

	"github.com/yuin/goldmark"
	"github.com/yuin/goldmark/extension"
	"github.com/yuin/goldmark/parser"
	"github.com/yuin/goldmark/renderer/html"
)

// RenderMarkdown 将 Markdown 渲染成 HTML 字符串
func RenderMarkdown(source string) (string, error) {
	engine := goldmark.New(
		goldmark.WithExtensions(
			extension.GFM,
			extension.Table,
			extension.TaskList,
			extension.Strikethrough,
		),
		goldmark.WithParserOptions(
			parser.WithAutoHeadingID(),
		),
		goldmark.WithRendererOptions(
			html.WithUnsafe(),
		),
	)

	var buffer bytes.Buffer
	if err := engine.Convert([]byte(source), &buffer); err != nil {
		return "", err
	}

	return buffer.String(), nil
}

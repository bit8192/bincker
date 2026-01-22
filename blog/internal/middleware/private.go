package middleware

import (
	"log/slog"
	"net/http"
	"regexp"

	"github.com/gin-gonic/gin"
)

func AuthPrivatePath(c *gin.Context) {
	if MathPrivatePath(c.Request.RequestURI) {
		c.Status(http.StatusUnauthorized)
		c.Abort()
		return
	}
}

func MathPrivatePath(path string) bool {
	match, err := regexp.MatchString("(/|^)private(/|$)", path)
	if err != nil {
		slog.Error("invalid regex pattern", "error", err)
	}
	return err != nil || match
}

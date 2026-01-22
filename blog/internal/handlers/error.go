package handlers

import (
	"net/http"

	"github.com/gin-gonic/gin"
)

func NotFound(c *gin.Context) {
	c.Status(http.StatusNotFound)
}

package main

import (
	"bytes"
	"fmt"
	"image"
	"image/png"
	"io"
	"net/http"
	"os"
	"path/filepath"
	"strconv"
	"strings"

	"github.com/disintegration/imaging"
)

const (
	maxSize   = 1024
	thumbSize = 64
)

func xorUIN(uin string) int {
	res := 0
	for _, c := range uin {
		res ^= int(c)
	}
	return res
}

func saveImage(uin string, img image.Image) error {
	os.MkdirAll("data", 0755)
	path := filepath.Join("data", uin+".png")
	f, err := os.Create(path)
	if err != nil {
		return err
	}
	defer f.Close()
	return png.Encode(f, img)
}

func loadImage(uin string) (image.Image, error) {
	path := filepath.Join("data", uin+".png")
	return imaging.Open(path)
}

func resizeIfNeeded(img image.Image, limit int) image.Image {
	if img.Bounds().Dx() <= limit && img.Bounds().Dy() <= limit {
		return img
	}
	return imaging.Fit(img, limit, limit, imaging.Lanczos)
}

func handleUpload(w http.ResponseWriter, r *http.Request) {
	uin := r.URL.Query().Get("uin")
	checkStr := r.URL.Query().Get("check")
	if uin == "" || checkStr == "" {
		http.Error(w, "missing params", http.StatusBadRequest)
		return
	}
	check, _ := strconv.Atoi(checkStr)
	if xorUIN(uin) != check {
		http.Error(w, "invalid check", http.StatusForbidden)
		return
	}
	data, err := io.ReadAll(http.MaxBytesReader(w, r.Body, 5<<20))
	if err != nil {
		http.Error(w, "read error", http.StatusBadRequest)
		return
	}
	img, _, err := image.Decode(bytes.NewReader(data))
	if err != nil {
		http.Error(w, "decode error", http.StatusBadRequest)
		return
	}
	img = resizeIfNeeded(img, maxSize)
	if err = saveImage(uin, img); err != nil {
		http.Error(w, "save error", http.StatusInternalServerError)
		return
	}
	fmt.Fprintln(w, "ok")
}

func handleAvatar(w http.ResponseWriter, r *http.Request) {
	uin := strings.TrimPrefix(r.URL.Path, "/avatar/")
	if uin == "" {
		http.NotFound(w, r)
		return
	}
	img, err := loadImage(uin)
	if err != nil {
		http.NotFound(w, r)
		return
	}
	if r.URL.Query().Get("hq") != "1" {
		img = imaging.Fit(img, thumbSize, thumbSize, imaging.Lanczos)
	}
	w.Header().Set("Content-Type", "image/png")
	png.Encode(w, img)
}

func main() {
	http.HandleFunc("/upload", handleUpload)
	http.HandleFunc("/avatar/", handleAvatar)
	fmt.Println("Server running on :80")
	http.ListenAndServe(":80", nil)
}

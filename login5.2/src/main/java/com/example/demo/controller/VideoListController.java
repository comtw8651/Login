package com.example.demo.controller;

// 匯入 Spring 框架元件
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

// 匯入 DAO 與模型
import com.example.demo.dao.VideoDAO;
import com.example.demo.entity.Video;

import java.util.List;

/**
 * VideoListController：
 * 提供前端查詢影片清單的 API
 * 路徑：GET /api/videoList
 * 可選擇排序方式（例如：title、published、videoId）
 * 回傳資料為 JSON 陣列（List<Video> 自動序列化）
 */
@RestController // 宣告為 REST 控制器，自動轉成 JSON 回傳
@RequestMapping("/api") // 所有路徑皆以 /api 開頭
public class VideoListController {

    @Autowired // 自動注入 VideoDAO 實作
    private VideoDAO videoDAO;

    /**
     * 取得影片清單（可選擇排序欄位）
     * @param sortBy 可選參數，可指定排序欄位，例如：title / published / videoId
     * @return List<Video>（Spring Boot 自動轉成 JSON 陣列）
     */
    @GetMapping("/videoList")
    public List<Video> getVideoList(@RequestParam(required = false) String sortBy) {
        // 若有指定排序欄位，則呼叫排序版本
        if (sortBy != null && !sortBy.isBlank()) {
            return videoDAO.getAllVideosSorted(sortBy);
        } else {
            // 否則回傳無排序版本
            return videoDAO.getAllVideos();
        }
    }
}

package com.example.demo.entity; // 依你的要求放在此套件下

import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

/**
 * ✅ 影片模型類別：封裝影片的屬性，對應資料庫中的 `videos` 表格。
 * 常用於 JPA 持久化操作、資料庫操作與應用程式前端的資料傳遞。
 */
@Entity // 標記為 JPA 實體類別
@Table(name = "videos") // 對應資料庫中的 `videos` 表
public class Video {

    // ✅ 主鍵欄位，自動編號 (對應資料庫主鍵 ID)
    // 使用 Long 型態作為主鍵更為通用和推薦，並設定為自動遞增
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long viedeo_id;

    // ✅ YouTube 影片 ID（例如："dQw4w9WgXcQ"）
    // 通常 YouTube ID 會有固定長度，可以設定長度限制
    @Column(name = "youtube_id", length = 100)
    private String youtubeId;

    // ✅ 影片標題（例如："介紹台中美食"）
    @Column(nullable = false, length = 255) // 不可為空，限制長度為 255
    private String title;

    // ✅ 影片描述文字（說明欄內容）
    @Column(columnDefinition = "TEXT") // 使用 TEXT 類型支援較長的描述內容
    private String description;

    // ✅ 影片播放網址（可直接用於播放）
    // 注意：這裡統一為影片的直接播放 URL，不再是自動生成的 YouTube iframe URL
    @Column(name = "video_url", length = 500)
    private String videoUrl;

    // ✅ YouTube 自動產生的縮圖 URL（可顯示預覽圖）
    // 通常縮圖 URL 也會比較長
    @Column(name = "thumbnail_url", columnDefinition = "TEXT")
    private String thumbnailUrl;

    // ✅ 影片發佈時間 (ISO 格式，從 String 改為 LocalDateTime 以便操作)
    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    // ✅ 影片類型，例如 "general" (通用), "education" (教育) 等
    @Column(length = 50)
    private String type = "general"; // 提供預設值 "general"

    // ✅ 影片主題或類別
    @Column(length = 100)
    private String subject;

    // ✅ 影片總長度（秒）
    @Column(name = "duration_seconds")
    private Integer durationSeconds;

    // ✅ 影片記錄的創建時間 (通常在新增時自動設定)
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    // ✅ One-to-Many 關係：一個影片可以有多個對話 (ChatSession)
    // mappedBy 指向 ChatSession 中擁有 Video 關係的欄位名稱
    @OneToMany(mappedBy = "video")
    private List<ChatSession> chatSessions;

    // ✅ 無參數建構子（JPA 必備）
    public Video() {
        // 通常在建構子中初始化 createdAt，但更推薦使用 @PrePersist
        // this.createdAt = LocalDateTime.now();
    }

    /**
     * ✅ 有參數建構子：方便快速建立影片資料物件（用於測試或初始化）
     *
     * @param youtubeId      YouTube ID
     * @param title          標題
     * @param description    描述
     * @param videoUrl       影片播放連結
     * @param thumbnailUrl   縮圖連結
     * @param publishedAt    發布時間
     * @param type           影片類型
     * @param subject        影片主題
     * @param durationSeconds 影片長度（秒）
     */
    public Video(String youtubeId, String title, String description, String videoUrl,
                 String thumbnailUrl, LocalDateTime publishedAt, String type,
                 String subject, Integer durationSeconds) {
        this.youtubeId = youtubeId;
        this.title = title;
        this.description = description;
        this.videoUrl = videoUrl;
        this.thumbnailUrl = thumbnailUrl;
        this.publishedAt = publishedAt;
        this.type = type;
        this.subject = subject;
        this.durationSeconds = durationSeconds;
        // createdAt 將由 @PrePersist 自動處理，無需在此手動設定
    }

    // ✅ 在物件持久化（存入資料庫）之前執行此方法
    // 確保 createdAt 欄位總是有一個值
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    // --- 以下為標準的 Getter / Setter，用於物件屬性操作與封裝 ---
    public Long getViedeo_id() {
		return viedeo_id;
	}

	public void setViedeo_id(Long viedeo_id) {
		this.viedeo_id = viedeo_id;
	}
	
	public String getYoutubeId() {
	        return youtubeId;
	}

	public void setYoutubeId(String youtubeId) {
        this.youtubeId = youtubeId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public LocalDateTime getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(LocalDateTime publishedAt) {
        this.publishedAt = publishedAt;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public Integer getDurationSeconds() {
        return durationSeconds;
    }

    public void setDurationSeconds(Integer durationSeconds) {
        this.durationSeconds = durationSeconds;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<ChatSession> getChatSessions() {
        return chatSessions;
    }

    public void setChatSessions(List<ChatSession> chatSessions) {
        this.chatSessions = chatSessions;
    }
}
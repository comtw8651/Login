package com.example.demo.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "video_session_links")
public class VideoSessionLink {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 正確的 ManyToOne 關聯到 Video 實體，使用 'video_id' 作為外來鍵欄位
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "video_id", nullable = false)
    private Video video;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private ChatSession chatSession;

    @Column(name = "start_time_seconds")
    private Integer startTimeSeconds; // 對話開始時的影片時間點（秒）

    @Column(name = "end_time_seconds")
    private Integer endTimeSeconds; // 對話結束時的影片時間點（秒）

    @Column(name = "last_viewed_time_seconds")
    private Integer lastViewedTimeSeconds; // 最後觀看到的時間點（秒）

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // 建構子
    public VideoSessionLink() {
        // createdAt 和 updatedAt 通常透過 @PrePersist 和 @PreUpdate 處理
    }

    public VideoSessionLink(Video video, ChatSession chatSession, Integer startTimeSeconds) {
        this.video = video;
        this.chatSession = chatSession;
        this.startTimeSeconds = startTimeSeconds;
        // createdAt 和 updatedAt 通常透過 @PrePersist 和 @PreUpdate 處理
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Video getVideo() { return video; }
    public void setVideo(Video video) { this.video = video; }

    public ChatSession getChatSession() { return chatSession; }
    public void setChatSession(ChatSession chatSession) { this.chatSession = chatSession; }

    public Integer getStartTimeSeconds() { return startTimeSeconds; }
    public void setStartTimeSeconds(Integer startTimeSeconds) { this.startTimeSeconds = startTimeSeconds; }

    public Integer getEndTimeSeconds() { return endTimeSeconds; }
    public void setEndTimeSeconds(Integer endTimeSeconds) { this.endTimeSeconds = endTimeSeconds; }

    public Integer getLastViewedTimeSeconds() { return lastViewedTimeSeconds; }
    public void setLastViewedTimeSeconds(Integer lastViewedTimeSeconds) {
        this.lastViewedTimeSeconds = lastViewedTimeSeconds;
        this.updatedAt = LocalDateTime.now(); // 觀看時間更新時也更新 updatedAt
    }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (updatedAt == null) updatedAt = LocalDateTime.now(); // 首次創建時也設定 updatedAt
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
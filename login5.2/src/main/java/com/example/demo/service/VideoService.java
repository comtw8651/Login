package com.example.demo.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.entity.ChatSession;
import com.example.demo.entity.Video;
import com.example.demo.entity.VideoSessionLink;
import com.example.demo.repository.ChatSessionRepository;
import com.example.demo.repository.VideoRepository;
import com.example.demo.repository.VideoSessionLinkRepository;

@Service
public class VideoService {
    
    private final VideoRepository videoRepository;
    private final ChatSessionRepository chatSessionRepository;
    private final VideoSessionLinkRepository videoSessionLinkRepository;
    
    public VideoService(VideoRepository videoRepository, 
                       ChatSessionRepository chatSessionRepository,
                       VideoSessionLinkRepository videoSessionLinkRepository) {
        this.videoRepository = videoRepository;
        this.chatSessionRepository = chatSessionRepository;
        this.videoSessionLinkRepository = videoSessionLinkRepository;
    }
    
    public List<Video> getAllVideos() {
        return videoRepository.findAll();
    }
    
    public List<Video> getVideosBySubject(String subject) {
        return videoRepository.findBySubjectOrderByCreatedAtDesc(subject);
    }
    
    public List<ChatSession> getVideoRelatedSessions(Long videoId) {
        Video video = videoRepository.findByIdWithChatSessions(videoId);
        return video != null ? video.getChatSessions() : List.of();
    }
    
    public boolean isSessionBelongsToVideo(Long videoId, Long sessionId) {
        Video video = videoRepository.findByIdWithChatSessions(videoId);
        if (video == null) return false;
        
        return video.getChatSessions().stream()
            .anyMatch(session -> session.getId().equals(sessionId));
    }
    
    @Transactional
    public boolean linkSessionToVideo(Long videoId, Long sessionId) {
        Optional<Video> videoOpt = videoRepository.findById(videoId);
        Optional<ChatSession> sessionOpt = chatSessionRepository.findById(sessionId);
        
        if (videoOpt.isPresent() && sessionOpt.isPresent()) {
            Video video = videoOpt.get();
            ChatSession session = sessionOpt.get();
            
            // 檢查是否已經關聯
            if (!video.getChatSessions().contains(session)) {
                video.getChatSessions().add(session);
                videoRepository.save(video);
            }
            return true;
        }
        return false;
    }
    
    @Transactional
    public Video createVideo(String title, String description, String videoUrl, String subject) {
        Video video = new Video(); // 使用無參數建構子
        video.setTitle(title);
        video.setDescription(description);
        video.setVideoUrl(videoUrl);
        video.setSubject(subject);
        // 其他欄位，例如 youtubeId, thumbnailUrl, publishedAt, durationSeconds, type
        // 如果這些欄位在創建時沒有提供，它們將會是 null 或使用 Entity 中的預設值 (例如 type = "general")
        // 你也可以在這裡設定它們的預設值或從其他來源獲取
        // video.setYoutubeId("預設或根據邏輯生成");
        // video.setThumbnailUrl("預設或根據邏輯生成");
        // video.setPublishedAt(LocalDateTime.now()); // 例如設定為當前時間
        return videoRepository.save(video);
    }
    
    // 新增時間戳記相關方法
    public VideoSessionLink getVideoLinkBySession(Long sessionId) {
        return videoSessionLinkRepository.findByChatSessionId(sessionId).orElse(null);
    }
    
    @Transactional
    public boolean createVideoSessionLink(Long videoId, Long sessionId, Integer startTimeSeconds, Integer endTimeSeconds) {
        Optional<Video> videoOpt = videoRepository.findById(videoId);
        Optional<ChatSession> sessionOpt = chatSessionRepository.findById(sessionId);
        
        if (videoOpt.isPresent() && sessionOpt.isPresent()) {
            VideoSessionLink link = new VideoSessionLink(videoOpt.get(), sessionOpt.get(), startTimeSeconds);
            link.setEndTimeSeconds(endTimeSeconds);
            videoSessionLinkRepository.save(link);
            return true;
        }
        return false;
    }
    
    @Transactional
    public boolean updateLastViewedTime(Long sessionId, Integer timeSeconds) {
        Optional<VideoSessionLink> linkOpt = videoSessionLinkRepository.findByChatSessionId(sessionId);
        if (linkOpt.isPresent()) {
            VideoSessionLink link = linkOpt.get();
            link.setLastViewedTimeSeconds(timeSeconds);
            videoSessionLinkRepository.save(link);
            return true;
        }
        return false;
    }
    
    public List<VideoSessionLink> getConversationsAtTime(Long videoId, Integer timeSeconds) {
        return videoSessionLinkRepository.findByVideoIdAndTimeRange(videoId, timeSeconds);
    }
} 
package com.example.demo.repository;

import com.example.demo.entity.VideoSessionLink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional; // 新增導入 Optional

/**
 * Spring Data JPA Repository for VideoSessionLink entity.
 * Provides standard CRUD operations and custom query methods.
 */
public interface VideoSessionLinkRepository extends JpaRepository<VideoSessionLink, Long> {

    /**
     * Finds all VideoSessionLink entities associated with a specific video ID,
     * ordered by their start time in seconds in ascending order.
     *
     * Spring Data JPA will interpret 'findByVideoId' as:
     * - Look for a property named 'video' in VideoSessionLink.
     * - Then, look for a property named 'id' within that 'video' object.
     * This works automatically now that Video.id is the primary key.
     *
     * @param videoId The ID of the video.
     * @return A list of VideoSessionLink entities.
     */
    List<VideoSessionLink> findByVideoIdOrderByStartTimeSecondsAsc(Long videoId);

    /**
     * Finds a single VideoSessionLink entity by its associated ChatSession ID.
     * Assumes there's at most one VideoSessionLink per ChatSession.
     *
     * Spring Data JPA will interpret 'findByChatSessionId' as:
     * - Look for a property named 'chatSession' in VideoSessionLink.
     * - Then, look for a property named 'id' within that 'chatSession' object.
     *
     * @param sessionId The ID of the ChatSession.
     * @return An Optional containing the VideoSessionLink if found, otherwise empty.
     */
    Optional<VideoSessionLink> findByChatSessionId(Long sessionId); // **新增這個方法**

    /**
     * Finds all VideoSessionLink entities associated with a specific video ID
     * whose time range (startTimeSeconds to endTimeSeconds) encompasses the given timeSeconds.
     *
     * @param videoId The ID of the video.
     * @param timeSeconds The time in seconds to check within the range.
     * @return A list of VideoSessionLink entities that match the criteria.
     */
    // 你目前的 getConversationsAtTime 方法使用的是 findByVideoIdAndTimeRange，
    // 這個方法需要自定義的 JPQL 或 Native SQL。
    // 以下是基於 JPQL 的示例，它假設 startTimeSeconds <= timeSeconds <= endTimeSeconds
    @Query("SELECT vsl FROM VideoSessionLink vsl WHERE vsl.video.id = :videoId " +
           "AND :timeSeconds >= vsl.startTimeSeconds AND :timeSeconds <= vsl.endTimeSeconds")
    List<VideoSessionLink> findByVideoIdAndTimeRange(@Param("videoId") Long videoId,
                                                   @Param("timeSeconds") Integer timeSeconds);
}
package com.bilibili.rec_system.repository;

import com.bilibili.rec_system.entity.Video;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Map;

@Repository
public interface VideoRepository extends JpaRepository<Video, Long> {

    /**
     * 获取视频时长
     */
    @Query("SELECT v.duration FROM Video v WHERE v.videoId = :videoId")
    Long findDurationByVideoId(@Param("videoId") Long videoId);

    // 根据视频ID列表获取标题列表
    @Query("SELECT v.title FROM Video v WHERE v.videoId IN :videoIds")
    List<String> findTitlesByVideoIdIn(@Param("videoIds") List<Long> videoIds);


}
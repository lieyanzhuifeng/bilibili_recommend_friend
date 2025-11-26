package com.bilibili.rec_system.repository;

import com.bilibili.rec_system.entity.Video;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@Repository
public interface VideoRepository extends JpaRepository<Video, Long> {

    //根据视频ID获取视频时长
    @Query("SELECT v.duration FROM Video v WHERE v.videoId = :videoId")
    LocalTime findDurationByVideoId(@Param("videoId") Long videoId);

    // 根据视频ID列表获取标题列表
    @Query("SELECT v.title FROM Video v WHERE v.videoId IN :videoIds")
    List<String> findTitlesByVideoIdIn(@Param("videoIds") List<Long> videoIds);

    /**
     * 根据标签ID返回该标签下的视频数量
     */
    @Query("SELECT COUNT(v) FROM Video v WHERE v.themeId = :tagId")
    Long countVideosByTagId(@Param("tagId") Long tagId);

    /**
     * 根据UP主ID返回该UP主的视频数量
     */
    @Query("SELECT COUNT(v) FROM Video v WHERE v.uploaderId = :upId")
    Long countVideosByUpId(@Param("upId") Long upId);

    /**
     * 根据标题模糊搜索视频（不区分大小写）
     */
    List<Video> findByTitleContaining(@Param("title") String title);
}
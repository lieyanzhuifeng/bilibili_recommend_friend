package com.bilibili.rec_system.repository;

import com.bilibili.rec_system.entity.TagVideo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TagVideoRepository extends JpaRepository<TagVideo, Long> {

    // 系列相关
    @Query("SELECT tv.videoId FROM TagVideo tv WHERE tv.tagId = :tagId")
    List<Long> findVideoIdsByTag(@Param("tagId") Long tagId);

    @Query("SELECT tv.videoId FROM TagVideo tv WHERE tv.tagId IN :tagIds")
    List<Long> findVideoIdsByTags(@Param("tagIds") List<Long> tagIds);
}
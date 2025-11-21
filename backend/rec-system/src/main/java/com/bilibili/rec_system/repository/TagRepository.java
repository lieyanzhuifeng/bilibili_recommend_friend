package com.bilibili.rec_system.repository;

import com.bilibili.rec_system.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {

    /**
     * 根据标签ID获取标签名称
     */
    @Query("SELECT t.tagName FROM Tag t WHERE t.tagId = :tagId")
    String findTagNameByTagId(@Param("tagId") Long tagId);

    /**
     * 根据标签ID获取标签实体
     */
    Tag findByTagId(Long tagId);

    /**
     * 检查是否为系列标签（标签名称以"系列"结尾）
     */
    @Query("SELECT CASE WHEN t.tagName LIKE '%系列' THEN true ELSE false END FROM Tag t WHERE t.tagId = :tagId")
    Boolean isSeriesTag(@Param("tagId") Long tagId);
}
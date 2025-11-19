package com.bilibili.rec_system.repository;

import com.bilibili.rec_system.entity.VideoCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface VideoCategoryRepository extends JpaRepository<VideoCategory, Long> {

    /**
     * 根据分区ID获取分区名称
     */
    @Query("SELECT vc.categoryName FROM VideoCategory vc WHERE vc.categoryId = :categoryId")
    String findCategoryNameById(@Param("categoryId") Long categoryId);

    /**
     * 根据分区ID获取分区实体
     */
    VideoCategory findByCategoryId(Long categoryId);
}
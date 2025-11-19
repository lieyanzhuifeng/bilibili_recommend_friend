package com.bilibili.rec_system.repository;

import com.bilibili.rec_system.entity.VideoTheme;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface VideoThemeRepository extends JpaRepository<VideoTheme, Long> {

    /**
     * 根据主题ID获取主题名称
     */
    @Query("SELECT vt.themeName FROM VideoTheme vt WHERE vt.themeId = :themeId")
    String findThemeNameById(@Param("themeId") Long themeId);

    /**
     * 根据主题ID获取主题实体
     */
    VideoTheme findByThemeId(Long themeId);
}
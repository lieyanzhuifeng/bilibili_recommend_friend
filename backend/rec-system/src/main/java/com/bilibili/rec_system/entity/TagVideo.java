package com.bilibili.rec_system.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;

@Entity
@Table(name = "tag_video")
@Data
@IdClass(TagVideo.TagVideoId.class)
public class TagVideo {
    @Id
    @Column(name = "videoID")
    private Long videoId;

    @Id
    @Column(name = "tagID")
    private Long tagId;

    // 使用Lombok自动生成equals和hashCode
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TagVideoId implements java.io.Serializable {
        private Long videoId;
        private Long tagId;
    }
}
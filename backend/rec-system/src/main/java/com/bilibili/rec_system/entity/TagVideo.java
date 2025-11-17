package com.bilibili.rec_system.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "tag_video")
@Data
public class TagVideo {
    @Id
    @Column(name = "videoID")
    private Long videoId;

    @Id
    @Column(name = "tagID")
    private Long tagId;
}

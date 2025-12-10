package com.bilibili.rec_system.dto;

import com.bilibili.rec_system.entity.Comment;
import com.bilibili.rec_system.entity.User;
import com.bilibili.rec_system.entity.Video;

import java.io.Serializable;
import java.util.Objects;

public class FriendRecommendationDTO extends BaseDTO implements Serializable {
    private Comment userComment;        // 用户自己的评论（新增）
    private Comment matchedComment;     // 匹配到的其他用户评论
    private Video video;                // 相关视频
    private User recommendedUser;       // 推荐用户
    private double matchScore;          // 匹配分数

    // 默认构造方法
    public FriendRecommendationDTO() {}

    // 全参构造方法
    public FriendRecommendationDTO(Comment userComment, Comment matchedComment,
                                   Video video, User recommendedUser, double matchScore) {
        this.userComment = userComment;
        this.matchedComment = matchedComment;
        this.video = video;
        this.recommendedUser = recommendedUser;
        this.matchScore = matchScore;
    }

    // getters and setters
    public Comment getUserComment() {
        return userComment;
    }

    public void setUserComment(Comment userComment) {
        this.userComment = userComment;
    }

    public Comment getMatchedComment() {
        return matchedComment;
    }

    public void setMatchedComment(Comment matchedComment) {
        this.matchedComment = matchedComment;
    }

    public Video getVideo() {
        return video;
    }

    public void setVideo(Video video) {
        this.video = video;
    }

    public User getRecommendedUser() {
        return recommendedUser;
    }

    public void setRecommendedUser(User recommendedUser) {
        this.recommendedUser = recommendedUser;
    }

    public double getMatchScore() {
        return matchScore;
    }

    public void setMatchScore(double matchScore) {
        this.matchScore = matchScore;
    }

    @Override
    public String toString() {
        return String.format("FriendRecommendationDTO{用户评论ID=%s, 匹配评论ID=%s, 推荐用户=%s, 分数=%.4f}",
                userComment != null ? userComment.getCommentId() : "null",
                matchedComment != null ? matchedComment.getCommentId() : "null",
                recommendedUser != null ? recommendedUser.getUsername() : "null",
                matchScore);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FriendRecommendationDTO that = (FriendRecommendationDTO) o;

        if (Double.compare(that.matchScore, matchScore) != 0) return false;
        if (!Objects.equals(userComment, that.userComment)) return false;
        if (!Objects.equals(matchedComment, that.matchedComment))
            return false;
        if (!Objects.equals(video, that.video)) return false;
        return Objects.equals(recommendedUser, that.recommendedUser);
    }

    @Override
    public int hashCode() {
        int result;
        result = userComment != null ? userComment.hashCode() : 0;
        result = 31 * result + (matchedComment != null ? matchedComment.hashCode() : 0);
        result = 31 * result + (video != null ? video.hashCode() : 0);
        result = 31 * result + (recommendedUser != null ? recommendedUser.hashCode() : 0);
        result = 31 * result + Double.hashCode(matchScore);
        return result;
    }
}
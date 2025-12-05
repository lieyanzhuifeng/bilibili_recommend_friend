package com.bilibili.rec_system.service.RecommendationServiceImpl;

import com.bilibili.rec_system.dto.BaseDTO;
import com.bilibili.rec_system.dto.FavoriteSimilarityDTO;
import com.bilibili.rec_system.entity.User;
import com.bilibili.rec_system.repository.*;
import com.bilibili.rec_system.service.RecommendationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class FavoriteSimilarityService implements RecommendationService {

    @Autowired
    private UserFavoritesRepository userFavoritesRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VideoRepository videoRepository;

    @Override
    public List<BaseDTO> recommendUsers(Long userId) {
        return recommendUsers(userId, 3);
    }

    public List<BaseDTO> recommendUsers(Long userId, Integer limit) {
        // 1. 获取目标用户收藏的所有视频
        List<Long> targetVideoIds = userFavoritesRepository.findVideoIdsByUserId(userId);
        if (targetVideoIds.isEmpty()) {
            return new ArrayList<>();
        }

        // 2. 获取所有其他用户
        List<User> allUsers = userRepository.findAll();
        List<Long> allUserIds = allUsers.stream()
                .map(User::getUserId)
                .filter(id -> !id.equals(userId))
                .collect(Collectors.toList());

        // 3. 计算每个候选用户的收藏夹相似度
        List<FavoriteSimilarityDTO> result = new ArrayList<>();

        for (Long candidateUserId : allUserIds) {
            // 获取候选用户收藏的所有视频
            List<Long> candidateVideoIds = userFavoritesRepository.findVideoIdsByUserId(candidateUserId);

            if (!candidateVideoIds.isEmpty()) {
                // 计算交集和并集
                Set<Long> intersection = new HashSet<>(targetVideoIds);
                intersection.retainAll(candidateVideoIds);

                Set<Long> union = new HashSet<>(targetVideoIds);
                union.addAll(candidateVideoIds);

                // 计算Jaccard相似度
                double similarity = union.size() > 0 ? (double) intersection.size() / union.size() : 0;

                if (similarity > 0) {
                    User candidateUser = userRepository.findByUserId(candidateUserId);
                    if (candidateUser != null) {
                        FavoriteSimilarityDTO dto = new FavoriteSimilarityDTO(
                                candidateUser.getUserId(),
                                candidateUser.getUsername(),
                                candidateUser.getAvatarPath(),
                                similarity
                        );
                        result.add(dto);
                    }
                }
            }
        }

        // 4. 按相似度排序并限制数量
        return result.stream()
                .sorted((a, b) -> Double.compare(b.getSimilarityScore(), a.getSimilarityScore()))
                .limit(limit)
                .collect(Collectors.toList());
    }

    public Map<Long, String> show(Long userId) {
        // 获取用户收藏的视频ID列表
        List<Long> favoriteVideoIds = userFavoritesRepository.findVideoIdsByUserId(userId);

        if (favoriteVideoIds.isEmpty()) {
            return Collections.emptyMap();
        }

        // 使用Repository提供的方法一步获取所有视频标题
        List<String> videoTitles = videoRepository.findTitlesByVideoIdIn(favoriteVideoIds);

        // 构建映射关系
        Map<Long, String> favorites = new LinkedHashMap<>();

        // 确保两个列表长度一致
        int minSize = Math.min(favoriteVideoIds.size(), videoTitles.size());
        for (int i = 0; i < minSize; i++) {
            favorites.put(favoriteVideoIds.get(i), videoTitles.get(i));
        }

        return favorites;
    }

}
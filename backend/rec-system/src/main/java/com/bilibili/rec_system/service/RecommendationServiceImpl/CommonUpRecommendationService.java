package com.bilibili.rec_system.service.RecommendationServiceImpl;

import com.bilibili.rec_system.dto.BaseDTO;
import com.bilibili.rec_system.dto.CommonUpRecommendationDTO;
import com.bilibili.rec_system.dto.FriendRecommendationDTO;
import com.bilibili.rec_system.entity.User;
import com.bilibili.rec_system.repository.UserFollowUpRepository;
import com.bilibili.rec_system.repository.UserRepository;
import com.bilibili.rec_system.service.RecommendationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class CommonUpRecommendationService implements RecommendationService {

    @Autowired
    private UserFollowUpRepository userFollowUpRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    public List<BaseDTO> recommendUsers(Long userId) {
        return recommendUsers(userId, 3); // 默认返回前3个
    }

    public List<BaseDTO> recommendUsers(Long userId, Integer limit) {
        // 1. 获取目标用户关注的所有UP主
        List<Long> targetUpIds = userFollowUpRepository.findUpIdsByUserId(userId);
        if (targetUpIds.isEmpty()) {
            return new ArrayList<>();
        }

        // 2. 获取所有其他用户
        List<User> allUsers = userRepository.findAll();
        List<Long> allUserIds = allUsers.stream()
                .map(User::getUserId)
                .filter(id -> !id.equals(userId))
                .collect(Collectors.toList());

        // 3. 计算每个候选用户的Jaccard相似度
        List<CommonUpRecommendationDTO> result = new ArrayList<>();

        for (Long candidateUserId : allUserIds) {
            // 获取候选用户关注的所有UP主
            List<Long> candidateUpIds = userFollowUpRepository.findUpIdsByUserId(candidateUserId);

            if (!candidateUpIds.isEmpty()) {
                // 计算交集和并集
                Set<Long> intersection = new HashSet<>(targetUpIds);
                intersection.retainAll(candidateUpIds);

                Set<Long> union = new HashSet<>(targetUpIds);
                union.addAll(candidateUpIds);

                // 计算Jaccard相似度
                double similarity = union.size() > 0 ? (double) intersection.size() / union.size() : 0;

                if (similarity > 0) {
                    // 获取共同关注的UP主名称
                    List<String> commonUpNames = getUpNames(new ArrayList<>(intersection));

                    User candidateUser = userRepository.findByUserId(candidateUserId);
                    if (candidateUser != null) {
                        CommonUpRecommendationDTO dto = new CommonUpRecommendationDTO(
                                candidateUser.getUserId(),
                                candidateUser.getUsername(),
                                candidateUser.getAvatarPath(),
                                intersection.size(),
                                commonUpNames
                        );
                        result.add(dto);
                    }
                }
            }
        }

        // 4. 按Jaccard相似度排序并限制数量
        return result.stream()
                .sorted((a, b) -> {
                    // 计算a的相似度
                    Set<Long> aUnion = new HashSet<>(targetUpIds);
                    aUnion.addAll(userFollowUpRepository.findUpIdsByUserId(a.getUserId()));
                    double aSimilarity = (double) a.getCommonUpCount() / aUnion.size();

                    // 计算b的相似度
                    Set<Long> bUnion = new HashSet<>(targetUpIds);
                    bUnion.addAll(userFollowUpRepository.findUpIdsByUserId(b.getUserId()));
                    double bSimilarity = (double) b.getCommonUpCount() / bUnion.size();

                    return Double.compare(bSimilarity, aSimilarity);
                })
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * 根据UP主ID列表获取UP主名称
     */
    private List<String> getUpNames(List<Long> upIds) {
        return upIds.stream()
                .map(upId -> {
                    String upName = userRepository.findUpNameById(upId);
                    return upName != null ? upName : "未知UP主";
                })
                .collect(Collectors.toList());
    }

    public List<Map<String, Object>> show(Long userId) {
        // 获取用户关注的所有UP主ID
        List<Long> upIds = userFollowUpRepository.findUpIdsByUserId(userId);

        if (upIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<Map<String, Object>> upList = new ArrayList<>();

        for (Long upId : upIds) {
            User up = userRepository.findByUserId(upId);
            if (up != null) {
                Map<String, Object> upInfo = new HashMap<>();
                upInfo.put("upId", up.getUserId());
                upInfo.put("username", up.getUsername());
                upInfo.put("avatarPath", up.getAvatarPath());
                upInfo.put("registerTime", up.getRegisterTime());
                upList.add(upInfo);
            }
        }

        return upList;
    }

}
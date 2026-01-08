/**
 * 头像生成工具函数
 * 用于在用户头像为空时生成默认头像
 */

/**
 * 根据用户ID生成随机头像
 * 使用dicebear API生成含有人物的头像
 * @param {string|number} seed - 用于生成头像的种子值，通常是用户ID
 * @returns {string} - 生成的头像URL
 */
export function generateRandomAvatar(seed) {
  // 使用用户ID生成种子，确保同一个用户总是得到相同的头像
  const seedValue = seed ? String(seed).toLowerCase().replace(/\s+/g, '-') : 'default';
  // 使用dicebear的avataaars样式生成含有人物的头像
  return `https://api.dicebear.com/7.x/avataaars/svg?seed=${seedValue}`;
}

/**
 * 获取用户头像，为空时自动生成
 * @param {string|null} avatarPath - 用户头像路径
 * @param {string|number} userId - 用户ID，用于生成默认头像
 * @returns {string} - 头像路径或生成的默认头像
 */
export function getUserAvatar(avatarPath, userId) {
  // 如果头像路径存在且不为空字符串，则返回头像路径
  if (avatarPath && avatarPath.trim() !== '') {
    return avatarPath;
  }
  
  // 否则生成默认头像
  return generateRandomAvatar(userId);
}

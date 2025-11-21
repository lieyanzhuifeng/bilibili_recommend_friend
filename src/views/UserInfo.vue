<template>
  <div class="user-info-container">
    <h1>编辑个人信息</h1>

    <Card class="profile-form-card">
      <form @submit.prevent="handleSubmit" class="profile-form">
        <!-- 头像编辑 -->
        <div class="form-section">
          <h2 class="section-title">基本信息</h2>

          <div class="avatar-upload-section">
            <div class="avatar-preview">
              <img :src="formData.avatar" alt="用户头像" class="avatar-img" />
              <div class="avatar-upload-overlay">
                <button type="button" @click="triggerAvatarUpload" class="upload-btn">
                  <i class="upload-icon">📷</i>
                  更换头像
                </button>
                <input
                  ref="avatarInput"
                  type="file"
                  accept="image/*"
                  style="display: none"
                  @change="handleAvatarChange"
                />
              </div>
            </div>
          </div>

          <!-- 用户名 -->
          <div class="form-group">
            <label for="username" class="form-label">用户名</label>
            <input
              type="text"
              id="username"
              v-model="formData.username"
              class="form-input"
              :class="{ error: errors.username }"
              placeholder="请输入用户名"
              maxlength="20"
            />
            <span v-if="errors.username" class="error-message">{{ errors.username }}</span>
            <span class="char-count">{{ formData.username.length }}/20</span>
          </div>

          <!-- 简介 -->
          <div class="form-group">
            <label for="bio" class="form-label">个人简介</label>
            <textarea
              id="bio"
              v-model="formData.bio"
              class="form-textarea"
              :class="{ error: errors.bio }"
              placeholder="介绍一下自己吧~"
              rows="4"
              maxlength="200"
            ></textarea>
            <span v-if="errors.bio" class="error-message">{{ errors.bio }}</span>
            <span class="char-count">{{ formData.bio.length }}/200</span>
          </div>
        </div>

        <!-- 联系信息 -->
        <div class="form-section">
          <h2 class="section-title">联系信息</h2>

          <div class="form-group">
            <label for="email" class="form-label">邮箱 <span class="optional">(选填)</span></label>
            <input
              type="email"
              id="email"
              v-model="formData.email"
              class="form-input"
              :class="{ error: errors.email }"
              placeholder="请输入邮箱地址"
            />
            <span v-if="errors.email" class="error-message">{{ errors.email }}</span>
          </div>

          <div class="form-group">
            <label for="phone" class="form-label">手机号码 <span class="optional">(选填)</span></label>
            <input
              type="tel"
              id="phone"
              v-model="formData.phone"
              class="form-input"
              :class="{ error: errors.phone }"
              placeholder="请输入手机号码"
            />
            <span v-if="errors.phone" class="error-message">{{ errors.phone }}</span>
          </div>
        </div>

        <!-- 偏好设置 -->
        <div class="form-section">
          <h2 class="section-title">偏好设置</h2>

          <div class="form-group">
            <label class="form-label">主题偏好</label>
            <div class="theme-options">
              <label class="theme-option">
                <input
                  type="radio"
                  name="theme"
                  value="light"
                  v-model="formData.theme"
                />
                <span class="theme-label">浅色模式</span>
              </label>
              <label class="theme-option">
                <input
                  type="radio"
                  name="theme"
                  value="dark"
                  v-model="formData.theme"
                />
                <span class="theme-label">深色模式</span>
              </label>
              <label class="theme-option">
                <input
                  type="radio"
                  name="theme"
                  value="system"
                  v-model="formData.theme"
                />
                <span class="theme-label">跟随系统</span>
              </label>
            </div>
          </div>

          <div class="form-group">
            <label class="checkbox-label">
              <input
                type="checkbox"
                v-model="formData.notifications"
              />
              <span>接收消息通知</span>
            </label>
          </div>

          <div class="form-group">
            <label class="checkbox-label">
              <input
                type="checkbox"
                v-model="formData.publicProfile"
              />
              <span>公开个人资料</span>
            </label>
          </div>
        </div>

        <!-- 账号安全 -->
        <div class="form-section">
          <h2 class="section-title">账号安全</h2>

          <div class="security-info">
            <div class="security-item">
              <span class="security-label">账号状态：</span>
              <span class="security-value account-active">正常</span>
            </div>
            <div class="security-item">
              <span class="security-label">注册时间：</span>
              <span class="security-value">{{ userStore.registerDate }}</span>
            </div>
            <div class="security-item">
              <span class="security-label">最后登录：</span>
              <span class="security-value">{{ lastLoginTime }}</span>
            </div>
          </div>

          <div class="security-actions">
            <CustomButton type="outline" size="medium" @click="showChangePasswordModal = true">
              修改密码
            </CustomButton>
            <CustomButton type="outline" size="medium" @click="showBindPhoneModal = true">
              绑定手机
            </CustomButton>
          </div>
        </div>

        <!-- 表单操作按钮 -->
        <div class="form-actions">
          <CustomButton type="secondary" size="large" @click="handleCancel">
            取消
          </CustomButton>
          <CustomButton
            type="primary"
            size="large"
            @click="handleSubmit"
            :loading="isSubmitting"
          >
            保存更改
          </CustomButton>
        </div>
      </form>
    </Card>

    <!-- 修改密码弹窗 -->
    <div v-if="showChangePasswordModal" class="modal-overlay" @click.self="closeChangePasswordModal">
      <div class="modal-content">
        <div class="modal-header">
          <h3>修改密码</h3>
          <button class="close-btn" @click="closeChangePasswordModal">×</button>
        </div>
        <div class="modal-body">
          <form @submit.prevent="handleChangePassword" class="password-form">
            <div class="form-group">
              <label for="oldPassword" class="form-label">当前密码</label>
              <input
                type="password"
                id="oldPassword"
                v-model="passwordForm.oldPassword"
                class="form-input"
                placeholder="请输入当前密码"
              />
            </div>
            <div class="form-group">
              <label for="newPassword" class="form-label">新密码</label>
              <input
                type="password"
                id="newPassword"
                v-model="passwordForm.newPassword"
                class="form-input"
                placeholder="请输入新密码（6-20位）"
              />
            </div>
            <div class="form-group">
              <label for="confirmPassword" class="form-label">确认新密码</label>
              <input
                type="password"
                id="confirmPassword"
                v-model="passwordForm.confirmPassword"
                class="form-input"
                placeholder="请再次输入新密码"
              />
            </div>
            <div v-if="passwordError" class="error-message password-error">
              {{ passwordError }}
            </div>
            <div class="modal-actions">
              <CustomButton type="secondary" size="medium" @click="closeChangePasswordModal">
                取消
              </CustomButton>
              <CustomButton
                type="primary"
                size="medium"
                @click="handleChangePassword"
                :loading="isChangingPassword"
              >
                确认修改
              </CustomButton>
            </div>
          </form>
        </div>
      </div>
    </div>

    <!-- 绑定手机弹窗 -->
    <div v-if="showBindPhoneModal" class="modal-overlay" @click.self="closeBindPhoneModal">
      <div class="modal-content">
        <div class="modal-header">
          <h3>绑定手机号</h3>
          <button class="close-btn" @click="closeBindPhoneModal">×</button>
        </div>
        <div class="modal-body">
          <form @submit.prevent="handleBindPhone" class="phone-form">
            <div class="form-group">
              <label for="bindPhone" class="form-label">手机号码</label>
              <input
                type="tel"
                id="bindPhone"
                v-model="phoneForm.phone"
                class="form-input"
                placeholder="请输入手机号码"
              />
            </div>
            <div class="form-group">
              <label for="verificationCode" class="form-label">验证码</label>
              <div class="verification-code-container">
                <input
                  type="text"
                  id="verificationCode"
                  v-model="phoneForm.verificationCode"
                  class="form-input verification-input"
                  placeholder="请输入验证码"
                />
                <CustomButton
                  type="outline"
                  size="medium"
                  class="send-code-btn"
                  :disabled="countdown > 0"
                  @click="sendVerificationCode"
                >
                  {{ countdown > 0 ? `${countdown}秒后重新发送` : '发送验证码' }}
                </CustomButton>
              </div>
            </div>
            <div v-if="phoneError" class="error-message phone-error">
              {{ phoneError }}
            </div>
            <div class="modal-actions">
              <CustomButton type="secondary" size="medium" @click="closeBindPhoneModal">
                取消
              </CustomButton>
              <CustomButton
                type="primary"
                size="medium"
                @click="handleBindPhone"
                :loading="isBindingPhone"
              >
                确认绑定
              </CustomButton>
            </div>
          </form>
        </div>
      </div>
    </div>

    <!-- 成功提示 -->
    <div v-if="showSuccessMessage" class="success-toast">
      <span class="success-icon">✅</span>
      <span>{{ successMessage }}</span>
    </div>
  </div>
</template>

<script>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import Card from '../components/Card.vue'
import CustomButton from '../components/Button.vue'

export default {
  name: 'UserInfo',
  components: {
    Card,
    CustomButton
  },
  setup() {
    const router = useRouter()
    const userStore = useUserStore()
    const avatarInput = ref(null)

    // 表单数据
    const formData = reactive({
      username: '',
      bio: '',
      avatar: '',
      email: '',
      phone: '',
      theme: 'light',
      notifications: true,
      publicProfile: true
    })

    // 错误信息
    const errors = reactive({
      username: '',
      bio: '',
      email: '',
      phone: ''
    })

    // 弹窗状态
    const showChangePasswordModal = ref(false)
    const showBindPhoneModal = ref(false)
    const showSuccessMessage = ref(false)
    const successMessage = ref('')

    // 密码表单
    const passwordForm = reactive({
      oldPassword: '',
      newPassword: '',
      confirmPassword: ''
    })
    const passwordError = ref('')

    // 手机表单
    const phoneForm = reactive({
      phone: '',
      verificationCode: ''
    })
    const phoneError = ref('')
    const countdown = ref(0)

    // 加载状态
    const isSubmitting = ref(false)
    const isChangingPassword = ref(false)
    const isBindingPhone = ref(false)

    // 计算属性
    const lastLoginTime = computed(() => {
      // 模拟最后登录时间
      const now = new Date()
      now.setMinutes(now.getMinutes() - 30)
      return now.toLocaleString('zh-CN')
    })

    // 方法
    const loadUserData = () => {
      const userInfo = userStore.userInfo
      formData.username = userInfo.username
      formData.bio = userInfo.bio || ''
      formData.avatar = userInfo.avatar
      formData.email = userInfo.email || ''
      formData.phone = userInfo.phone || ''
      formData.theme = userInfo.theme || 'light'
      formData.notifications = userInfo.notifications !== false
      formData.publicProfile = userInfo.publicProfile !== false
    }

    const validateForm = () => {
      let isValid = true

      // 清空错误信息
      Object.keys(errors).forEach(key => {
        errors[key] = ''
      })

      // 验证用户名
      if (!formData.username.trim()) {
        errors.username = '用户名不能为空'
        isValid = false
      } else if (formData.username.length < 2) {
        errors.username = '用户名至少需要2个字符'
        isValid = false
      }

      // 验证邮箱（如果填写）
      if (formData.email && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(formData.email)) {
        errors.email = '请输入有效的邮箱地址'
        isValid = false
      }

      // 验证手机号（如果填写）
      if (formData.phone && !/^1[3-9]\d{9}$/.test(formData.phone)) {
        errors.phone = '请输入有效的手机号码'
        isValid = false
      }

      return isValid
    }

    const handleSubmit = async () => {
      if (!validateForm()) {
        return
      }

      isSubmitting.value = true

      try {
        // 模拟API请求延迟
        await new Promise(resolve => setTimeout(resolve, 1000))

        // 更新用户信息
        userStore.updateUserInfo({
          username: formData.username,
          bio: formData.bio,
          avatar: formData.avatar,
          email: formData.email,
          phone: formData.phone,
          theme: formData.theme,
          notifications: formData.notifications,
          publicProfile: formData.publicProfile
        })

        showSuccess('个人信息更新成功！')
      } catch (e) {
        console.error('更新失败:', e)
      } finally {
        isSubmitting.value = false
      }
    }

    const handleCancel = () => {
      router.go(-1)
    }

    const triggerAvatarUpload = () => {
      avatarInput.value.click()
    }

    const handleAvatarChange = (event) => {
      const file = event.target.files[0]
      if (file) {
        // 检查文件大小（最大5MB）
        if (file.size > 5 * 1024 * 1024) {
          showError('文件大小不能超过5MB')
          return
        }

        // 检查文件类型
        if (!file.type.startsWith('image/')) {
          showError('请上传图片文件')
          return
        }

        // 预览图片
        const reader = new FileReader()
        reader.onload = (e) => {
          formData.avatar = e.target.result
        }
        reader.readAsDataURL(file)

        // 清空input，允许重复选择同一文件
        event.target.value = ''
      }
    }

    // 密码修改相关方法
    const closeChangePasswordModal = () => {
      showChangePasswordModal.value = false
      passwordForm.oldPassword = ''
      passwordForm.newPassword = ''
      passwordForm.confirmPassword = ''
      passwordError.value = ''
    }

    const handleChangePassword = async () => {
      // 重置错误信息
      passwordError.value = ''

      // 验证密码
      if (!passwordForm.oldPassword) {
        passwordError.value = '请输入当前密码'
        return
      }

      if (!passwordForm.newPassword) {
        passwordError.value = '请输入新密码'
        return
      }

      if (passwordForm.newPassword.length < 6 || passwordForm.newPassword.length > 20) {
        passwordError.value = '密码长度需要在6-20位之间'
        return
      }

      if (passwordForm.newPassword !== passwordForm.confirmPassword) {
        passwordError.value = '两次输入的密码不一致'
        return
      }

      isChangingPassword.value = true

      try {
        // 模拟API请求延迟
        await new Promise(resolve => setTimeout(resolve, 1000))

        // 模拟修改密码成功
        closeChangePasswordModal()
        showSuccess('密码修改成功！')
      } catch (e) {
        passwordError.value = '修改密码失败，请稍后重试'
      } finally {
        isChangingPassword.value = false
      }
    }

    // 手机绑定相关方法
    const closeBindPhoneModal = () => {
      showBindPhoneModal.value = false
      phoneForm.phone = ''
      phoneForm.verificationCode = ''
      phoneError.value = ''
      countdown.value = 0
    }

    const sendVerificationCode = () => {
      if (!phoneForm.phone) {
        phoneError.value = '请输入手机号码'
        return
      }

      if (!/^1[3-9]\d{9}$/.test(phoneForm.phone)) {
        phoneError.value = '请输入有效的手机号码'
        return
      }

      // 开始倒计时
      countdown.value = 60
      const timer = setInterval(() => {
        countdown.value--
        if (countdown.value <= 0) {
          clearInterval(timer)
        }
      }, 1000)

      // 模拟发送验证码
      phoneError.value = '验证码已发送（模拟）'
      setTimeout(() => {
        phoneError.value = ''
      }, 3000)
    }

    const handleBindPhone = async () => {
      // 重置错误信息
      phoneError.value = ''

      // 验证手机号
      if (!phoneForm.phone) {
        phoneError.value = '请输入手机号码'
        return
      }

      if (!/^1[3-9]\d{9}$/.test(phoneForm.phone)) {
        phoneError.value = '请输入有效的手机号码'
        return
      }

      // 验证验证码
      if (!phoneForm.verificationCode) {
        phoneError.value = '请输入验证码'
        return
      }

      if (!/^\d{6}$/.test(phoneForm.verificationCode)) {
        phoneError.value = '请输入6位数字验证码'
        return
      }

      isBindingPhone.value = true

      try {
        // 模拟API请求延迟
        await new Promise(resolve => setTimeout(resolve, 1000))

        // 模拟绑定成功
        formData.phone = phoneForm.phone
        closeBindPhoneModal()
        showSuccess('手机号绑定成功！')
      } catch (e) {
        phoneError.value = '绑定手机号失败，请稍后重试'
      } finally {
        isBindingPhone.value = false
      }
    }

    // 提示信息
    const showSuccess = (message) => {
      successMessage.value = message
      showSuccessMessage.value = true
      setTimeout(() => {
        showSuccessMessage.value = false
      }, 3000)
    }

    const showError = (message) => {
      phoneError.value = message
      setTimeout(() => {
        phoneError.value = ''
      }, 3000)
    }

    onMounted(() => {
      loadUserData()
    })

    return {
      formData,
      errors,
      avatarInput,
      showChangePasswordModal,
      showBindPhoneModal,
      showSuccessMessage,
      successMessage,
      passwordForm,
      passwordError,
      phoneForm,
      phoneError,
      countdown,
      isSubmitting,
      isChangingPassword,
      isBindingPhone,
      lastLoginTime,
      userStore,
      handleSubmit,
      handleCancel,
      triggerAvatarUpload,
      handleAvatarChange,
      closeChangePasswordModal,
      handleChangePassword,
      closeBindPhoneModal,
      sendVerificationCode,
      handleBindPhone
    }
  }
}
</script>

<style scoped>
.user-info-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
  min-height: 100vh;
  background-color: var(--background-color);
}

.user-info-container h1 {
  margin: 0 0 20px 0;
  font-size: 28px;
  color: var(--text-primary);
}

.profile-form-card {
  margin-bottom: 20px;
}

.profile-form {
  display: flex;
  flex-direction: column;
  gap: 30px;
}

.form-section {
  border-bottom: 1px solid var(--border-color);
  padding-bottom: 20px;
}

.form-section:last-child {
  border-bottom: none;
}

.section-title {
  margin: 0 0 20px 0;
  font-size: 20px;
  color: var(--text-primary);
  font-weight: 600;
}

/* 头像上传 */
.avatar-upload-section {
  display: flex;
  justify-content: center;
  margin-bottom: 30px;
}

.avatar-preview {
  position: relative;
  width: 150px;
  height: 150px;
  border-radius: 50%;
  overflow: hidden;
  border: 3px solid var(--border-color);
  cursor: pointer;
  transition: all 0.3s;
}

.avatar-preview:hover {
  border-color: var(--primary-color);
  transform: scale(1.05);
}

.avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-upload-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s;
}

.avatar-preview:hover .avatar-upload-overlay {
  opacity: 1;
}

.upload-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 5px;
  background: none;
  border: none;
  color: white;
  cursor: pointer;
  font-size: 14px;
}

.upload-icon {
  font-size: 24px;
}

/* 表单组 */
.form-group {
  margin-bottom: 20px;
  position: relative;
}

.form-label {
  display: block;
  margin-bottom: 8px;
  font-weight: 600;
  color: var(--text-primary);
  font-size: 14px;
}

.optional {
  color: var(--text-secondary);
  font-weight: normal;
  font-size: 12px;
}

.form-input {
  width: 100%;
  padding: 10px 15px;
  border: 1px solid var(--border-color);
  border-radius: var(--border-radius);
  font-size: 14px;
  transition: all 0.3s;
  background-color: white;
}

.form-input:focus {
  outline: none;
  border-color: var(--primary-color);
  box-shadow: 0 0 0 2px rgba(251, 114, 153, 0.2);
}

.form-input.error {
  border-color: var(--danger-color);
}

.form-input.error:focus {
  box-shadow: 0 0 0 2px rgba(245, 34, 45, 0.2);
}

.form-textarea {
  width: 100%;
  padding: 10px 15px;
  border: 1px solid var(--border-color);
  border-radius: var(--border-radius);
  font-size: 14px;
  transition: all 0.3s;
  resize: vertical;
  background-color: white;
  font-family: inherit;
}

.form-textarea:focus {
  outline: none;
  border-color: var(--primary-color);
  box-shadow: 0 0 0 2px rgba(251, 114, 153, 0.2);
}

.form-textarea.error {
  border-color: var(--danger-color);
}

.char-count {
  position: absolute;
  right: 10px;
  bottom: 10px;
  font-size: 12px;
  color: var(--text-secondary);
}

.error-message {
  color: var(--danger-color);
  font-size: 12px;
  margin-top: 5px;
  display: block;
}

/* 主题选项 */
.theme-options {
  display: flex;
  gap: 20px;
}

.theme-option {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}

.theme-option input[type="radio"] {
  cursor: pointer;
}

.theme-label {
  font-size: 14px;
  color: var(--text-primary);
}

/* 复选框 */
.checkbox-label {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  font-size: 14px;
  color: var(--text-primary);
}

.checkbox-label input[type="checkbox"] {
  cursor: pointer;
}

/* 安全信息 */
.security-info {
  margin-bottom: 20px;
}

.security-item {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
}

.security-label {
  font-size: 14px;
  color: var(--text-secondary);
  min-width: 80px;
}

.security-value {
  font-size: 14px;
  color: var(--text-primary);
}

.account-active {
  color: #52c41a;
  font-weight: 600;
}

.security-actions {
  display: flex;
  gap: 15px;
}

/* 表单操作按钮 */
.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 15px;
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid var(--border-color);
}

/* 弹窗样式 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  background-color: white;
  border-radius: var(--border-radius);
  width: 90%;
  max-width: 500px;
  max-height: 80vh;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.15);
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  border-bottom: 1px solid var(--border-color);
}

.modal-header h3 {
  margin: 0;
  font-size: 20px;
  color: var(--text-primary);
}

.close-btn {
  background: none;
  border: none;
  font-size: 24px;
  color: var(--text-secondary);
  cursor: pointer;
  width: 30px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  transition: all 0.3s;
}

.close-btn:hover {
  background-color: var(--hover-background);
  color: var(--text-primary);
}

.modal-body {
  padding: 20px;
  overflow-y: auto;
  flex: 1;
}

.password-form, .phone-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.verification-code-container {
  display: flex;
  gap: 10px;
  align-items: stretch;
}

.verification-input {
  flex: 1;
}

.send-code-btn {
  white-space: nowrap;
  min-width: 150px;
}

.password-error, .phone-error {
  text-align: center;
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 10px;
}

/* 成功提示 */
.success-toast {
  position: fixed;
  top: 20px;
  right: 20px;
  background-color: #52c41a;
  color: white;
  padding: 15px 20px;
  border-radius: var(--border-radius);
  display: flex;
  align-items: center;
  gap: 10px;
  box-shadow: 0 4px 12px rgba(82, 196, 26, 0.3);
  animation: slideIn 0.3s ease-out;
  z-index: 2000;
}

.success-icon {
  font-size: 20px;
}

@keyframes slideIn {
  from {
    transform: translateX(100%);
    opacity: 0;
  }
  to {
    transform: translateX(0);
    opacity: 1;
  }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .user-info-container {
    padding: 15px;
  }

  .theme-options {
    flex-direction: column;
    gap: 10px;
  }

  .security-actions {
    flex-direction: column;
  }

  .form-actions {
    flex-direction: column;
  }

  .modal-actions {
    flex-direction: column;
  }

  .verification-code-container {
    flex-direction: column;
  }

  .send-code-btn {
    width: 100%;
  }
}
</style>

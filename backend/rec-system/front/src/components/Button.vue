<template>
  <button
    :class="[
      'bili-button',
      `bili-button--${type}`,
      `bili-button--${size}`,
      { 'bili-button--disabled': disabled },
      { 'bili-button--loading': loading },
      { 'bili-button--block': block }
    ]"
    :disabled="disabled || loading"
    @click="handleClick"
  >
    <span v-if="loading" class="bili-button__loading">⏳</span>
    <span v-else-if="icon" class="bili-button__icon">{{ icon }}</span>
    <span v-if="$slots.default" class="bili-button__text"><slot /></span>
  </button>
</template>

<script>
export default {
  name: 'Button',
  props: {
    type: {
      type: String,
      default: 'primary',
      validator: (value) => ['primary', 'secondary', 'outline', 'text'].includes(value)
    },
    size: {
      type: String,
      default: 'medium',
      validator: (value) => ['small', 'medium', 'large'].includes(value)
    },
    disabled: {
      type: Boolean,
      default: false
    },
    loading: {
      type: Boolean,
      default: false
    },
    icon: {
      type: String,
      default: ''
    },
    block: {
      type: Boolean,
      default: false
    }
  },
  methods: {
    handleClick(event) {
      this.$emit('click', event)
    }
  }
}
</script>

<style scoped>
.bili-button {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  border: none;
  border-radius: var(--radius-md);
  font-family: inherit;
  font-size: var(--font-sm);
  font-weight: 500;
  cursor: pointer;
  transition: all var(--transition-fast);
  outline: none;
  user-select: none;
}

/* 尺寸变体 */
.bili-button--small {
  padding: 4px 12px;
  font-size: var(--font-xs);
}

.bili-button--medium {
  padding: 8px 16px;
  font-size: var(--font-sm);
}

.bili-button--large {
  padding: 12px 24px;
  font-size: var(--font-md);
}

/* 类型变体 */
.bili-button--primary {
  background-color: var(--primary-color);
  color: var(--text-light);
}

.bili-button--primary:hover:not(:disabled) {
  background-color: var(--primary-hover);
  transform: translateY(-1px);
}

.bili-button--secondary {
  background-color: var(--secondary-color);
  color: var(--text-light);
}

.bili-button--secondary:hover:not(:disabled) {
  background-color: #1DA5E8;
  transform: translateY(-1px);
}

.bili-button--outline {
  background-color: transparent;
  color: var(--primary-color);
  border: 1px solid var(--primary-color);
}

.bili-button--outline:hover:not(:disabled) {
  background-color: var(--bg-hover);
}

.bili-button--text {
  background-color: transparent;
  color: var(--text-secondary);
  border: none;
}

.bili-button--text:hover:not(:disabled) {
  color: var(--primary-color);
  background-color: var(--bg-hover);
}

/* 状态变体 */
.bili-button--disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none !important;
}

.bili-button--loading {
  cursor: wait;
  opacity: 0.8;
}

.bili-button--block {
  display: flex;
  width: 100%;
}

/* 内部元素 */
.bili-button__icon,
.bili-button__loading {
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: inherit;
}

.bili-button__text {
  white-space: nowrap;
}

/* 点击效果 */
.bili-button:active:not(:disabled) {
  transform: translateY(0);
}

/* 聚焦效果 */
.bili-button:focus-visible {
  box-shadow: 0 0 0 3px rgba(251, 114, 153, 0.2);
}
</style>
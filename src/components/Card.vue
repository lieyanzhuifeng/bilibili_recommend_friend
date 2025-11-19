<template>
  <div :class="['bili-card', { 'bili-card--shadow': shadow }]">
    <div v-if="title" class="bili-card__header">
      <h3 class="bili-card__title">{{ title }}</h3>
      <slot name="header-right"></slot>
    </div>
    <div class="bili-card__body">
      <slot></slot>
    </div>
    <div v-if="footer || $slots.footer" class="bili-card__footer">
      <slot name="footer">{{ footer }}</slot>
    </div>
  </div>
</template>

<script>
export default {
  name: 'Card',
  props: {
    title: {
      type: String,
      default: ''
    },
    footer: {
      type: String,
      default: ''
    },
    shadow: {
      type: Boolean,
      default: true
    }
  }
}
</script>

<style scoped>
.bili-card {
  background-color: var(--bg-primary);
  border-radius: var(--radius-lg);
  overflow: hidden;
  transition: all var(--transition-normal);
}

.bili-card--shadow {
  box-shadow: var(--shadow-light);
}

.bili-card:hover {
  box-shadow: var(--shadow-medium);
}

.bili-card__header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--space-lg);
  border-bottom: 1px solid var(--border-color);
}

.bili-card__title {
  margin: 0;
  font-size: var(--font-lg);
  font-weight: 600;
  color: var(--text-primary);
}

.bili-card__body {
  padding: var(--space-lg);
}

.bili-card__footer {
  padding: var(--space-lg);
  border-top: 1px solid var(--border-color);
  color: var(--text-secondary);
  font-size: var(--font-sm);
  text-align: center;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .bili-card__header,
  .bili-card__body,
  .bili-card__footer {
    padding: var(--space-md);
  }
  
  .bili-card__title {
    font-size: var(--font-md);
  }
}
</style>
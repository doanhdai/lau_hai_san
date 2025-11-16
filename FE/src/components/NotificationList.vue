<template>
  <div class="fixed top-4 right-4 z-[10000] space-y-2 max-w-sm">
    <transition-group name="notification">
      <div
        v-for="notification in notificationStore.notifications"
        :key="notification.id"
        class="p-4 rounded-lg shadow-lg flex items-center gap-3 animate-slide-in"
        :class="getNotificationClass(notification.type)"
      >
        <component :is="getIcon(notification.type)" class="w-6 h-6 flex-shrink-0" />
        <p class="flex-1 text-sm font-medium">{{ notification.message }}</p>
        <button @click="notificationStore.remove(notification.id)" class="flex-shrink-0">
          <XMarkIcon class="w-5 h-5" />
        </button>
      </div>
    </transition-group>
  </div>
</template>

<script setup>
import { useNotificationStore } from '@/stores/notification'
import {
  CheckCircleIcon,
  ExclamationCircleIcon,
  InformationCircleIcon,
  XCircleIcon,
  XMarkIcon
} from '@heroicons/vue/24/outline'

const notificationStore = useNotificationStore()

function getNotificationClass(type) {
  const classes = {
    success: 'bg-green-50 text-green-800 border border-green-200',
    error: 'bg-red-50 text-red-800 border border-red-200',
    warning: 'bg-yellow-50 text-yellow-800 border border-yellow-200',
    info: 'bg-blue-50 text-blue-800 border border-blue-200'
  }
  return classes[type] || classes.info
}

function getIcon(type) {
  const icons = {
    success: CheckCircleIcon,
    error: XCircleIcon,
    warning: ExclamationCircleIcon,
    info: InformationCircleIcon
  }
  return icons[type] || InformationCircleIcon
}
</script>

<style scoped>
.notification-enter-active {
  transition: all 0.3s ease-out;
}

.notification-leave-active {
  transition: all 0.2s ease-in;
}

.notification-enter-from {
  transform: translateX(100%);
  opacity: 0;
}

.notification-leave-to {
  transform: translateX(100%);
  opacity: 0;
}
</style>

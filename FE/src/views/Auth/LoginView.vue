<template>
  <div
    class="min-h-screen flex items-center justify-center bg-gradient-to-br from-sky-500 via-sky-600 to-blue-700 py-12 px-4 sm:px-6 lg:px-8 relative overflow-hidden"
  >
    <!-- Background Animation -->
    <div class="absolute inset-0 overflow-hidden">
      <div
        class="absolute -top-40 -right-40 w-80 h-80 bg-sky-400 rounded-full opacity-20 blur-3xl animate-pulse-slow"
      ></div>
      <div
        class="absolute -bottom-40 -left-40 w-80 h-80 bg-blue-800 rounded-full opacity-20 blur-3xl animate-pulse-slow"
        style="animation-delay: 1s"
      ></div>
    </div>

    <div class="max-w-md w-full space-y-8 relative z-10">
      <!-- Logo & Title -->
      <div class="text-center">
        <div class="flex justify-center mb-4">
          <div
            class="w-20 h-20 bg-white rounded-full flex items-center justify-center shadow-2xl animate-bounce-slow"
          >
            <span class="text-5xl">🍲</span>
          </div>
        </div>
        <h2 class="text-4xl font-extrabold text-white drop-shadow-lg">
          Nhà Hàng Lẫu
        </h2>
        <p class="mt-2 text-lg text-sky-100">Hệ thống quản lý chuyên nghiệp</p>
      </div>

      <!-- Login Form -->
      <div
        class="bg-white rounded-2xl shadow-2xl p-8 space-y-6 transform transition-all hover:scale-[1.02]"
      >
        <div>
          <h3 class="text-2xl font-bold text-gray-900 text-center">
            Đăng nhập
          </h3>
          <p class="mt-2 text-center text-sm text-gray-600">
            Chào mừng quay trở lại!
          </p>
        </div>

        <form @submit.prevent="handleLogin" class="space-y-6">
          <!-- Username -->
          <div>
            <label
              for="username"
              class="block text-sm font-medium text-gray-700 mb-2"
            >
              Tên đăng nhập
            </label>
            <input
              id="username"
              v-model="form.username"
              type="text"
              required
              class="input-field"
              placeholder="Nhập tên đăng nhập"
            />
          </div>

          <!-- Password -->
          <div>
            <label
              for="password"
              class="block text-sm font-medium text-gray-700 mb-2"
            >
              Mật khẩu
            </label>
            <input
              id="password"
              v-model="form.password"
              type="password"
              required
              class="input-field"
              placeholder="Nhập mật khẩu"
            />
          </div>

          <!-- Error Message -->
          <div
            v-if="authStore.error"
            class="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg text-sm"
          >
            {{ authStore.error }}
          </div>

          <!-- Submit Button -->
          <button
            type="submit"
            :disabled="authStore.loading"
            class="w-full btn-primary relative overflow-hidden"
          >
            <span v-if="!authStore.loading">Đăng nhập</span>
            <span v-else class="flex items-center justify-center gap-2">
              <div
                class="w-5 h-5 border-2 border-white border-t-transparent rounded-full animate-spin"
              ></div>
              Đang xử lý...
            </span>
          </button>
        </form>

        <!-- Footer -->
        <div class="text-center">
          <p class="text-sm text-gray-600">
            Chưa có tài khoản?
            <router-link
              to="/register"
              class="font-medium text-sky-600 hover:text-sky-500 transition"
            >
              Đăng ký ngay
            </router-link>
          </p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from "vue";
import { useRouter } from "vue-router";
import { useAuthStore } from "@/stores/auth";

const router = useRouter();
const authStore = useAuthStore();

const form = ref({
  username: "",
  password: "",
});

async function handleLogin() {
  const result = await authStore.login(form.value);

  if (result && result.success) {
    // Redirect based on role
    const roles = result.userData.roles || [];
    if (
      roles.includes("ROLE_ADMIN") ||
      roles.includes("ROLE_MANAGER") ||
      roles.includes("ROLE_STAFF")
    ) {
      router.push("/admin/dashboard");
    } else {
      router.push("/home");
    }
  }
}
</script>

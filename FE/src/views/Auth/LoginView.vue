<template>
  <div
    class="min-h-screen flex items-center justify-center bg-slate-900 py-12 px-4 sm:px-6 lg:px-8 relative"
  >
    <!-- Background Pattern -->
    <div class="absolute inset-0 bg-black/40"></div>

    <div class="max-w-md w-full space-y-8 relative z-10">
      <!-- Logo & Title -->
      <div class="text-center">
        <div class="flex justify-center mb-4">
          <div
            class="w-16 h-16 bg-white rounded-lg flex items-center justify-center shadow-lg"
          >
            <i class="fas fa-bowl-food text-slate-900 text-3xl"></i>
          </div>
        </div>
        <h2 class="text-3xl md:text-4xl font-bold text-white mb-2">
          Nhà Hàng Lẫu
        </h2>
        <p class="text-base text-slate-200">Hệ thống quản lý chuyên nghiệp</p>
      </div>

      <!-- Login Form -->
      <div
        class="bg-white rounded-lg shadow-xl p-8 border border-gray-200"
      >
        <div class="mb-6">
          <h3 class="text-2xl font-bold text-slate-900 text-center">
            Đăng nhập
          </h3>
          <p class="mt-2 text-center text-sm text-slate-600">
            Chào mừng quay trở lại!
          </p>
        </div>

        <form @submit.prevent="handleLogin" class="space-y-5">
          <!-- Username -->
          <div>
            <label
              for="username"
              class="block text-sm font-medium text-slate-700 mb-2"
            >
              Tên đăng nhập
            </label>
            <input
              id="username"
              v-model="form.username"
              type="text"
              required
              class="w-full px-4 py-2.5 border border-gray-300 rounded-lg focus:ring-2 focus:ring-slate-500 focus:border-transparent transition"
              placeholder="Nhập tên đăng nhập"
            />
          </div>

          <!-- Password -->
          <div>
            <label
              for="password"
              class="block text-sm font-medium text-slate-700 mb-2"
            >
              Mật khẩu
            </label>
            <input
              id="password"
              v-model="form.password"
              type="password"
              required
              class="w-full px-4 py-2.5 border border-gray-300 rounded-lg focus:ring-2 focus:ring-slate-500 focus:border-transparent transition"
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
            class="w-full bg-slate-900 hover:bg-slate-800 text-white py-2.5 rounded-lg font-semibold transition-colors disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center gap-2"
          >
            <span v-if="!authStore.loading">Đăng nhập</span>
            <span v-else class="flex items-center justify-center gap-2">
              <i class="fas fa-spinner fa-spin"></i>
              <span>Đang xử lý...</span>
            </span>
          </button>
        </form>

        <!-- Footer -->
        <div class="text-center mt-6">
          <p class="text-sm text-slate-600">
            Chưa có tài khoản?
            <router-link
              to="/register"
              class="font-medium text-slate-900 hover:text-slate-700 transition"
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

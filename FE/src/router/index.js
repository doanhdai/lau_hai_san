import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    // Public Routes
    {
      path: '/',
      component: () => import('@/layouts/PublicLayout.vue'),
      meta: { requiresAuth: false },
      children: [
        {
          path: '',
          redirect: '/home'
        },
        {
          path: 'home',
          name: 'PublicHome',
          component: () => import('@/views/Public/HomePage.vue')
        },
        {
          path: 'services',
          name: 'Services',
          component: () => import('@/views/Public/ServicesPage.vue')
        },
        {
          path: 'menu',
          name: 'PublicMenu',
          component: () => import('@/views/Public/MenuPage.vue')
        },
        {
          path: 'about',
          name: 'About',
          component: () => import('@/views/Public/AboutPage.vue')
        },
        {
          path: 'reservation',
          name: 'PublicReservation',
          component: () => import('@/views/Public/ReservationPage.vue')
        },
        {
          path: 'my-reservations',
          name: 'MyReservations',
          component: () => import('@/views/Public/MyReservationsPage.vue')
        },
        {
          path: 'sitemap',
          name: 'SiteMap',
          component: () => import('@/views/Public/SiteMapPage.vue')
        }
      ]
    },
    {
      path: '/login',
      name: 'Login',
      component: () => import('@/views/Auth/LoginView.vue'),
      meta: { requiresAuth: false }
    },
    {
      path: '/register',
      name: 'Register',
      component: () => import('@/views/Auth/RegisterView.vue'),
      meta: { requiresAuth: false }
    },
    // Admin Routes
    {
      path: '/admin',
      component: () => import('@/layouts/MainLayout.vue'),
      meta: { requiresAuth: true },
      children: [
        {
          path: '',
          redirect: '/admin/dashboard'
        },
        {
          path: 'dashboard',
          name: 'Dashboard',
          component: () => import('@/views/DashboardView.vue'),
          meta: { roles: ['ROLE_ADMIN'] }
        },
        {
          path: 'users',
          name: 'Users',
          component: () => import('@/views/Users/UserList.vue'),
          meta: { roles: ['ROLE_ADMIN'] }
        },
        {
          path: 'customers',
          name: 'Customers',
          component: () => import('@/views/Customers/CustomerList.vue'),
          meta: { roles: ['ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_STAFF'] }
        },
        {
          path: 'tables',
          name: 'Tables',
          component: () => import('@/views/Tables/TableMap.vue'),
          meta: { roles: ['ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_STAFF'] }
        },
        {
          path: 'dishes',
          name: 'Dishes',
          component: () => import('@/views/Dishes/DishList.vue'),
          meta: { roles: ['ROLE_ADMIN', 'ROLE_MANAGER'] }
        },
        {
          path: 'categories',
          name: 'Categories',
          component: () => import('@/views/Categories/CategoryList.vue'),
          meta: { roles: ['ROLE_ADMIN', 'ROLE_MANAGER'] }
        },
        {
          path: 'orders',
          name: 'Orders',
          component: () => import('@/views/Orders/OrderList.vue'),
          meta: { roles: ['ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_STAFF'] }
        },
        {
          path: 'orders/create',
          name: 'CreateOrder',
          component: () => import('@/views/Orders/CreateOrder.vue'),
          meta: { roles: ['ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_STAFF'] }
        },
        {
          path: 'reservations',
          name: 'Reservations',
          component: () => import('@/views/Reservations/ReservationList.vue'),
          meta: { roles: ['ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_STAFF'] }
        },
        {
          path: 'reservations/checkin',
          name: 'TableCheckIn',
          component: () => import('@/views/Reservations/TableCheckIn.vue'),
          meta: { roles: ['ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_STAFF'] }
        },
        {
          path: 'promotions',
          name: 'Promotions',
          component: () => import('@/views/Promotions/PromotionList.vue'),
          meta: { roles: ['ROLE_ADMIN', 'ROLE_MANAGER'] }
        },
        {
          path: 'feedbacks',
          name: 'Feedbacks',
          component: () => import('@/views/Feedbacks/FeedbackList.vue'),
          meta: { roles: ['ROLE_ADMIN', 'ROLE_MANAGER'] }
        },
        {
          path: 'reports',
          name: 'Reports',
          component: () => import('@/views/Reports/ReportsView.vue'),
          meta: { roles: ['ROLE_ADMIN', 'ROLE_MANAGER'] }
        }
      ]
    },
    
    // Staff Routes
    {
      path: '/staff',
      component: () => import('@/layouts/MainLayout.vue'),
      meta: { requiresAuth: true },
      children: [
        {
          path: '',
          redirect: '/staff/dashboard'
        },
        {
          path: 'dashboard',
          name: 'StaffDashboard',
          component: () => import('@/views/Staff/StaffDashboard.vue'),
          meta: { roles: ['ROLE_STAFF'] }
        },
        {
          path: 'counter-order',
          name: 'CounterOrder',
          component: () => import('@/views/Staff/CounterOrder.vue'),
          meta: { roles: ['ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_STAFF'] }
        }
      ]
    },
    
    // Manager Routes
    {
      path: '/manager',
      component: () => import('@/layouts/MainLayout.vue'),
      meta: { requiresAuth: true },
      children: [
        {
          path: '',
          redirect: '/manager/dashboard'
        },
        {
          path: 'dashboard',
          name: 'ManagerDashboard',
          component: () => import('@/views/Manager/ManagerDashboard.vue'),
          meta: { roles: ['ROLE_MANAGER'] }
        }
      ]
    }
  ]
})

router.beforeEach(async (to, from, next) => {
  // Dynamically import authStore to avoid circular dependency
  const { useAuthStore } = await import('@/stores/auth')
  const authStore = useAuthStore()
  
  if (to.meta.requiresAuth && !authStore.isAuthenticated) {
    next('/login')
  } else if (to.path === '/login' && authStore.isAuthenticated) {
    // Redirect based on user role
    const userRoles = authStore.user?.roles || []
    if (userRoles.includes('ROLE_ADMIN')) {
      next('/admin/dashboard')
    } else if (userRoles.includes('ROLE_MANAGER')) {
      next('/manager/dashboard')
    } else if (userRoles.includes('ROLE_STAFF')) {
      next('/staff/dashboard')
    } else {
      // CUSTOMER goes to home page
      next('/home')
    }
  } else if (to.meta.roles) {
    // Check role-based access
    const userRoles = authStore.user?.roles || []
    const hasAccess = to.meta.roles.some(role => userRoles.includes(role))
    
    if (!hasAccess) {
      // Redirect based on user role
      console.warn('Access denied: User does not have required role')
      if (userRoles.includes('ROLE_ADMIN')) {
        next('/admin/dashboard')
      } else if (userRoles.includes('ROLE_MANAGER')) {
        next('/manager/dashboard')
      } else if (userRoles.includes('ROLE_STAFF')) {
        next('/staff/dashboard')
      } else {
        // CUSTOMER goes to home page
        next('/home')
      }
    } else {
      next()
    }
  } else {
    next()
  }
})

export default router

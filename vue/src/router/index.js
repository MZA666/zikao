import {createRouter, createWebHistory} from 'vue-router'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    { path: '/', redirect: '/login' },
    {
      path: '/manager',
      component: () => import('@/views/Manager.vue'),
      redirect: '/manager/home',
      children: [
        { path: 'home', component: () => import('@/views/manager/Home.vue')},
        { path: 'admin', component: () => import('@/views/manager/Admin.vue')},
        { path: 'file-audit', component: () => import('@/views/manager/FileAudit.vue')},
        { path: 'file-preview', component: () => import('@/views/manager/FilePreview.vue'), name: 'FilePreview'},
      ]
    },
    {
      path: '/front',
      component: () => import('@/views/front/Home.vue'),
      redirect: '/front/home',
      children: [
        { path: 'home', component: () => import('@/views/front/Home.vue')},
        { path: 'course-materials', component: () => import('@/views/front/CourseMaterials.vue')},
        { path: 'exam-bank', component: () => import('@/views/front/ExamBank.vue')},
        { path: 'student-forum', component: () => import('@/views/front/StudentForum.vue')},
      ]
    },
    {
      path: '/front-file-preview',
      component: () => import('@/views/front/FrontFilePreview.vue'),
      name: 'FrontFilePreview'
    },
    { path: '/login', component: () => import('@/views/Login.vue') },
    { path: '/register', component: () => import('@/views/Register.vue') },
  ]
})

export default router
import { ElMessage } from 'element-plus'
import router from '../router'
import axios from "axios";

const request = axios.create({
    baseURL: import.meta.env.VITE_BASE_URL,
    timeout: 60000  // 增加超时时间到60秒
})

// request 拦截器
// 可以自请求发送前对请求做一些处理
request.interceptors.request.use(config => {
    // 对于文件上传请求，不要设置Content-Type为application/json
    if (config.data instanceof FormData) {
        // 如果是FormData格式（文件上传），则不设置Content-Type，让浏览器自动设置
        delete config.headers['Content-Type'];
    } else {
        config.headers['Content-Type'] = 'application/json;charset=utf-8';
    }
    return config
}, error => {
    return Promise.reject(error)
});

// response 拦截器
// 可以在接口响应后统一处理结果
request.interceptors.response.use(
    response => {
        let res = response.data;
        // 如果是返回的文件
        if (response.config.responseType === 'blob') {
            return res
        }
        // 兼容服务端返回的字符串数据
        if (typeof res === 'string') {
            res = res ? JSON.parse(res) : res
        }
        // 当权限验证不通过的时候给出提示
        if (res.code === '401') {
            ElMessage.error(res.msg);
            router.push("/login")
        }
        return res;
    },
        error => {
        console.log('err' + error)
        // 检查是否是超时错误
        if (error.code === 'ECONNABORTED' || error.message.includes('timeout')) {
            ElMessage.error('请求超时，请检查网络连接或后端服务是否正常运行');
        } else if (error.response) {
            // 服务器返回了错误状态码
            const status = error.response.status;
            if (status === 401) {
                ElMessage.error('登录已过期，请重新登录');
                router.push("/login");
            } else if (status === 500) {
                ElMessage.error('服务器内部错误，请稍后重试');
            } else {
                ElMessage.error(`请求失败: ${error.response.data?.msg || error.response.statusText}`);
            }
        } else {
            // 网络错误或其他错误
            ElMessage.error('网络错误或后端服务未启动，请检查后端服务是否正常运行');
        }
        return Promise.reject(error)
    }
)


export default request
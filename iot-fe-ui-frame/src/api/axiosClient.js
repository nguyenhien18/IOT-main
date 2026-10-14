import axios from 'axios'

const axiosClient = axios.create({
  baseURL: import.meta.env.VITE_API_URL || 'http://localhost:8080',
  headers: { 'Content-Type': 'application/json' },
})

axiosClient.interceptors.request.use((config) => {
  const token = localStorage.getItem('token')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

axiosClient.interceptors.response.use(
  (response) => {
    const payload = response.data
    if (
      payload &&
      typeof payload === 'object' &&
      Object.prototype.hasOwnProperty.call(payload, 'result')
    ) {
      response.data = payload.result
    }
    return response
  },
  (error) => {
    const status = error.response?.status
    if ((status === 401 || status === 403) && window.location.pathname !== '/login') {
      localStorage.removeItem('token')
      localStorage.removeItem('username')
      window.location.assign('/login')
    }
    return Promise.reject(error)
  },
)

export default axiosClient

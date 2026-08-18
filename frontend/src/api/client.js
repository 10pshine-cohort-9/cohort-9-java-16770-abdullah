import axios from 'axios'
import { readStored, TOKEN_KEY } from '../lib/storage'

const client = axios.create({
  baseURL: '/api',
  headers: { 'Content-Type': 'application/json' },
})

// Attach the JWT to every request if we have one stored.
client.interceptors.request.use((config) => {
  const token = readStored(TOKEN_KEY)
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// The backend returns errors as { message, details: [...] }, so pull out
// something readable instead of letting axios throw "Request failed with 400".
client.interceptors.response.use(
  (response) => response,
  (error) => {
    const data = error.response?.data
    let message = data?.message || 'Something went wrong, please try again.'
    if (data?.details?.length) {
      message = data.details.join(', ')
    }
    return Promise.reject(new Error(message))
  },
)

export default client

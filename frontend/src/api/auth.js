import client from './client'

export function register({ email, phoneNumber, password }) {
  return client
    .post('/auth/register', {
      email: email || null,
      phoneNumber: phoneNumber || null,
      password,
    })
    .then((res) => res.data)
}

export function login({ identifier, password }) {
  return client.post('/auth/login', { identifier, password }).then((res) => res.data)
}

export function changePassword({ currentPassword, newPassword }) {
  return client.put('/users/me/password', { currentPassword, newPassword })
}

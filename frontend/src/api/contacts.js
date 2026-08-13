import client from './client'

export function fetchContacts({ page = 0, size = 10, search = '' } = {}) {
  const params = { page, size }
  if (search) {
    params.search = search
  }
  return client.get('/contacts', { params }).then((res) => res.data)
}

export function fetchContact(id) {
  return client.get(`/contacts/${id}`).then((res) => res.data)
}

export function createContact(contact) {
  return client.post('/contacts', contact).then((res) => res.data)
}

export function updateContact(id, contact) {
  return client.put(`/contacts/${id}`, contact).then((res) => res.data)
}

export function deleteContact(id) {
  return client.delete(`/contacts/${id}`)
}

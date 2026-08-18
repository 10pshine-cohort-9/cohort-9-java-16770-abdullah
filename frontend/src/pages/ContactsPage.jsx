import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { fetchContacts } from '../api/contacts'
import { useAuth } from '../context/auth-context'

const PAGE_SIZE = 10

export default function ContactsPage() {
  const [contacts, setContacts] = useState([])
  const [page, setPage] = useState(0)
  const [totalPages, setTotalPages] = useState(0)
  const [totalElements, setTotalElements] = useState(0)
  const [searchInput, setSearchInput] = useState('')
  const [search, setSearch] = useState('')
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  const { signOut } = useAuth()
  const navigate = useNavigate()

  // Wait for a pause in typing before hitting the API, otherwise every
  // keystroke fires a request.
  useEffect(() => {
    const timer = setTimeout(() => {
      setSearch(searchInput.trim())
      setPage(0)
    }, 300)
    return () => clearTimeout(timer)
  }, [searchInput])

  useEffect(() => {
    // Typing in the search box or paging quickly leaves more than one
    // request in flight, and they don't necessarily come back in order.
    // Ignore anything that resolves after we've already moved on.
    let ignore = false

    async function loadContacts() {
      setLoading(true)
      setError('')
      try {
        const data = await fetchContacts({ page, size: PAGE_SIZE, search })
        if (ignore) return
        setContacts(data.content)
        setTotalPages(data.totalPages)
        setTotalElements(data.totalElements)
      } catch (err) {
        if (ignore) return
        setError(err.message)
      } finally {
        if (!ignore) setLoading(false)
      }
    }

    loadContacts()
    return () => {
      ignore = true
    }
  }, [page, search])

  function handleSignOut() {
    signOut()
    navigate('/login')
  }

  return (
    <div className="page">
      <header className="page-header">
        <h1>Contacts</h1>
        <button type="button" className="secondary" onClick={handleSignOut}>
          Sign out
        </button>
      </header>

      <input
        type="search"
        placeholder="Search by first or last name"
        value={searchInput}
        onChange={(e) => setSearchInput(e.target.value)}
      />

      {error && <p className="error">{error}</p>}

      {loading ? (
        <p className="muted">Loading contacts...</p>
      ) : contacts.length === 0 ? (
        <p className="muted">{search ? `No contacts match "${search}".` : 'No contacts yet.'}</p>
      ) : (
        <>
          <table className="contacts-table">
            <thead>
              <tr>
                <th>Name</th>
                <th>Title</th>
                <th>Email</th>
                <th>Phone</th>
              </tr>
            </thead>
            <tbody>
              {contacts.map((contact) => (
                <tr key={contact.id}>
                  <td>
                    {contact.firstName} {contact.lastName}
                  </td>
                  <td>{contact.title || '-'}</td>
                  <td>{contact.emails[0]?.email || '-'}</td>
                  <td>{contact.phones[0]?.phoneNumber || '-'}</td>
                </tr>
              ))}
            </tbody>
          </table>

          <div className="pagination">
            <button
              type="button"
              className="secondary"
              onClick={() => setPage((current) => current - 1)}
              disabled={page === 0}
            >
              Previous
            </button>
            <span className="muted">
              Page {page + 1} of {totalPages} ({totalElements} total)
            </span>
            <button
              type="button"
              className="secondary"
              onClick={() => setPage((current) => current + 1)}
              disabled={page >= totalPages - 1}
            >
              Next
            </button>
          </div>
        </>
      )}
    </div>
  )
}

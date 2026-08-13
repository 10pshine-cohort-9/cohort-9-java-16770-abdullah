import { useNavigate } from 'react-router-dom'
import { useAuth } from '../context/auth-context'

export default function ContactsPage() {
  const { signOut } = useAuth()
  const navigate = useNavigate()

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

      <p className="muted">The contacts list is next.</p>
    </div>
  )
}

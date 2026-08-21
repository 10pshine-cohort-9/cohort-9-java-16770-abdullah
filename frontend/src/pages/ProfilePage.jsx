import { useEffect, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { fetchProfile } from '../api/auth'
import ChangePasswordModal from '../components/ChangePasswordModal'
import { useAuth } from '../context/auth-context'

export default function ProfilePage() {
  const [profile, setProfile] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [showChangePassword, setShowChangePassword] = useState(false)
  const [notice, setNotice] = useState('')

  const { signOut } = useAuth()
  const navigate = useNavigate()

  useEffect(() => {
    let ignore = false

    async function loadProfile() {
      try {
        const data = await fetchProfile()
        if (!ignore) setProfile(data)
      } catch (err) {
        if (!ignore) setError(err.message)
      } finally {
        if (!ignore) setLoading(false)
      }
    }

    loadProfile()
    return () => {
      ignore = true
    }
  }, [])

  function handleSignOut() {
    signOut()
    navigate('/login')
  }

  function handlePasswordChanged() {
    setShowChangePassword(false)
    setNotice('Your password has been changed.')
  }

  return (
    <div className="page">
      <header className="page-header">
        <h1>Profile</h1>
        <div className="header-actions">
          <Link to="/contacts">Contacts</Link>
          <button type="button" className="secondary" onClick={handleSignOut}>
            Sign out
          </button>
        </div>
      </header>

      {error && <p className="error">{error}</p>}
      {notice && <p className="notice">{notice}</p>}

      {loading ? (
        <p className="muted">Loading profile...</p>
      ) : (
        profile && (
          <div className="details">
            <dl>
              <dt>Email</dt>
              <dd>{profile.email || 'Not set'}</dd>

              <dt>Phone number</dt>
              <dd>{profile.phoneNumber || 'Not set'}</dd>

              <dt>Member since</dt>
              <dd>{new Date(profile.createdAt).toLocaleDateString()}</dd>
            </dl>

            <button type="button" onClick={() => setShowChangePassword(true)}>
              Change password
            </button>
          </div>
        )
      )}

      {showChangePassword && (
        <ChangePasswordModal
          onClose={() => setShowChangePassword(false)}
          onChanged={handlePasswordChanged}
        />
      )}
    </div>
  )
}

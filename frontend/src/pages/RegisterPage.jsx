import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { register } from '../api/auth'
import { useAuth } from '../context/auth-context'

export default function RegisterPage() {
  const [email, setEmail] = useState('')
  const [phoneNumber, setPhoneNumber] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [submitting, setSubmitting] = useState(false)

  const { signIn } = useAuth()
  const navigate = useNavigate()

  async function handleSubmit(event) {
    event.preventDefault()
    setError('')

    if (!email && !phoneNumber) {
      setError('Enter an email address or a phone number.')
      return
    }

    setSubmitting(true)
    try {
      const auth = await register({ email, phoneNumber, password })
      signIn(auth)
      navigate('/contacts')
    } catch (err) {
      setError(err.message)
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="auth-page">
      <form className="card" onSubmit={handleSubmit}>
        <h1>Create an account</h1>
        <p className="muted">You can sign up with an email, a phone number, or both.</p>

        {error && <p className="error">{error}</p>}

        <label htmlFor="email">Email</label>
        <input id="email" type="email" value={email} onChange={(e) => setEmail(e.target.value)} />

        <label htmlFor="phoneNumber">Phone number</label>
        <input
          id="phoneNumber"
          value={phoneNumber}
          onChange={(e) => setPhoneNumber(e.target.value)}
        />

        <label htmlFor="password">Password</label>
        <input
          id="password"
          type="password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          minLength={8}
          required
        />

        <button type="submit" disabled={submitting}>
          {submitting ? 'Creating account...' : 'Register'}
        </button>

        <p className="muted">
          Already registered? <Link to="/login">Sign in</Link>
        </p>
      </form>
    </div>
  )
}

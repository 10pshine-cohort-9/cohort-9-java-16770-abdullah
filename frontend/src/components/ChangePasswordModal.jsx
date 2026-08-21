import { useState } from 'react'
import { changePassword } from '../api/auth'
import Modal from './Modal'

export default function ChangePasswordModal({ onClose, onChanged }) {
  const [currentPassword, setCurrentPassword] = useState('')
  const [newPassword, setNewPassword] = useState('')
  const [confirmPassword, setConfirmPassword] = useState('')
  const [error, setError] = useState('')
  const [saving, setSaving] = useState(false)

  async function handleSubmit(event) {
    event.preventDefault()
    setError('')

    if (newPassword !== confirmPassword) {
      setError('The new passwords do not match.')
      return
    }

    setSaving(true)
    try {
      await changePassword({ currentPassword, newPassword })
      onChanged()
    } catch (err) {
      setError(err.message)
      setSaving(false)
    }
  }

  return (
    <Modal title="Change password" onClose={onClose}>
      <form onSubmit={handleSubmit}>
        {error && <p className="error">{error}</p>}

        <label htmlFor="currentPassword">Current password</label>
        <input
          id="currentPassword"
          type="password"
          value={currentPassword}
          onChange={(e) => setCurrentPassword(e.target.value)}
          required
        />

        <label htmlFor="newPassword">New password</label>
        <input
          id="newPassword"
          type="password"
          value={newPassword}
          onChange={(e) => setNewPassword(e.target.value)}
          minLength={8}
          required
        />

        <label htmlFor="confirmPassword">Confirm new password</label>
        <input
          id="confirmPassword"
          type="password"
          value={confirmPassword}
          onChange={(e) => setConfirmPassword(e.target.value)}
          minLength={8}
          required
        />

        <div className="modal-actions">
          <button type="button" className="secondary" onClick={onClose} disabled={saving}>
            Cancel
          </button>
          <button type="submit" disabled={saving}>
            {saving ? 'Saving...' : 'Reset password'}
          </button>
        </div>
      </form>
    </Modal>
  )
}

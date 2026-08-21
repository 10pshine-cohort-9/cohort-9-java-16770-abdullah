import { useState } from 'react'
import { deleteContact } from '../api/contacts'
import Modal from './Modal'

export default function ConfirmDeleteModal({ contact, onClose, onDeleted }) {
  const [error, setError] = useState('')
  const [deleting, setDeleting] = useState(false)

  async function handleConfirm() {
    setError('')
    setDeleting(true)
    try {
      await deleteContact(contact.id)
      onDeleted()
    } catch (err) {
      setError(err.message)
      setDeleting(false)
    }
  }

  return (
    <Modal title="Delete contact" onClose={onClose}>
      {error && <p className="error">{error}</p>}

      <p>
        Delete{' '}
        <strong>
          {contact.firstName} {contact.lastName}
        </strong>
        ? This can&apos;t be undone.
      </p>

      <div className="modal-actions">
        <button type="button" className="secondary" onClick={onClose} disabled={deleting}>
          Cancel
        </button>
        <button type="button" className="danger" onClick={handleConfirm} disabled={deleting}>
          {deleting ? 'Deleting...' : 'Delete'}
        </button>
      </div>
    </Modal>
  )
}

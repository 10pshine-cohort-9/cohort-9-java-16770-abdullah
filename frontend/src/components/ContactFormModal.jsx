import { useState } from 'react'
import { createContact, updateContact } from '../api/contacts'
import Modal from './Modal'

const LABELS = ['WORK', 'HOME', 'PERSONAL', 'OTHER']

function toFormState(contact) {
  if (!contact) {
    return { firstName: '', lastName: '', title: '', emails: [], phones: [] }
  }
  return {
    firstName: contact.firstName,
    lastName: contact.lastName,
    title: contact.title || '',
    emails: contact.emails.map((e) => ({ label: e.label, email: e.email })),
    phones: contact.phones.map((p) => ({ label: p.label, phoneNumber: p.phoneNumber })),
  }
}

export default function ContactFormModal({ contact, onClose, onSaved }) {
  const [form, setForm] = useState(() => toFormState(contact))
  const [error, setError] = useState('')
  const [saving, setSaving] = useState(false)

  const isEdit = Boolean(contact)

  function updateField(field, value) {
    setForm((current) => ({ ...current, [field]: value }))
  }

  function updateRow(field, index, key, value) {
    setForm((current) => {
      const rows = [...current[field]]
      rows[index] = { ...rows[index], [key]: value }
      return { ...current, [field]: rows }
    })
  }

  function addRow(field, blank) {
    setForm((current) => ({ ...current, [field]: [...current[field], blank] }))
  }

  function removeRow(field, index) {
    setForm((current) => ({
      ...current,
      [field]: current[field].filter((_, i) => i !== index),
    }))
  }

  async function handleSubmit(event) {
    event.preventDefault()
    setError('')
    setSaving(true)

    const payload = {
      firstName: form.firstName.trim(),
      lastName: form.lastName.trim(),
      title: form.title.trim() || null,
      emails: form.emails,
      phones: form.phones,
    }

    try {
      if (isEdit) {
        await updateContact(contact.id, payload)
      } else {
        await createContact(payload)
      }
      onSaved()
    } catch (err) {
      setError(err.message)
      setSaving(false)
    }
  }

  return (
    <Modal title={isEdit ? 'Edit contact' : 'New contact'} onClose={onClose}>
      <form onSubmit={handleSubmit}>
        {error && <p className="error">{error}</p>}

        <label htmlFor="firstName">First name</label>
        <input
          id="firstName"
          value={form.firstName}
          onChange={(e) => updateField('firstName', e.target.value)}
          required
        />

        <label htmlFor="lastName">Last name</label>
        <input
          id="lastName"
          value={form.lastName}
          onChange={(e) => updateField('lastName', e.target.value)}
          required
        />

        <label htmlFor="title">Title</label>
        <input
          id="title"
          value={form.title}
          onChange={(e) => updateField('title', e.target.value)}
        />

        <fieldset>
          <legend>Email addresses</legend>
          {form.emails.map((row, index) => (
            <div className="row" key={index}>
              <select
                value={row.label}
                onChange={(e) => updateRow('emails', index, 'label', e.target.value)}
                aria-label="Email label"
              >
                {LABELS.map((label) => (
                  <option key={label} value={label}>
                    {label}
                  </option>
                ))}
              </select>
              <input
                type="email"
                value={row.email}
                placeholder="name@example.com"
                onChange={(e) => updateRow('emails', index, 'email', e.target.value)}
                aria-label="Email address"
                required
              />
              <button type="button" className="secondary" onClick={() => removeRow('emails', index)}>
                Remove
              </button>
            </div>
          ))}
          <button
            type="button"
            className="secondary"
            onClick={() => addRow('emails', { label: 'WORK', email: '' })}
          >
            Add email
          </button>
        </fieldset>

        <fieldset>
          <legend>Phone numbers</legend>
          {form.phones.map((row, index) => (
            <div className="row" key={index}>
              <select
                value={row.label}
                onChange={(e) => updateRow('phones', index, 'label', e.target.value)}
                aria-label="Phone label"
              >
                {LABELS.map((label) => (
                  <option key={label} value={label}>
                    {label}
                  </option>
                ))}
              </select>
              <input
                value={row.phoneNumber}
                placeholder="Phone number"
                onChange={(e) => updateRow('phones', index, 'phoneNumber', e.target.value)}
                aria-label="Phone number"
                required
              />
              <button type="button" className="secondary" onClick={() => removeRow('phones', index)}>
                Remove
              </button>
            </div>
          ))}
          <button
            type="button"
            className="secondary"
            onClick={() => addRow('phones', { label: 'HOME', phoneNumber: '' })}
          >
            Add phone
          </button>
        </fieldset>

        <div className="modal-actions">
          <button type="button" className="secondary" onClick={onClose} disabled={saving}>
            Cancel
          </button>
          <button type="submit" disabled={saving}>
            {saving ? 'Saving...' : 'Save'}
          </button>
        </div>
      </form>
    </Modal>
  )
}

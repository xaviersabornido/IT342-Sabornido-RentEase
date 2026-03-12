import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import './PropertyDetails.css'
import { createListing } from '../api/listings'

export default function ListProperty() {
  const navigate = useNavigate()
  const storedUser = localStorage.getItem('user')
  const user = storedUser ? JSON.parse(storedUser) : null
  const isOwner = user?.role === 'OWNER'

  const [title, setTitle] = useState('')
  const [price, setPrice] = useState('')
  const [location, setLocation] = useState('')
  const [propertyType, setPropertyType] = useState('Apartment')
  const [description, setDescription] = useState('')
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')
  const [submitting, setSubmitting] = useState(false)

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')
    setSuccess('')

    if (!isOwner) {
      setError('Only owners can list properties.')
      return
    }
    if (!title.trim() || !location.trim() || !price) {
      setError('Title, price, and location are required.')
      return
    }

    try {
      setSubmitting(true)
      await createListing({
        title: title.trim(),
        price,
        location: location.trim(),
        description: description.trim(),
        propertyType,
      })
      setSuccess('Property listed successfully.')
      navigate('/dashboard')
    } catch (err) {
      console.error('Failed to create listing', err)
      setError(err.message || 'Unable to create listing.')
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="property-page">
      <header className="property-header">
        <div className="property-header-left">
          <div className="property-brand-icon">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
              <path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z" />
              <polyline points="9 22 9 12 15 12 15 22" />
            </svg>
          </div>
          <span className="property-brand-name">RentEase</span>
        </div>
      </header>

      <main className="property-main">
        <button type="button" className="property-back" onClick={() => navigate('/dashboard')}>
          ← Back to listings
        </button>

        <div className="property-layout">
          <section className="property-content">
            <h1 className="property-title">List a New Property</h1>
            <p className="property-subtitle">
              Provide details about your property so renters can discover it.
            </p>

            {!isOwner && (
              <p className="property-not-found">
                You must be logged in as an <strong>OWNER</strong> to list properties.
              </p>
            )}

            <form className="list-property-form" onSubmit={handleSubmit}>
              <div className="form-row-two">
                <div className="form-field">
                  <label htmlFor="title">Title</label>
                  <input
                    id="title"
                    type="text"
                    value={title}
                    onChange={(e) => setTitle(e.target.value)}
                    placeholder="Modern Downtown Apartment"
                    required
                  />
                </div>
                <div className="form-field">
                  <label htmlFor="price">Monthly Price (USD)</label>
                  <input
                    id="price"
                    type="number"
                    min="0"
                    step="50"
                    value={price}
                    onChange={(e) => setPrice(e.target.value)}
                    placeholder="2850"
                    required
                  />
                </div>
              </div>

              <div className="form-row-two">
                <div className="form-field">
                  <label htmlFor="location">Location</label>
                  <input
                    id="location"
                    type="text"
                    value={location}
                    onChange={(e) => setLocation(e.target.value)}
                    placeholder="123 Main Street, Downtown"
                    required
                  />
                </div>
                <div className="form-field">
                  <label htmlFor="propertyType">Property Type</label>
                  <select
                    id="propertyType"
                    value={propertyType}
                    onChange={(e) => setPropertyType(e.target.value)}
                  >
                    <option value="Apartment">Apartment</option>
                    <option value="House">House</option>
                    <option value="Condo">Condo</option>
                  </select>
                </div>
              </div>

              <div className="form-field">
                <label htmlFor="description">Description</label>
                <textarea
                  id="description"
                  rows={5}
                  value={description}
                  onChange={(e) => setDescription(e.target.value)}
                  placeholder="Describe the layout, features, and amenities of your property."
                />
              </div>

              {error && <p className="property-status-text property-status-error">{error}</p>}
              {success && <p className="property-status-text">{success}</p>}

              <button type="submit" className="sidebar-primary-btn" disabled={submitting || !isOwner}>
                {submitting ? 'Listing Property…' : 'Publish Listing'}
              </button>
            </form>
          </section>
        </div>
      </main>
    </div>
  )
}


import { useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import '../styles/PropertyDetails.css'
import { deleteListing, fetchListingById, updateListing } from '../api/listings'

function formatDateInput(value) {
  if (value == null || value === '') return ''
  const s = String(value)
  return s.length >= 10 ? s.slice(0, 10) : s
}

function numToInput(n) {
  if (n == null || n === '') return ''
  return String(n)
}

export default function EditListing() {
  const { id } = useParams()
  const navigate = useNavigate()
  const storedUser = localStorage.getItem('user')
  const user = storedUser ? JSON.parse(storedUser) : null
  const isOwner = String(user?.role || '').toUpperCase() === 'OWNER'

  const [loading, setLoading] = useState(true)
  const [loadError, setLoadError] = useState('')
  const [title, setTitle] = useState('')
  const [price, setPrice] = useState('')
  const [location, setLocation] = useState('')
  const [propertyType, setPropertyType] = useState('Apartment')
  const [description, setDescription] = useState('')
  const [amenities, setAmenities] = useState('')
  const [imageUrls, setImageUrls] = useState('')
  const [bedrooms, setBedrooms] = useState('')
  const [bathrooms, setBathrooms] = useState('')
  const [areaSqFt, setAreaSqFt] = useState('')
  const [parkingSpaces, setParkingSpaces] = useState('')
  const [availableFrom, setAvailableFrom] = useState('')
  const [leaseTermMonths, setLeaseTermMonths] = useState('')
  const [deposit, setDeposit] = useState('')
  const [furnished, setFurnished] = useState(false)
  const [petsAllowed, setPetsAllowed] = useState(true)
  const [utilitiesEstimate, setUtilitiesEstimate] = useState('')
  const [error, setError] = useState('')
  const [submitting, setSubmitting] = useState(false)
  const [deleting, setDeleting] = useState(false)

  useEffect(() => {
    if (!isOwner) {
      navigate('/dashboard', { replace: true })
    }
  }, [isOwner, navigate])

  useEffect(() => {
    if (!isOwner || !id) return

    let cancelled = false
    const load = async () => {
      try {
        setLoading(true)
        setLoadError('')
        const listing = await fetchListingById(id)
        if (cancelled || !listing) return
        setTitle(listing.title || '')
        setPrice(numToInput(listing.price))
        setLocation(listing.location || '')
        setPropertyType(listing.propertyType || 'Apartment')
        setDescription(listing.description || '')
        setAmenities(listing.amenities || '')
        setImageUrls(listing.imageUrls || '')
        setBedrooms(numToInput(listing.bedrooms))
        setBathrooms(numToInput(listing.bathrooms))
        setAreaSqFt(numToInput(listing.areaSqFt))
        setParkingSpaces(numToInput(listing.parkingSpaces))
        setAvailableFrom(formatDateInput(listing.availableFrom))
        setLeaseTermMonths(numToInput(listing.leaseTermMonths))
        setDeposit(numToInput(listing.deposit))
        setFurnished(Boolean(listing.furnished))
        setPetsAllowed(listing.petsAllowed !== false)
        setUtilitiesEstimate(numToInput(listing.utilitiesEstimate))
      } catch (err) {
        console.error(err)
        if (!cancelled) setLoadError(err.message || 'Unable to load this listing.')
      } finally {
        if (!cancelled) setLoading(false)
      }
    }
    load()
    return () => {
      cancelled = true
    }
  }, [id, isOwner])

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')

    if (!isOwner) {
      setError('Only owners can edit listings.')
      return
    }
    if (!title.trim() || !location.trim() || !price) {
      setError('Title, price, and location are required.')
      return
    }

    try {
      setSubmitting(true)
      await updateListing(id, {
        title: title.trim(),
        price,
        location: location.trim(),
        description: description.trim(),
        propertyType,
        amenities: amenities.trim(),
        imageUrls: imageUrls.trim(),
        bedrooms,
        bathrooms,
        areaSqFt,
        parkingSpaces,
        availableFrom,
        leaseTermMonths,
        deposit,
        furnished,
        petsAllowed,
        utilitiesEstimate,
      })
      navigate('/my-listings')
    } catch (err) {
      console.error(err)
      setError(err.message || 'Unable to save changes.')
    } finally {
      setSubmitting(false)
    }
  }

  const handleDelete = async () => {
    if (!window.confirm('Remove this listing? This cannot be undone.')) return
    setError('')
    try {
      setDeleting(true)
      await deleteListing(id)
      navigate('/my-listings')
    } catch (err) {
      console.error(err)
      setError(err.message || 'Could not delete listing.')
    } finally {
      setDeleting(false)
    }
  }

  if (!isOwner) {
    return null
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
        <button type="button" className="property-back" onClick={() => navigate('/my-listings')}>
          ← Back to My Listings
        </button>

        <div className="property-layout">
          <section className="property-content">
            <h1 className="property-title">Edit property</h1>
            <p className="property-subtitle">Update details for this listing. Changes apply immediately after you save.</p>

            {loading && <p className="property-status-text">Loading listing…</p>}
            {loadError && <p className="property-status-text property-status-error">{loadError}</p>}

            {!loading && !loadError && (
              <form className="list-property-form" onSubmit={handleSubmit}>
                <div className="form-row-two">
                  <div className="form-field">
                    <label htmlFor="edit-title">Title</label>
                    <input
                      id="edit-title"
                      type="text"
                      value={title}
                      onChange={(e) => setTitle(e.target.value)}
                      required
                    />
                  </div>
                  <div className="form-field">
                    <label htmlFor="edit-price">Monthly Price (USD)</label>
                    <input
                      id="edit-price"
                      type="number"
                      min="0"
                      step="50"
                      value={price}
                      onChange={(e) => setPrice(e.target.value)}
                      required
                    />
                  </div>
                </div>

                <div className="form-row-two">
                  <div className="form-field">
                    <label htmlFor="edit-location">Location</label>
                    <input
                      id="edit-location"
                      type="text"
                      value={location}
                      onChange={(e) => setLocation(e.target.value)}
                      required
                    />
                  </div>
                  <div className="form-field">
                    <label htmlFor="edit-property-type">Property Type</label>
                    <select
                      id="edit-property-type"
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
                  <label htmlFor="edit-description">Description</label>
                  <textarea
                    id="edit-description"
                    rows={5}
                    value={description}
                    onChange={(e) => setDescription(e.target.value)}
                  />
                </div>

                <div className="form-field">
                  <label htmlFor="edit-amenities">Amenities (comma-separated)</label>
                  <input
                    id="edit-amenities"
                    type="text"
                    value={amenities}
                    onChange={(e) => setAmenities(e.target.value)}
                    placeholder="WiFi, Air conditioning, Parking"
                  />
                </div>

                <div className="form-field">
                  <label htmlFor="edit-images">Image URLs (comma-separated)</label>
                  <input
                    id="edit-images"
                    type="text"
                    value={imageUrls}
                    onChange={(e) => setImageUrls(e.target.value)}
                    placeholder="https://..., https://..."
                  />
                </div>

                <div className="form-row-two">
                  <div className="form-field">
                    <label htmlFor="edit-bedrooms">Bedrooms</label>
                    <input
                      id="edit-bedrooms"
                      type="number"
                      min="0"
                      value={bedrooms}
                      onChange={(e) => setBedrooms(e.target.value)}
                    />
                  </div>
                  <div className="form-field">
                    <label htmlFor="edit-bathrooms">Bathrooms</label>
                    <input
                      id="edit-bathrooms"
                      type="number"
                      min="0"
                      value={bathrooms}
                      onChange={(e) => setBathrooms(e.target.value)}
                    />
                  </div>
                </div>

                <div className="form-row-two">
                  <div className="form-field">
                    <label htmlFor="edit-area">Area (sq ft)</label>
                    <input
                      id="edit-area"
                      type="number"
                      min="0"
                      value={areaSqFt}
                      onChange={(e) => setAreaSqFt(e.target.value)}
                    />
                  </div>
                  <div className="form-field">
                    <label htmlFor="edit-parking">Parking Spaces</label>
                    <input
                      id="edit-parking"
                      type="number"
                      min="0"
                      value={parkingSpaces}
                      onChange={(e) => setParkingSpaces(e.target.value)}
                    />
                  </div>
                </div>

                <div className="form-row-two">
                  <div className="form-field">
                    <label htmlFor="edit-available">Available From</label>
                    <input
                      id="edit-available"
                      type="date"
                      value={availableFrom}
                      onChange={(e) => setAvailableFrom(e.target.value)}
                    />
                  </div>
                  <div className="form-field">
                    <label htmlFor="edit-lease">Lease Term (months)</label>
                    <input
                      id="edit-lease"
                      type="number"
                      min="0"
                      value={leaseTermMonths}
                      onChange={(e) => setLeaseTermMonths(e.target.value)}
                    />
                  </div>
                </div>

                <div className="form-row-two">
                  <div className="form-field">
                    <label htmlFor="edit-deposit">Deposit (USD)</label>
                    <input
                      id="edit-deposit"
                      type="number"
                      min="0"
                      step="50"
                      value={deposit}
                      onChange={(e) => setDeposit(e.target.value)}
                    />
                  </div>
                  <div className="form-field">
                    <label htmlFor="edit-utilities">Utilities Estimate (USD)</label>
                    <input
                      id="edit-utilities"
                      type="number"
                      min="0"
                      step="25"
                      value={utilitiesEstimate}
                      onChange={(e) => setUtilitiesEstimate(e.target.value)}
                    />
                  </div>
                </div>

                <div className="form-row-two">
                  <div className="form-field">
                    <label htmlFor="edit-furnished">Furnished</label>
                    <select
                      id="edit-furnished"
                      value={furnished ? 'yes' : 'no'}
                      onChange={(e) => setFurnished(e.target.value === 'yes')}
                    >
                      <option value="no">No</option>
                      <option value="yes">Yes</option>
                    </select>
                  </div>
                  <div className="form-field">
                    <label htmlFor="edit-pets">Pets Allowed</label>
                    <select
                      id="edit-pets"
                      value={petsAllowed ? 'yes' : 'no'}
                      onChange={(e) => setPetsAllowed(e.target.value === 'yes')}
                    >
                      <option value="yes">Yes</option>
                      <option value="no">No</option>
                    </select>
                  </div>
                </div>

                {error && <p className="property-status-text property-status-error">{error}</p>}

                <div className="edit-listing-form-actions">
                  <button type="submit" className="sidebar-primary-btn" disabled={submitting || deleting}>
                    {submitting ? 'Saving…' : 'Save changes'}
                  </button>
                  <button
                    type="button"
                    className="sidebar-danger-btn"
                    disabled={submitting || deleting}
                    onClick={handleDelete}
                  >
                    {deleting ? 'Deleting…' : 'Delete listing'}
                  </button>
                </div>
              </form>
            )}
          </section>
        </div>
      </main>
    </div>
  )
}

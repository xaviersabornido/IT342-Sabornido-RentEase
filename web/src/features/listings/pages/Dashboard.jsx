import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import OwnerSidebar from '../../owner/layouts/OwnerSidebar'
import '../styles/Dashboard.css'
import { fetchListings, createListing } from '../api/listings'

const PER_PAGE = 9

export default function Dashboard() {
  const navigate = useNavigate()
  const [user, setUser] = useState({
    firstname: '',
    lastname: '',
    email: '',
    role: 'RENTER'
  })
  const [listings, setListings] = useState([])
  const [filter, setFilter] = useState('all')
  const [currentPage, setCurrentPage] = useState(1)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [showListModal, setShowListModal] = useState(false)
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
  const [modalError, setModalError] = useState('')
  const [modalSubmitting, setModalSubmitting] = useState(false)

  const loadListings = async () => {
    try {
      setLoading(true)
      setError('')
      const data = await fetchListings()
      setListings(Array.isArray(data) ? data : [])
    } catch (err) {
      console.error('Failed to load listings', err)
      setError(err.message || 'Unable to load listings')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    const stored = localStorage.getItem('user')
    if (stored) {
      try {
        setUser(JSON.parse(stored))
      } catch (_) {}
    }
    loadListings()
  }, [])

  const handleLogout = () => {
    localStorage.removeItem('accessToken')
    localStorage.removeItem('user')
    navigate('/login')
  }

  const displayName = user.firstname ? `${user.firstname} ${user.lastname}`.trim() : user.email || 'User'
  const roleUpper = String(user.role || '').toUpperCase()
  const isOwner = roleUpper === 'OWNER'
  const isRenter = roleUpper === 'RENTER'

  const filteredListings = filter === 'all'
    ? listings
    : listings.filter((l) => {
        const type = (l.propertyType || '').toLowerCase()
        if (filter === 'apartments') return type === 'apartment'
        if (filter === 'houses') return type === 'house'
        if (filter === 'condominiums') {
          return type === 'condo' || type === 'condominium'
        }
        return true
      })
  const totalPages = Math.max(1, Math.ceil(filteredListings.length / PER_PAGE))
  const pageListings = filteredListings.slice((currentPage - 1) * PER_PAGE, currentPage * PER_PAGE)

  const hasListings = listings.length > 0

  const resetModalForm = () => {
    setTitle('')
    setPrice('')
    setLocation('')
    setPropertyType('Apartment')
    setDescription('')
    setAmenities('')
    setImageUrls('')
    setModalError('')
    setBedrooms('')
    setBathrooms('')
    setAreaSqFt('')
    setParkingSpaces('')
    setAvailableFrom('')
    setLeaseTermMonths('')
    setDeposit('')
    setFurnished(false)
    setPetsAllowed(true)
    setUtilitiesEstimate('')
  }

  const handleCreateListing = async (e) => {
    e.preventDefault()
    setModalError('')

    if (!title.trim() || !location.trim() || !price) {
      setModalError('Title, price, and location are required.')
      return
    }

    if (!isOwner) {
      setModalError('Only owners can list properties.')
      return
    }

    try {
      setModalSubmitting(true)
      await createListing({
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
      resetModalForm()
      setShowListModal(false)
      await loadListings()
    } catch (err) {
      console.error('Failed to create listing', err)
      setModalError(err.message || 'Unable to create listing.')
    } finally {
      setModalSubmitting(false)
    }
  }

  const dashboardBody = (
    <div className="dashboard-page">
      <header className="dashboard-header">
        <div className={`header-content${isOwner ? ' header-content--owner-toolbar' : ''}`}>
          {!isOwner && (
            <div className="header-brand">
              <div className="brand-icon">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
                  <path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z" />
                  <polyline points="9 22 9 12 15 12 15 22" />
                </svg>
              </div>
              <h1 className="header-title">RentEase</h1>
            </div>
          )}
          <div className="header-actions">
            {isOwner && (
              <button
                type="button"
                className="btn-list-property"
                onClick={() => setShowListModal(true)}
              >
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                  <path d="M12 5v14M5 12h14" />
                </svg>
                List Property
              </button>
            )}
            {!isOwner && (
              <>
                {isRenter && (
                  <button
                    type="button"
                    className="btn-my-requests"
                    onClick={() => navigate('/my-requests')}
                  >
                    My rental requests
                  </button>
                )}
                <button type="button" className="btn-profile">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                    <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2" />
                    <circle cx="12" cy="7" r="4" />
                  </svg>
                  <span>{displayName}</span>
                </button>
                <button type="button" className="btn-logout" onClick={handleLogout}>Logout</button>
              </>
            )}
          </div>
        </div>
      </header>

      <main className="dashboard-main">
        {!hasListings && !loading && !error ? (
          <>
            <section className="greeting-section">
              <div className="greeting-content">
                <h2 className="greeting-title">Welcome back, {user.firstname || displayName}! 👋</h2>
                <p className="greeting-subtitle">
                  {isOwner ? 'Manage your listings and rental requests here.' : 'Find and book properties that fit your needs.'}
                </p>
              </div>
            </section>
            <section className="dashboard-empty">
              <div className="empty-state">
                <div className="empty-icon">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
                    <path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z" />
                    <polyline points="9 22 9 12 15 12 15 22" />
                  </svg>
                </div>
                <h3 className="empty-title">
                  {isOwner ? 'No properties listed yet' : 'No listings yet'}
                </h3>
                <p className="empty-message">
                  {isOwner
                    ? "When you add and list your property, it will appear here. You can then manage bookings and requests."
                    : "When owners list their properties, you'll see them here and can browse or book."}
                </p>
              </div>
            </section>
          </>
        ) : hasListings ? (
          <>
            <section className="rentals-header">
              <h2 className="rentals-title">Available Rentals</h2>
              <p className="rentals-subtitle">Discover your perfect rental property.</p>
              <div className="rentals-filters">
                <button
                  type="button"
                  className={`filter-btn ${filter === 'all' ? 'filter-btn-active' : ''}`}
                  onClick={() => { setFilter('all'); setCurrentPage(1) }}
                >
                  All Properties
                </button>
                <button
                  type="button"
                  className={`filter-btn ${filter === 'apartments' ? 'filter-btn-active' : ''}`}
                  onClick={() => { setFilter('apartments'); setCurrentPage(1) }}
                >
                  Apartments
                </button>
                <button
                  type="button"
                  className={`filter-btn ${filter === 'houses' ? 'filter-btn-active' : ''}`}
                  onClick={() => { setFilter('houses'); setCurrentPage(1) }}
                >
                  Houses
                </button>
                <button
                  type="button"
                  className={`filter-btn ${filter === 'condominiums' ? 'filter-btn-active' : ''}`}
                  onClick={() => { setFilter('condominiums'); setCurrentPage(1) }}
                >
                  Condominium
                </button>
              </div>
            </section>

            <section className="listings-grid">
              {pageListings.map((listing) => (
                <article key={listing.id} className="listing-card">
                  <button
                    type="button"
                    className="listing-card-link"
                    onClick={() => navigate(`/listings/${listing.id}`)}
                  >
                    <div className="listing-image">
                      {listing.imageUrls
                      && listing.imageUrls.split(',').map((u) => u.trim()).filter(Boolean)[0] ? (
                        <img
                          src={listing.imageUrls.split(',').map((u) => u.trim()).filter(Boolean)[0]}
                          alt={listing.title}
                        />
                        ) : (
                        <span className="listing-image-placeholder">
                          {(() => {
                            const t = (listing.propertyType || '').toLowerCase()
                            if (t === 'house') return 'Cozy House Exterior'
                            if (t === 'condo' || t === 'condominium') return 'Condominium'
                            return 'Modern Apartment Interior'
                          })()}
                        </span>
                        )}
                    </div>
                    <div className="listing-body">
                      <h3 className="listing-title">{listing.title}</h3>
                      <p className="listing-location">{listing.location}</p>
                      <p className="listing-price">
                        {new Intl.NumberFormat('en-US', {
                          style: 'currency',
                          currency: 'USD',
                          maximumFractionDigits: 0,
                        }).format(listing.price ?? 0)}
                        <span className="listing-price-period">/month</span>
                      </p>
                      <p className="listing-description">{listing.description}</p>
                      <span className="btn-view-details">View Details</span>
                    </div>
                  </button>
                </article>
              ))}
            </section>

            {totalPages > 1 && (
              <nav className="pagination" aria-label="Pagination">
                <button
                  type="button"
                  className="pagination-btn"
                  disabled={currentPage === 1}
                  onClick={() => setCurrentPage((p) => Math.max(1, p - 1))}
                  aria-label="Previous page"
                >
                  &lt;
                </button>
                {Array.from({ length: totalPages }, (_, i) => i + 1).map((page) => (
                  <button
                    key={page}
                    type="button"
                    className={`pagination-btn ${currentPage === page ? 'pagination-btn-active' : ''}`}
                    onClick={() => setCurrentPage(page)}
                  >
                    {page}
                  </button>
                ))}
                <button
                  type="button"
                  className="pagination-btn"
                  disabled={currentPage === totalPages}
                  onClick={() => setCurrentPage((p) => Math.min(totalPages, p + 1))}
                  aria-label="Next page"
                >
                  &gt;
                </button>
                <span className="pagination-indicator">
                  Page <strong>{currentPage}</strong> of <strong>{totalPages}</strong>
                </span>
              </nav>
            )}
          </>
        ) : null}

        {loading && (
          <p className="dashboard-status-text">Loading listings…</p>
        )}
        {error && (
          <p className="dashboard-status-text dashboard-status-error">{error}</p>
        )}

        {showListModal && (
          <div className="modal-overlay" role="dialog" aria-modal="true">
            <div className="modal-content">
              <div className="modal-header">
                <h2>List a New Property</h2>
                <button
                  type="button"
                  className="modal-close"
                  onClick={() => {
                    resetModalForm()
                    setShowListModal(false)
                  }}
                >
                  ×
                </button>
              </div>
              <form className="modal-body" onSubmit={handleCreateListing}>
                <div className="modal-row">
                  <div className="modal-field">
                    <label htmlFor="modal-title">Title</label>
                    <input
                      id="modal-title"
                      type="text"
                      value={title}
                      onChange={(e) => setTitle(e.target.value)}
                      placeholder="Modern Downtown Apartment"
                      required
                    />
                  </div>
                  <div className="modal-field">
                    <label htmlFor="modal-price">Monthly Price (USD)</label>
                    <input
                      id="modal-price"
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

                <div className="modal-row">
                  <div className="modal-field">
                    <label htmlFor="modal-location">Location</label>
                    <input
                      id="modal-location"
                      type="text"
                      value={location}
                      onChange={(e) => setLocation(e.target.value)}
                      placeholder="123 Main Street, Downtown"
                      required
                    />
                  </div>
                  <div className="modal-field">
                    <label htmlFor="modal-property-type">Property Type</label>
                    <select
                      id="modal-property-type"
                      value={propertyType}
                      onChange={(e) => setPropertyType(e.target.value)}
                    >
                      <option value="Apartment">Apartment</option>
                      <option value="House">House</option>
                      <option value="Condo">Condo</option>
                    </select>
                  </div>
                </div>

                <div className="modal-field">
                  <label htmlFor="modal-description">Description</label>
                  <textarea
                    id="modal-description"
                    rows={4}
                    value={description}
                    onChange={(e) => setDescription(e.target.value)}
                    placeholder="Describe the layout, features, and amenities of your property."
                  />
                </div>

                <div className="modal-field">
                  <label htmlFor="modal-amenities">Amenities (comma-separated)</label>
                  <input
                    id="modal-amenities"
                    type="text"
                    value={amenities}
                    onChange={(e) => setAmenities(e.target.value)}
                    placeholder="WiFi, Air conditioning, Parking"
                  />
                </div>

                <div className="modal-field">
                  <label htmlFor="modal-images">Image URLs (comma-separated)</label>
                  <input
                    id="modal-images"
                    type="text"
                    value={imageUrls}
                    onChange={(e) => setImageUrls(e.target.value)}
                    placeholder="https://..., https://..."
                  />
                </div>

                <div className="modal-row">
                  <div className="modal-field">
                    <label htmlFor="modal-bedrooms">Bedrooms</label>
                    <input
                      id="modal-bedrooms"
                      type="number"
                      min="0"
                      value={bedrooms}
                      onChange={(e) => setBedrooms(e.target.value)}
                      placeholder="3"
                    />
                  </div>
                  <div className="modal-field">
                    <label htmlFor="modal-bathrooms">Bathrooms</label>
                    <input
                      id="modal-bathrooms"
                      type="number"
                      min="0"
                      value={bathrooms}
                      onChange={(e) => setBathrooms(e.target.value)}
                      placeholder="2"
                    />
                  </div>
                </div>

                <div className="modal-row">
                  <div className="modal-field">
                    <label htmlFor="modal-area">Area (sq ft)</label>
                    <input
                      id="modal-area"
                      type="number"
                      min="0"
                      value={areaSqFt}
                      onChange={(e) => setAreaSqFt(e.target.value)}
                      placeholder="1200"
                    />
                  </div>
                  <div className="modal-field">
                    <label htmlFor="modal-parking">Parking Spaces</label>
                    <input
                      id="modal-parking"
                      type="number"
                      min="0"
                      value={parkingSpaces}
                      onChange={(e) => setParkingSpaces(e.target.value)}
                      placeholder="1"
                    />
                  </div>
                </div>

                <div className="modal-row">
                  <div className="modal-field">
                    <label htmlFor="modal-available">Available From</label>
                    <input
                      id="modal-available"
                      type="date"
                      value={availableFrom}
                      onChange={(e) => setAvailableFrom(e.target.value)}
                    />
                  </div>
                  <div className="modal-field">
                    <label htmlFor="modal-lease">Lease Term (months)</label>
                    <input
                      id="modal-lease"
                      type="number"
                      min="0"
                      value={leaseTermMonths}
                      onChange={(e) => setLeaseTermMonths(e.target.value)}
                      placeholder="12"
                    />
                  </div>
                </div>

                <div className="modal-row">
                  <div className="modal-field">
                    <label htmlFor="modal-deposit">Deposit (USD)</label>
                    <input
                      id="modal-deposit"
                      type="number"
                      min="0"
                      step="50"
                      value={deposit}
                      onChange={(e) => setDeposit(e.target.value)}
                      placeholder="2850"
                    />
                  </div>
                  <div className="modal-field">
                    <label htmlFor="modal-utilities">Utilities Estimate (USD)</label>
                    <input
                      id="modal-utilities"
                      type="number"
                      min="0"
                      step="25"
                      value={utilitiesEstimate}
                      onChange={(e) => setUtilitiesEstimate(e.target.value)}
                      placeholder="150"
                    />
                  </div>
                </div>

                <div className="modal-row">
                  <div className="modal-field">
                    <label htmlFor="modal-furnished">Furnished</label>
                    <select
                      id="modal-furnished"
                      value={furnished ? 'yes' : 'no'}
                      onChange={(e) => setFurnished(e.target.value === 'yes')}
                    >
                      <option value="no">No</option>
                      <option value="yes">Yes</option>
                    </select>
                  </div>
                  <div className="modal-field">
                    <label htmlFor="modal-pets">Pets Allowed</label>
                    <select
                      id="modal-pets"
                      value={petsAllowed ? 'yes' : 'no'}
                      onChange={(e) => setPetsAllowed(e.target.value === 'yes')}
                    >
                      <option value="yes">Yes</option>
                      <option value="no">No</option>
                    </select>
                  </div>
                </div>

                {modalError && (
                  <p className="dashboard-status-text dashboard-status-error">
                    {modalError}
                  </p>
                )}

                <div className="modal-actions">
                  <button
                    type="button"
                    className="modal-secondary-btn"
                    onClick={() => {
                      resetModalForm()
                      setShowListModal(false)
                    }}
                  >
                    Cancel
                  </button>
                  <button
                    type="submit"
                    className="modal-primary-btn"
                    disabled={modalSubmitting}
                  >
                    {modalSubmitting ? 'Listing Property…' : 'Publish Listing'}
                  </button>
                </div>
              </form>
            </div>
          </div>
        )}
      </main>
    </div>
  )

  if (isOwner) {
    return (
      <div className="owner-layout">
        <OwnerSidebar user={user} />
        <div className="owner-main owner-main--dashboard">
          {dashboardBody}
        </div>
      </div>
    )
  }

  return dashboardBody
}

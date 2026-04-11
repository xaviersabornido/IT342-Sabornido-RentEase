import { useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import './PropertyDetails.css'
import { fetchListingById } from '../api/listings'
import { createRentalRequest } from '../api/rentalRequests'

const MAX_MESSAGE = 500

const LEASE_OPTIONS = [
  { value: '', label: 'Select duration' },
  { value: '3', label: '3 months' },
  { value: '6', label: '6 months' },
  { value: '12', label: '12 months' },
  { value: '18', label: '18 months' },
  { value: '24', label: '24 months' },
]

const EMPLOYMENT_OPTIONS = [
  'Full-time Employee',
  'Part-time Employee',
  'Self-employed',
  'Student',
  'Unemployed',
  'Other',
]

function getStoredUser() {
  const raw = localStorage.getItem('user')
  if (!raw) return null
  try {
    return JSON.parse(raw)
  } catch {
    return null
  }
}

function availabilityLabel(listing) {
  const s = String(listing?.status || '').toLowerCase()
  if (s === 'available') return 'Available now'
  if (s === 'rented') return 'Currently occupied'
  if (s === 'pending') return 'Pending'
  return 'See listing'
}

function formatMoneySidebar(n) {
  if (n == null || n === '') return null
  return new Intl.NumberFormat('en-US', {
    style: 'currency',
    currency: 'USD',
    maximumFractionDigits: 0,
  }).format(Number(n))
}

function ownerInitials(first, last) {
  const f = String(first || '').trim()
  const l = String(last || '').trim()
  if (f || l) {
    return ((f[0] || '') + (l[0] || f[1] || '')).toUpperCase().slice(0, 2) || '?'
  }
  return '?'
}

function ownerDisplayFields(listing) {
  const fn = listing?.ownerFirstname ?? listing?.owner_firstname
  const ln = listing?.ownerLastname ?? listing?.owner_lastname
  const name = `${fn || ''} ${ln || ''}`.trim()
  return {
    name: name || 'Property owner',
    initials: ownerInitials(fn, ln),
    listingCount: listing?.ownerListingCount ?? listing?.owner_listing_count,
    rating: listing?.ownerRating ?? listing?.owner_rating,
  }
}

function formatAvailableFromSidebar(d) {
  if (!d) return '—'
  const x = new Date(String(d).slice(0, 10) + 'T12:00:00')
  if (Number.isNaN(x.getTime())) return String(d)
  return x.toLocaleDateString('en-US', { month: 'long', day: 'numeric', year: 'numeric' })
}

function SidebarOwnerStars({ rating }) {
  const r = rating != null && !Number.isNaN(Number(rating)) ? Number(rating) : null
  const capped = r != null ? Math.min(5, Math.max(0, r)) : null
  const filled = capped != null ? Math.round(capped) : 0
  return (
    <div
      className="sidebar-owner-stars"
      aria-label={capped != null ? `Rating ${capped.toFixed(1)} out of 5 stars` : 'No rating yet'}
    >
      {[1, 2, 3, 4, 5].map((i) => (
        <span
          key={i}
          className={i <= filled ? 'sidebar-owner-star sidebar-owner-star--full' : 'sidebar-owner-star'}
          aria-hidden
        >
          ★
        </span>
      ))}
      <span className="sidebar-owner-rating-value">
        {capped != null ? capped.toFixed(1) : 'New'}
      </span>
    </div>
  )
}

export default function PropertyDetails() {
  const navigate = useNavigate()
  const { id } = useParams()
  const [listing, setListing] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [activeImage, setActiveImage] = useState(null)

  const [showRequestModal, setShowRequestModal] = useState(false)
  const [preferredStart, setPreferredStart] = useState('')
  const [leaseMonths, setLeaseMonths] = useState('')
  const [monthlyIncome, setMonthlyIncome] = useState('')
  const [employmentStatus, setEmploymentStatus] = useState(EMPLOYMENT_OPTIONS[0])
  const [message, setMessage] = useState('')
  const [hasPets, setHasPets] = useState(false)
  const [smokes, setSmokes] = useState(false)
  const [creditCheckAgreed, setCreditCheckAgreed] = useState(false)
  const [formError, setFormError] = useState('')
  const [submitting, setSubmitting] = useState(false)
  const [requestSuccess, setRequestSuccess] = useState('')

  useEffect(() => {
    const load = async () => {
      try {
        setLoading(true)
        setError('')
        const data = await fetchListingById(id)
        setListing(data)
      } catch (err) {
        console.error('Failed to load listing', err)
        setError(err.message || 'Unable to load listing')
      } finally {
        setLoading(false)
      }
    }
    load()
  }, [id])

  useEffect(() => {
    if (!activeImage) return
    const prevOverflow = document.body.style.overflow
    document.body.style.overflow = 'hidden'
    const onKeyDown = (e) => {
      if (e.key === 'Escape') {
        setActiveImage(null)
      }
    }
    window.addEventListener('keydown', onKeyDown)
    return () => {
      document.body.style.overflow = prevOverflow
      window.removeEventListener('keydown', onKeyDown)
    }
  }, [activeImage])

  const resetRequestForm = () => {
    setPreferredStart('')
    setLeaseMonths('')
    setMonthlyIncome('')
    setEmploymentStatus(EMPLOYMENT_OPTIONS[0])
    setMessage('')
    setHasPets(false)
    setSmokes(false)
    setCreditCheckAgreed(false)
    setFormError('')
  }

  const closeRequestModal = () => {
    setShowRequestModal(false)
    resetRequestForm()
  }

  useEffect(() => {
    if (!showRequestModal) return
    const onKeyDown = (e) => {
      if (e.key === 'Escape') {
        setShowRequestModal(false)
        setPreferredStart('')
        setLeaseMonths('')
        setMonthlyIncome('')
        setEmploymentStatus(EMPLOYMENT_OPTIONS[0])
        setMessage('')
        setHasPets(false)
        setSmokes(false)
        setCreditCheckAgreed(false)
        setFormError('')
      }
    }
    window.addEventListener('keydown', onKeyDown)
    return () => window.removeEventListener('keydown', onKeyDown)
  }, [showRequestModal])

  const openRequestModal = () => {
    setRequestSuccess('')
    const token = localStorage.getItem('accessToken')
    const user = getStoredUser()
    if (!token || !user) {
      navigate('/login', { state: { from: `/listings/${id}` } })
      return
    }
    const role = String(user.role || '').toUpperCase()
    if (role === 'OWNER') {
      setFormError('You cannot send a rental request as a property owner.')
      return
    }
    if (role !== 'RENTER') {
      setFormError('Sign in as a renter to send a rental request.')
      return
    }
    setFormError('')
    resetRequestForm()
    setShowRequestModal(true)
  }

  const handleSubmitRequest = async (e) => {
    e.preventDefault()
    setFormError('')

    if (!preferredStart) {
      setFormError('Preferred start date is required.')
      return
    }
    if (!monthlyIncome || Number(monthlyIncome) <= 0) {
      setFormError('Please enter a valid monthly income.')
      return
    }
    if (!employmentStatus) {
      setFormError('Employment status is required.')
      return
    }
    if (!creditCheckAgreed) {
      setFormError('You must agree to a credit check.')
      return
    }

    const trimmedMsg = message.trim().slice(0, MAX_MESSAGE)
    try {
      setSubmitting(true)
      await createRentalRequest({
        listingId: Number(id),
        preferredStartDate: preferredStart,
        leaseDurationMonths: leaseMonths ? Number(leaseMonths) : null,
        monthlyIncome: Number(monthlyIncome),
        employmentStatus,
        message: trimmedMsg || null,
        hasPets,
        smokes,
        creditCheckAgreed: true,
      })
      setRequestSuccess('Your rental request was sent. Track its status anytime from My rental requests on your dashboard.')
      closeRequestModal()
    } catch (err) {
      console.error(err)
      setFormError(err.message || 'Could not send rental request.')
    } finally {
      setSubmitting(false)
    }
  }

  if (loading) {
    return (
      <div className="property-page">
        <div className="property-shell">
          <button type="button" className="property-back" onClick={() => navigate(-1)}>
            ← Back to listings
          </button>
          <p className="property-status-text">Loading listing…</p>
        </div>
      </div>
    )
  }

  if (error || !listing) {
    return (
      <div className="property-page">
        <div className="property-shell">
          <button type="button" className="property-back" onClick={() => navigate(-1)}>
            ← Back to listings
          </button>
          <p className="property-not-found">{error || 'Listing not found.'}</p>
        </div>
      </div>
    )
  }

  const imageList = listing.imageUrls
    ? listing.imageUrls.split(',').map((u) => u.trim()).filter(Boolean)
    : []
  const mainImage = imageList[0] || null
  /** Always three equal slots below the hero (extra URLs after the third are omitted here). */
  const THUMB_COUNT = 3
  const thumbSlots = Array.from({ length: THUMB_COUNT }, (_, i) => imageList[i + 1] ?? null)

  const handleOpenImage = (url) => {
    if (!url) return
    setActiveImage(url)
  }

  const handleCloseImage = () => {
    setActiveImage(null)
  }

  const handleLogout = () => {
    localStorage.removeItem('accessToken')
    localStorage.removeItem('user')
    navigate('/login')
  }

  const user = getStoredUser()
  const role = String(user?.role || '').toUpperCase()
  const isOwnerUser = role === 'OWNER'
  const isRenterUser = role === 'RENTER'
  const hasToken = Boolean(localStorage.getItem('accessToken'))
  const ownerInfo = ownerDisplayFields(listing)
  const utilitiesLine = formatMoneySidebar(listing.utilitiesEstimate)

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
        <div className="property-header-right">
          <button type="button" className="property-nav-btn" onClick={() => navigate('/dashboard')}>
            Browse
          </button>
          {hasToken && (
            <button type="button" className="property-nav-btn property-nav-btn--muted" onClick={handleLogout}>
              Logout
            </button>
          )}
        </div>
      </header>

      <main className="property-main">
        <nav className="property-breadcrumb">
          <button type="button" onClick={() => navigate('/dashboard')}>
            Home
          </button>
          <span>›</span>
          <button type="button" onClick={() => navigate('/dashboard')}>
            Listings
          </button>
          <span>›</span>
          <span className="breadcrumb-current">Property Details</span>
        </nav>

        {formError && !showRequestModal && (
          <p className="property-inline-alert" role="alert">{formError}</p>
        )}
        {requestSuccess && (
          <p className="property-inline-success" role="status">{requestSuccess}</p>
        )}

        <div className="property-layout">
          <section className="property-content">
            <div className="property-hero">
              <div className="property-hero-main">
                {mainImage ? (
                  <button
                    type="button"
                    className="property-hero-button"
                    onClick={() => handleOpenImage(mainImage)}
                  >
                    <img src={mainImage} alt={listing.title} />
                  </button>
                ) : (
                  <span className="property-hero-placeholder">
                    Property Main Image
                  </span>
                )}
              </div>
              <div className="property-hero-thumbs">
                {thumbSlots.map((url, index) =>
                  url ? (
                    <button
                      key={`thumb-${index}-${url}`}
                      type="button"
                      className="property-thumb property-thumb-button"
                      onClick={() => handleOpenImage(url)}
                    >
                      <img src={url} alt={`${listing.title} thumbnail ${index + 1}`} />
                    </button>
                  ) : (
                    <div
                      key={`thumb-placeholder-${index}`}
                      className="property-thumb property-thumb--empty"
                      aria-hidden
                    />
                  ),
                )}
              </div>
            </div>

            <header className="property-title-block">
              <h1 className="property-title">{listing.title}</h1>
              <p className="property-subtitle">
                {listing.location} • 4.8 (24 reviews)
              </p>
            </header>

            <section className="property-features-card">
              <h2 className="section-heading">Property Features</h2>
              <div className="property-features-grid">
                <div className="property-feature-item">
                  <span className="feature-label">Bathrooms</span>
                  <span className="feature-value">{listing.bathrooms ?? '—'}</span>
                </div>
                <div className="property-feature-item">
                  <span className="feature-label">Bedrooms</span>
                  <span className="feature-value">{listing.bedrooms ?? '—'}</span>
                </div>
                <div className="property-feature-item">
                  <span className="feature-label">Area</span>
                  <span className="feature-value">
                    {listing.areaSqFt != null ? `${listing.areaSqFt} sq ft` : '—'}
                  </span>
                </div>
                <div className="property-feature-item">
                  <span className="feature-label">Parking</span>
                  <span className="feature-value">
                    {listing.parkingSpaces != null ? `${listing.parkingSpaces} Space${listing.parkingSpaces === 1 ? '' : 's'}` : '—'}
                  </span>
                </div>
              </div>
            </section>

            <section className="property-section">
              <h2 className="section-heading">Description</h2>
              <p className="property-description">
                {listing.description || 'No description provided for this property yet.'}
              </p>
            </section>

            <section className="property-section">
              <h2 className="section-heading">Amenities</h2>
              <div className="amenities-grid">
                {(listing.amenities
                  ? listing.amenities.split(',').map((a) => a.trim()).filter(Boolean)
                  : [
                      'High-Speed Internet',
                      'Air Conditioning',
                      'Fitness Center',
                      '24/7 Security',
                      'Elevator Access',
                      'Pet Friendly',
                    ]
                ).map((amenity) => (
                  <div key={amenity} className="amenity-pill">
                    {amenity}
                  </div>
                ))}
              </div>
            </section>
          </section>

          <aside className="property-sidebar">
            <div className="property-sidebar-card">
              <div className="price-row">
                <div>
                  <div className="price-main">
                    {new Intl.NumberFormat('en-US', {
                      style: 'currency',
                      currency: 'USD',
                      maximumFractionDigits: 0,
                    }).format(listing.price ?? 0)}
                    <span className="price-period">/month</span>
                  </div>
                  {utilitiesLine && (
                    <div className="price-note">+ {utilitiesLine} utilities</div>
                  )}
                </div>
                <div className="price-availability">{availabilityLabel(listing)}</div>
              </div>

              <div className="sidebar-available-block">
                <span className="sidebar-available-icon" aria-hidden>
                  <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" strokeWidth="1.75" strokeLinecap="round" strokeLinejoin="round">
                    <rect x="3" y="4" width="18" height="18" rx="2" />
                    <path d="M16 2v4M8 2v4M3 10h18" />
                  </svg>
                </span>
                <div>
                  <div className="sidebar-available-label">Available from</div>
                  <div className="sidebar-available-date">{formatAvailableFromSidebar(listing.availableFrom)}</div>
                </div>
              </div>

              <div className="sidebar-card-divider" role="presentation" />

              <section className="sidebar-owner" aria-labelledby="sidebar-owner-heading">
                <h3 id="sidebar-owner-heading" className="sidebar-owner-heading">
                  Property owner
                </h3>
                <div className="sidebar-owner-body">
                  <div className="sidebar-owner-avatar" aria-hidden>
                    {ownerInfo.initials}
                  </div>
                  <div className="sidebar-owner-text">
                    <p className="sidebar-owner-name">{ownerInfo.name}</p>
                    <SidebarOwnerStars rating={ownerInfo.rating} />
                    <p className="sidebar-owner-meta">
                      Verified owner
                      {ownerInfo.listingCount != null && ownerInfo.listingCount > 0 && (
                        <>
                          <span className="sidebar-owner-meta-dot" aria-hidden>
                            ·
                          </span>
                          <span>
                            {ownerInfo.listingCount}{' '}
                            {ownerInfo.listingCount === 1 ? 'property' : 'properties'}
                          </span>
                        </>
                      )}
                    </p>
                  </div>
                </div>
              </section>

              <div className="sidebar-card-divider" role="presentation" />

              <h3 className="sidebar-section-heading">Property details</h3>
              <dl className="sidebar-details">
                <div className="sidebar-detail-row">
                  <dt>Property Type</dt>
                  <dd>{listing.propertyType}</dd>
                </div>
                <div className="sidebar-detail-row">
                  <dt>Lease Term</dt>
                  <dd>
                    {listing.leaseTermMonths != null ? `${listing.leaseTermMonths} months` : '—'}
                  </dd>
                </div>
                <div className="sidebar-detail-row">
                  <dt>Deposit</dt>
                  <dd>
                    {listing.deposit != null
                      ? new Intl.NumberFormat('en-US', {
                          style: 'currency',
                          currency: 'USD',
                          maximumFractionDigits: 0,
                        }).format(listing.deposit)
                      : '—'}
                  </dd>
                </div>
                <div className="sidebar-detail-row">
                  <dt>Furnished</dt>
                  <dd>
                    {listing.furnished == null ? '—' : listing.furnished ? 'Furnished' : 'Unfurnished'}
                  </dd>
                </div>
                <div className="sidebar-detail-row">
                  <dt>Pets</dt>
                  <dd>
                    {listing.petsAllowed == null ? '—' : listing.petsAllowed ? 'Allowed' : 'Not allowed'}
                  </dd>
                </div>
                <div className="sidebar-detail-row">
                  <dt>Utilities</dt>
                  <dd>
                    {listing.utilitiesEstimate != null
                      ? `≈ ${new Intl.NumberFormat('en-US', {
                          style: 'currency',
                          currency: 'USD',
                          maximumFractionDigits: 0,
                        }).format(listing.utilitiesEstimate)}`
                      : '—'}
                  </dd>
                </div>
              </dl>

              <button
                type="button"
                className="sidebar-primary-btn"
                onClick={openRequestModal}
                disabled={isOwnerUser}
                title={isOwnerUser ? 'You cannot request your own listing' : undefined}
              >
                {isOwnerUser ? 'Your listing' : 'Send Rental Request'}
              </button>
              {!hasToken && (
                <p className="sidebar-hint">Sign in as a renter to request this property.</p>
              )}
              {hasToken && !isRenterUser && !isOwnerUser && (
                <p className="sidebar-hint">Only renter accounts can send requests.</p>
              )}
              <button type="button" className="sidebar-secondary-btn">
                <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" aria-hidden>
                  <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z" />
                </svg>
                Save property
              </button>
              <p className="sidebar-hint">You won&apos;t be charged yet.</p>
            </div>
          </aside>
        </div>
      </main>

      {activeImage && (
        <div
          className="image-modal-overlay"
          role="dialog"
          aria-modal="true"
          onClick={handleCloseImage}
        >
          <div
            className="image-modal-content"
            onClick={(e) => e.stopPropagation()}
          >
            <button
              type="button"
              className="image-modal-close"
              onClick={handleCloseImage}
            >
              ×
            </button>
            <img src={activeImage} alt={listing.title} />
          </div>
        </div>
      )}

      {showRequestModal && (
        <div
          className="rental-modal-overlay"
          role="dialog"
          aria-modal="true"
          aria-labelledby="rental-modal-title"
          onClick={closeRequestModal}
        >
          <div
            className="rental-modal"
            onClick={(e) => e.stopPropagation()}
          >
            <div className="rental-modal-header">
              <h2 id="rental-modal-title" className="rental-modal-title">Rental Request</h2>
              <button type="button" className="rental-modal-close" onClick={closeRequestModal} aria-label="Close">
                ×
              </button>
            </div>
            <div className="rental-modal-divider" />

            <div className="rental-modal-summary">
              <div className="rental-modal-summary-image">
                {mainImage ? (
                  <img src={mainImage} alt="" />
                ) : (
                  <span className="rental-modal-image-placeholder">Property image</span>
                )}
              </div>
              <div className="rental-modal-summary-body">
                <h3 className="rental-modal-property-title">{listing.title}</h3>
                <p className="rental-modal-property-meta">
                  <span className="rental-modal-pin" aria-hidden>
                    <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" strokeWidth="2"><path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z" /><circle cx="12" cy="10" r="3" /></svg>
                  </span>
                  {listing.location}
                </p>
                <p className="rental-modal-property-beds">
                  <span>{listing.bedrooms ?? '—'} bed</span>
                  <span className="rental-modal-dot">·</span>
                  <span>{listing.bathrooms ?? '—'} bath</span>
                </p>
                <div className="rental-modal-summary-price-row">
                  <span className="rental-modal-price">
                    {new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD', maximumFractionDigits: 0 }).format(listing.price ?? 0)}
                    <span className="rental-modal-price-period">/month</span>
                  </span>
                  <span className="rental-modal-avail">{availabilityLabel(listing)}</span>
                </div>
              </div>
            </div>
            <div className="rental-modal-divider" />

            <form className="rental-modal-form" onSubmit={handleSubmitRequest}>
              <div className="rental-modal-row">
                <div className="rental-modal-field">
                  <label htmlFor="rr-start">Preferred Start Date *</label>
                  <input
                    id="rr-start"
                    type="date"
                    value={preferredStart}
                    onChange={(e) => setPreferredStart(e.target.value)}
                    required
                  />
                </div>
                <div className="rental-modal-field">
                  <label htmlFor="rr-lease">Lease Duration</label>
                  <select
                    id="rr-lease"
                    value={leaseMonths}
                    onChange={(e) => setLeaseMonths(e.target.value)}
                  >
                    {LEASE_OPTIONS.map((o) => (
                      <option key={o.label} value={o.value}>{o.label}</option>
                    ))}
                  </select>
                </div>
              </div>
              <div className="rental-modal-row">
                <div className="rental-modal-field">
                  <label htmlFor="rr-income">Monthly Income *</label>
                  <div className="rental-modal-input-prefix">
                    <span>$</span>
                    <input
                      id="rr-income"
                      type="number"
                      min="1"
                      step="100"
                      placeholder="5000"
                      value={monthlyIncome}
                      onChange={(e) => setMonthlyIncome(e.target.value)}
                      required
                    />
                  </div>
                </div>
                <div className="rental-modal-field">
                  <label htmlFor="rr-employ">Employment Status *</label>
                  <select
                    id="rr-employ"
                    value={employmentStatus}
                    onChange={(e) => setEmploymentStatus(e.target.value)}
                    required
                  >
                    {EMPLOYMENT_OPTIONS.map((opt) => (
                      <option key={opt} value={opt}>{opt}</option>
                    ))}
                  </select>
                </div>
              </div>
              <div className="rental-modal-field rental-modal-field--full">
                <label htmlFor="rr-msg">Message to Landlord (Optional)</label>
                <textarea
                  id="rr-msg"
                  rows={4}
                  maxLength={MAX_MESSAGE}
                  placeholder="Tell the landlord why you'd be a great tenant..."
                  value={message}
                  onChange={(e) => setMessage(e.target.value)}
                />
                <p className="rental-modal-char-count">{message.length}/{MAX_MESSAGE} characters</p>
              </div>

              <div className="rental-modal-extra">
                <p className="rental-modal-extra-title">Additional Information</p>
                <label className="rental-modal-check">
                  <input type="checkbox" checked={hasPets} onChange={(e) => setHasPets(e.target.checked)} />
                  I have pets
                </label>
                <label className="rental-modal-check">
                  <input type="checkbox" checked={smokes} onChange={(e) => setSmokes(e.target.checked)} />
                  I smoke
                </label>
                <label className="rental-modal-check">
                  <input
                    type="checkbox"
                    checked={creditCheckAgreed}
                    onChange={(e) => setCreditCheckAgreed(e.target.checked)}
                  />
                  I agree to a credit check *
                </label>
              </div>

              {formError && <p className="rental-modal-error" role="alert">{formError}</p>}

              <div className="rental-modal-footer">
                <button type="submit" className="rental-modal-submit" disabled={submitting}>
                  {submitting ? 'Sending…' : 'Submit Rental Request'}
                </button>
                <button type="button" className="rental-modal-cancel" onClick={closeRequestModal}>
                  Cancel
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  )
}

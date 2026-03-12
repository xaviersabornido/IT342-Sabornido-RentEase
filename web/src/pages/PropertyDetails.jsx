import { useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import './PropertyDetails.css'
import { fetchListingById } from '../api/listings'

export default function PropertyDetails() {
  const navigate = useNavigate()
  const { id } = useParams()
  const [listing, setListing] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [activeImage, setActiveImage] = useState(null)

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
    const onKeyDown = (e) => {
      if (e.key === 'Escape') {
        setActiveImage(null)
      }
    }
    window.addEventListener('keydown', onKeyDown)
    return () => window.removeEventListener('keydown', onKeyDown)
  }, [activeImage])

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
  const thumbImages = imageList.slice(1, 5)

  const handleOpenImage = (url) => {
    if (!url) return
    setActiveImage(url)
  }

  const handleCloseImage = () => {
    setActiveImage(null)
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
        <div className="property-header-right">
          <button type="button" className="property-nav-btn" onClick={() => navigate('/dashboard')}>
            Browse
          </button>
          <button type="button" className="property-nav-btn">
            About
          </button>
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
                {thumbImages.length > 0
                  ? thumbImages.map((url) => (
                      <button
                        key={url}
                        type="button"
                        className="property-thumb property-thumb-button"
                        onClick={() => handleOpenImage(url)}
                      >
                        <img src={url} alt={`${listing.title} thumbnail`} />
                      </button>
                    ))
                  : [1, 2, 3, 4].map((thumb) => (
                      <div key={thumb} className="property-thumb" />
                    ))}
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
                  <div className="price-note">+ $150 utilities</div>
                </div>
              </div>

              <dl className="sidebar-details">
                <div className="sidebar-detail-row">
                  <dt>Available from</dt>
                  <dd>{listing.availableFrom || '—'}</dd>
                </div>
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

              <button type="button" className="sidebar-primary-btn">
                Send Rental Request
              </button>
              <button type="button" className="sidebar-secondary-btn">
                Save Property
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
    </div>
  )
}


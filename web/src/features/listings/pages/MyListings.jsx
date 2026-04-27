import { useCallback, useEffect, useState } from 'react'
import { useNavigate, useLocation } from 'react-router-dom'
import '../styles/MyListings.css'
import { deleteListing, fetchMyListings } from '../api/listings'

function formatAvailability(status) {
  const s = String(status || '').toLowerCase()
  if (s === 'available') return { label: 'Available', className: 'my-listings-badge-available' }
  if (s === 'rented') return { label: 'Rented', className: 'my-listings-badge-rented' }
  if (s === 'pending') return { label: 'Pending', className: 'my-listings-badge-pending' }
  return { label: status || '—', className: 'my-listings-badge-default' }
}

function firstImageUrl(imageUrls) {
  if (!imageUrls) return null
  const first = imageUrls.split(',').map((u) => u.trim()).filter(Boolean)[0]
  return first || null
}

export default function MyListings() {
  const navigate = useNavigate()
  const location = useLocation()
  const [listings, setListings] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [deletingId, setDeletingId] = useState(null)

  const load = useCallback(async () => {
    try {
      setLoading(true)
      setError('')
      const data = await fetchMyListings()
      setListings(Array.isArray(data) ? data : [])
    } catch (err) {
      console.error(err)
      setError(err.message || 'Unable to load your listings.')
      setListings([])
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    load()
  }, [load, location.key])

  const handleDelete = async (id) => {
    if (!window.confirm('Remove this listing? This cannot be undone.')) return
    try {
      setDeletingId(id)
      await deleteListing(id)
      setListings((prev) => prev.filter((l) => l.id !== id))
    } catch (err) {
      console.error(err)
      setError(err.message || 'Could not delete listing.')
    } finally {
      setDeletingId(null)
    }
  }

  return (
    <div className="my-listings-page">
      <header className="my-listings-header">
        <div className="my-listings-header-text">
          <h1 className="my-listings-title">My Listings</h1>
          <p className="my-listings-subtitle">
            Manage your rental properties. When you approve a renter, availability becomes <strong>Rented</strong> and the property is removed from the public dashboard.
          </p>
        </div>
      </header>
      <div className="my-listings-divider" />

      {loading && <p className="my-listings-status">Loading your listings…</p>}
      {error && <p className="my-listings-status my-listings-status-error">{error}</p>}

      {!loading && !error && listings.length === 0 && (
        <div className="my-listings-empty">
          <p>
            You have no listings yet. Use <strong>List Property</strong> on the dashboard to publish a property — it will appear here once saved.
          </p>
        </div>
      )}

      {!loading && listings.length > 0 && (
        <div className="my-listings-table-wrap">
          <table className="my-listings-table">
            <thead>
              <tr>
                <th scope="col">Property</th>
                <th scope="col">Description</th>
                <th scope="col">Availability</th>
                <th scope="col" className="my-listings-col-actions">Actions</th>
              </tr>
            </thead>
            <tbody>
              {listings.map((listing) => {
                const thumb = firstImageUrl(listing.imageUrls)
                const badge = formatAvailability(listing.status)
                return (
                  <tr key={listing.id}>
                    <td>
                      <div className="my-listings-property-cell">
                        <div className="my-listings-thumb" aria-hidden>
                          {thumb ? (
                            <img src={thumb} alt="" />
                          ) : (
                            <span className="my-listings-thumb-placeholder" />
                          )}
                        </div>
                        <div>
                          <div className="my-listings-property-name">{listing.title}</div>
                          <div className="my-listings-property-address">{listing.location}</div>
                        </div>
                      </div>
                    </td>
                    <td>
                      <p className="my-listings-desc">{listing.description || '—'}</p>
                    </td>
                    <td>
                      <span className={`my-listings-badge ${badge.className}`}>{badge.label}</span>
                    </td>
                    <td className="my-listings-col-actions">
                      <div className="my-listings-actions">
                        <button
                          type="button"
                          className="my-listings-icon-btn"
                          title="Edit property"
                          aria-label={`Edit ${listing.title}`}
                          onClick={() => navigate(`/listings/${listing.id}/edit`)}
                        >
                          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" aria-hidden>
                            <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7" />
                            <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z" />
                          </svg>
                        </button>
                        <button
                          type="button"
                          className="my-listings-icon-btn my-listings-icon-btn-danger"
                          title="Delete listing"
                          aria-label={`Delete ${listing.title}`}
                          disabled={deletingId === listing.id}
                          onClick={() => handleDelete(listing.id)}
                        >
                          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" aria-hidden>
                            <polyline points="3 6 5 6 21 6" />
                            <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2" />
                          </svg>
                        </button>
                      </div>
                    </td>
                  </tr>
                )
              })}
            </tbody>
          </table>
        </div>
      )}
    </div>
  )
}

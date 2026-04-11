import { useCallback, useEffect, useMemo, useState } from 'react'
import { useLocation, useNavigate } from 'react-router-dom'
import { fetchRenterRentalRequests } from '../api/rentalRequests'
import './MyRentalRequestsPage.css'

function formatMoney(n) {
  if (n == null || n === '') return '—'
  return new Intl.NumberFormat('en-US', {
    style: 'currency',
    currency: 'USD',
    maximumFractionDigits: 0,
  }).format(Number(n))
}

function propertyMonthlyPrice(r) {
  if (r == null) return null
  const p = r.listingMonthlyPrice ?? r.listing_monthly_price ?? r.listingPrice
  if (p == null || p === '') return null
  const num = Number(p)
  return Number.isFinite(num) ? num : null
}

function formatDateLong(d) {
  if (!d) return '—'
  const start = new Date(String(d).slice(0, 10) + 'T12:00:00')
  if (Number.isNaN(start.getTime())) return '—'
  return start.toLocaleDateString('en-US', {
    month: 'short',
    day: 'numeric',
    year: 'numeric',
  })
}

function statusExplanation(status) {
  const s = String(status || '').toUpperCase()
  if (s === 'PENDING') {
    return 'The property owner has not responded yet. You’ll see the result here when they approve or decline.'
  }
  if (s === 'APPROVED') {
    return 'The owner accepted your request. They may contact you using your account email for next steps.'
  }
  if (s === 'DECLINED') {
    return 'The owner declined this request. You can keep browsing other listings on the dashboard.'
  }
  return ''
}

function isPending(r) {
  return String(r.status || '').toUpperCase() === 'PENDING'
}

function isPast(r) {
  const s = String(r.status || '').toUpperCase()
  return s === 'APPROVED' || s === 'DECLINED'
}

export default function MyRentalRequestsPage() {
  const navigate = useNavigate()
  const location = useLocation()
  const [requests, setRequests] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [activeTab, setActiveTab] = useState('pending')

  const load = useCallback(async () => {
    const token = localStorage.getItem('accessToken')
    const raw = localStorage.getItem('user')
    let user = null
    try {
      user = raw ? JSON.parse(raw) : null
    } catch {
      user = null
    }
    const role = String(user?.role || '').toUpperCase()
    if (!token || !user) {
      navigate('/login', { replace: true, state: { from: '/my-requests' } })
      return
    }
    if (role !== 'RENTER') {
      navigate('/dashboard', { replace: true })
      return
    }

    try {
      setLoading(true)
      setError('')
      const data = await fetchRenterRentalRequests()
      setRequests(Array.isArray(data) ? data : [])
    } catch (err) {
      console.error(err)
      setError(err.message || 'Unable to load your rental requests.')
      setRequests([])
    } finally {
      setLoading(false)
    }
  }, [navigate])

  useEffect(() => {
    load()
  }, [load])

  const pendingList = useMemo(
    () => requests.filter((r) => isPending(r)),
    [requests],
  )
  const pastList = useMemo(() => requests.filter((r) => isPast(r)), [requests])

  const visibleRows = useMemo(() => {
    if (activeTab === 'pending') return pendingList
    return pastList
  }, [activeTab, pendingList, pastList])

  const handleLogout = () => {
    localStorage.removeItem('accessToken')
    localStorage.removeItem('user')
    navigate('/login')
  }

  return (
    <div className="renter-rr-page">
      <header className="renter-rr-topnav">
        <div className="renter-rr-topnav-left">
          <button
            type="button"
            className="renter-rr-brand"
            onClick={() => navigate('/dashboard')}
            aria-label="RentEase home"
          >
            <span className="renter-rr-brand-icon" aria-hidden>
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
                <path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z" />
                <polyline points="9 22 9 12 15 12 15 22" />
              </svg>
            </span>
            <span className="renter-rr-brand-name">RentEase</span>
          </button>
        </div>
        <div className="renter-rr-topnav-right">
          <button type="button" className="renter-rr-nav-btn" onClick={() => navigate('/dashboard')}>
            Browse
          </button>
          <button type="button" className="renter-rr-nav-btn renter-rr-nav-btn--muted" onClick={handleLogout}>
            Logout
          </button>
        </div>
      </header>

      <main className="renter-rr-main">
        <nav className="renter-rr-breadcrumb" aria-label="Breadcrumb">
          <button type="button" onClick={() => navigate('/dashboard')}>
            Dashboard
          </button>
          <span aria-hidden>›</span>
          <span className="renter-rr-breadcrumb-current">My rental requests</span>
        </nav>

        <div className="renter-rr-intro">
          <h1 className="renter-rr-title">My rental requests</h1>
          <p className="renter-rr-subtitle">
            Track applications you’ve sent to property owners — pending decisions and past outcomes stay in one place.
          </p>
        </div>

        {location.state?.rated && (
          <p className="renter-rr-flash" role="status">
            Thanks! Your rating was submitted and will help other renters.
          </p>
        )}

        <div className="renter-rr-tabs" role="tablist" aria-label="Request filters">
          <button
            type="button"
            role="tab"
            aria-selected={activeTab === 'pending'}
            className={`renter-rr-tab${activeTab === 'pending' ? ' renter-rr-tab--active' : ''}`}
            onClick={() => setActiveTab('pending')}
          >
            Pending
            {pendingList.length > 0 && (
              <span className="renter-rr-tab-count">{pendingList.length}</span>
            )}
          </button>
          <button
            type="button"
            role="tab"
            aria-selected={activeTab === 'past'}
            className={`renter-rr-tab${activeTab === 'past' ? ' renter-rr-tab--active' : ''}`}
            onClick={() => setActiveTab('past')}
          >
            Past
            {pastList.length > 0 && (
              <span className="renter-rr-tab-count renter-rr-tab-count--muted">{pastList.length}</span>
            )}
          </button>
        </div>

        {loading && <p className="renter-rr-status">Loading your requests…</p>}
        {error && (
          <p className="renter-rr-status renter-rr-status--error" role="alert">
            {error}
          </p>
        )}

        {!loading && !error && requests.length === 0 && (
          <div className="renter-rr-empty">
            <div className="renter-rr-empty-icon" aria-hidden>
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.25" strokeLinecap="round" strokeLinejoin="round">
                <path d="M9 5H7a2 2 0 0 0-2 2v12a2 2 0 0 0 2 2h10a2 2 0 0 0 2-2V7a2 2 0 0 0-2-2h-2" />
                <rect x="9" y="3" width="6" height="4" rx="1" />
                <path d="M9 12h6M9 16h4" />
              </svg>
            </div>
            <h2 className="renter-rr-empty-title">No requests yet</h2>
            <p className="renter-rr-empty-text">
              You haven’t submitted any rental requests. Browse listings on the dashboard and use <strong>Send Rental Request</strong> on a property you like.
            </p>
            <button type="button" className="renter-rr-cta" onClick={() => navigate('/dashboard')}>
              Browse listings
            </button>
          </div>
        )}

        {!loading && requests.length > 0 && visibleRows.length === 0 && (
          <div className="renter-rr-panel renter-rr-panel--soft">
            <p className="renter-rr-muted">
              {activeTab === 'pending'
                ? 'No pending requests. When you apply to a property, it will show up here.'
                : 'No past requests yet. Approved or declined applications appear in this tab.'}
            </p>
          </div>
        )}

        {!loading && visibleRows.length > 0 && (
          <ul className="renter-rr-list">
            {visibleRows.map((r) => {
              const st = String(r.status || '').toUpperCase()
              const price = propertyMonthlyPrice(r)
              const canOpenListing =
                r.listingId != null &&
                (st === 'PENDING' || st === 'DECLINED')
              const rated = Boolean(r.ratingSubmitted ?? r.rating_submitted)
              const canRateOwner = st === 'APPROVED' && !rated && r.requestId != null
              return (
                <li key={r.requestId} className="renter-rr-card">
                  <div className="renter-rr-card-top">
                    <div className="renter-rr-card-titles">
                      <h2 className="renter-rr-listing-title">{r.listingTitle || 'Listing'}</h2>
                      {r.listingLocation && (
                        <p className="renter-rr-listing-loc">{r.listingLocation}</p>
                      )}
                    </div>
                    <span
                      className={`renter-rr-badge renter-rr-badge--${String(r.status || 'pending').toLowerCase()}`}
                    >
                      {st === 'PENDING' && 'Pending'}
                      {st === 'APPROVED' && 'Approved'}
                      {st === 'DECLINED' && 'Declined'}
                    </span>
                  </div>

                  <p className="renter-rr-note">{statusExplanation(r.status)}</p>

                  <dl className="renter-rr-meta">
                    <div>
                      <dt>Preferred start</dt>
                      <dd>{formatDateLong(r.preferredStartDate)}</dd>
                    </div>
                    <div>
                      <dt>Lease</dt>
                      <dd>{r.leaseDurationMonths != null ? `${r.leaseDurationMonths} mo` : '—'}</dd>
                    </div>
                    <div>
                      <dt>Monthly income</dt>
                      <dd>{formatMoney(r.monthlyIncome)}</dd>
                    </div>
                    <div>
                      <dt>Employment</dt>
                      <dd>{r.employmentStatus || '—'}</dd>
                    </div>
                    <div className="renter-rr-meta-span">
                      <dt>Property rent</dt>
                      <dd>
                        {price != null ? (
                          <>
                            {formatMoney(price)}
                            <span className="renter-rr-price-suffix">/mo</span>
                          </>
                        ) : (
                          '—'
                        )}
                      </dd>
                    </div>
                  </dl>

                  {r.message && (
                    <blockquote className="renter-rr-quote">&ldquo;{r.message}&rdquo;</blockquote>
                  )}

                  {(canOpenListing || canRateOwner) && (
                    <div className="renter-rr-card-actions">
                      {canRateOwner && (
                        <button
                          type="button"
                          className="renter-rr-rate-btn"
                          onClick={() => navigate(`/my-requests/${r.requestId}/rate`)}
                        >
                          Rate owner
                        </button>
                      )}
                      {canOpenListing && (
                        <button
                          type="button"
                          className="renter-rr-link-btn"
                          onClick={() => navigate(`/listings/${r.listingId}`)}
                        >
                          View listing
                        </button>
                      )}
                    </div>
                  )}

                  {st === 'APPROVED' && rated && (
                    <p className="renter-rr-rated-note">You submitted a rating for this application.</p>
                  )}
                </li>
              )
            })}
          </ul>
        )}
      </main>
    </div>
  )
}

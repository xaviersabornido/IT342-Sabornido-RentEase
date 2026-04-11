import { Fragment, useCallback, useEffect, useMemo, useState } from 'react'
import {
  approveRentalRequest,
  declineRentalRequest,
  fetchOwnerRentalRequests,
} from '../api/rentalRequests'
import './RentalRequestsPage.css'

function formatMoney(n) {
  if (n == null || n === '') return '—'
  return new Intl.NumberFormat('en-US', {
    style: 'currency',
    currency: 'USD',
    maximumFractionDigits: 0,
  }).format(Number(n))
}

/** Monthly rent from the listing (property price). */
function propertyMonthlyPrice(r) {
  if (r == null) return null
  const p = r.listingMonthlyPrice ?? r.listing_monthly_price ?? r.listingPrice
  if (p == null || p === '') return null
  const num = Number(p)
  return Number.isFinite(num) ? num : null
}

function formatDateRange(preferredStart, leaseMonths) {
  if (!preferredStart) return '—'
  const start = new Date(String(preferredStart).slice(0, 10) + 'T12:00:00')
  if (Number.isNaN(start.getTime())) return '—'
  const opts = { month: 'short', day: 'numeric', year: 'numeric' }
  const startStr = start.toLocaleDateString('en-US', opts)
  if (leaseMonths == null || leaseMonths === '') return startStr
  const end = new Date(start)
  end.setMonth(end.getMonth() + Number(leaseMonths))
  const endStr = end.toLocaleDateString('en-US', opts)
  return `${startStr} – ${endStr}`
}

function listingSubtitle(location) {
  if (!location) return ''
  const parts = String(location).split(',').map((s) => s.trim()).filter(Boolean)
  return parts.length > 1 ? parts.slice(1).join(', ') : parts[0] || ''
}

function renterInitials(first, last, email) {
  const f = String(first || '').trim()
  const l = String(last || '').trim()
  if (f || l) {
    return ((f[0] || '') + (l[0] || f[1] || '')).toUpperCase().slice(0, 2) || '?'
  }
  const e = String(email || '')
  return e.slice(0, 2).toUpperCase() || '?'
}

function renterDisplayName(r) {
  const n = `${r.renterFirstname || ''} ${r.renterLastname || ''}`.trim()
  return n || r.renterEmail || 'Renter'
}

function isPending(r) {
  return String(r.status || '').toUpperCase() === 'PENDING'
}

function isPast(r) {
  const s = String(r.status || '').toUpperCase()
  return s === 'APPROVED' || s === 'DECLINED'
}

export default function RentalRequestsPage() {
  const [requests, setRequests] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [actingId, setActingId] = useState(null)
  const [successMessage, setSuccessMessage] = useState('')
  const [activeTab, setActiveTab] = useState('pending')
  const [expandedId, setExpandedId] = useState(null)

  const load = useCallback(async () => {
    try {
      setLoading(true)
      setError('')
      const data = await fetchOwnerRentalRequests()
      setRequests(Array.isArray(data) ? data : [])
    } catch (err) {
      console.error(err)
      setError(err.message || 'Unable to load rental requests.')
      setRequests([])
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    load()
  }, [load])

  const pendingList = useMemo(() => requests.filter(isPending), [requests])
  const pastList = useMemo(() => requests.filter(isPast), [requests])

  const handleApprove = async (id) => {
    try {
      setActingId(id)
      setSuccessMessage('')
      await approveRentalRequest(id)
      await load()
      setSuccessMessage(
        'Request approved. That listing is now marked as Rented in My Listings and is no longer shown on the public dashboard.',
      )
      setActiveTab('past')
    } catch (err) {
      console.error(err)
      setError(err.message || 'Could not approve request.')
    } finally {
      setActingId(null)
    }
  }

  const handleDecline = async (id) => {
    if (!window.confirm('Decline this rental request?')) return
    try {
      setActingId(id)
      setSuccessMessage('')
      await declineRentalRequest(id)
      await load()
    } catch (err) {
      console.error(err)
      setError(err.message || 'Could not decline request.')
    } finally {
      setActingId(null)
    }
  }

  const visibleRows = activeTab === 'pending' ? pendingList : pastList

  return (
    <div className="owner-rr-page">
      <header className="owner-rr-header">
        <h1 className="owner-rr-title">Rental requests</h1>
        <p className="owner-rr-subtitle">Review applications for your properties</p>
      </header>

      <div className="owner-rr-tabs" role="tablist">
        <button
          type="button"
          role="tab"
          aria-selected={activeTab === 'pending'}
          className={`owner-rr-tab${activeTab === 'pending' ? ' owner-rr-tab--active' : ''}`}
          onClick={() => setActiveTab('pending')}
        >
          Pending requests
          {pendingList.length > 0 && (
            <span className="owner-rr-tab-count">{pendingList.length}</span>
          )}
        </button>
        <button
          type="button"
          role="tab"
          aria-selected={activeTab === 'past'}
          className={`owner-rr-tab${activeTab === 'past' ? ' owner-rr-tab--active' : ''}`}
          onClick={() => setActiveTab('past')}
        >
          Past requests
          {pastList.length > 0 && (
            <span className="owner-rr-tab-count owner-rr-tab-count--muted">{pastList.length}</span>
          )}
        </button>
      </div>

      {loading && <p className="owner-rr-status">Loading requests…</p>}
      {error && <p className="owner-rr-status owner-rr-status--error">{error}</p>}
      {successMessage && !loading && (
        <p className="owner-rr-status owner-rr-status--success">{successMessage}</p>
      )}

      {!loading && !error && requests.length === 0 && (
        <div className="owner-rr-card owner-rr-empty">
          <p>No rental requests yet. When renters apply to your listings, they will appear under Pending requests.</p>
        </div>
      )}

      {!loading && requests.length > 0 && visibleRows.length === 0 && (
        <div className="owner-rr-card owner-rr-empty">
          <p>
            {activeTab === 'pending'
              ? 'No pending requests. All caught up.'
              : 'No past requests yet.'}
          </p>
        </div>
      )}

      {!loading && visibleRows.length > 0 && (
        <div className="owner-rr-card owner-rr-table-wrap">
          <table className="owner-rr-table">
            <thead>
              <tr>
                <th scope="col">Renter</th>
                <th scope="col">Listing</th>
                <th scope="col">Dates</th>
                <th scope="col">Property price</th>
                <th scope="col">Status</th>
                {activeTab === 'pending' && <th scope="col" className="owner-rr-th-actions">Actions</th>}
              </tr>
            </thead>
            <tbody>
              {visibleRows.map((r) => {
                const pending = isPending(r)
                const st = String(r.status || '').toUpperCase()
                const locLine = listingSubtitle(r.listingLocation)
                const titleLine = r.listingTitle || 'Listing'
                const expanded = expandedId === r.requestId
                const showDetailToggle = Boolean(
                  r.message || r.monthlyIncome != null || r.employmentStatus,
                )
                return (
                  <Fragment key={r.requestId}>
                    <tr className="owner-rr-row">
                      <td>
                        <div className="owner-rr-renter">
                          <div className="owner-rr-avatar" aria-hidden>
                            {renterInitials(r.renterFirstname, r.renterLastname, r.renterEmail)}
                          </div>
                          <div>
                            <div className="owner-rr-renter-name">{renterDisplayName(r)}</div>
                            <div className="owner-rr-renter-email">{r.renterEmail || '—'}</div>
                          </div>
                        </div>
                      </td>
                      <td>
                        <div className="owner-rr-listing-title">{titleLine}</div>
                        <div className="owner-rr-listing-sub">{locLine || r.listingLocation || '—'}</div>
                      </td>
                      <td className="owner-rr-dates">
                        {formatDateRange(r.preferredStartDate, r.leaseDurationMonths)}
                      </td>
                      <td className="owner-rr-total">
                        {propertyMonthlyPrice(r) != null ? (
                          <>
                            {formatMoney(propertyMonthlyPrice(r))}
                            <span className="owner-rr-price-suffix">/mo</span>
                          </>
                        ) : (
                          '—'
                        )}
                      </td>
                      <td>
                        {pending ? (
                          <span className="owner-rr-badge owner-rr-badge--pending">Pending</span>
                        ) : st === 'APPROVED' ? (
                          <span className="owner-rr-badge owner-rr-badge--approved">Approved</span>
                        ) : (
                          <span className="owner-rr-badge owner-rr-badge--declined">Declined</span>
                        )}
                      </td>
                      {activeTab === 'pending' && (
                        <td className="owner-rr-actions">
                          <button
                            type="button"
                            className="owner-rr-btn owner-rr-btn--approve"
                            disabled={actingId === r.requestId}
                            onClick={() => handleApprove(r.requestId)}
                          >
                            {actingId === r.requestId ? '…' : 'Approve'}
                          </button>
                          <button
                            type="button"
                            className="owner-rr-btn owner-rr-btn--decline"
                            disabled={actingId === r.requestId}
                            onClick={() => handleDecline(r.requestId)}
                          >
                            Decline
                          </button>
                        </td>
                      )}
                    </tr>
                    {showDetailToggle && (
                      <tr className="owner-rr-row-detail">
                        <td colSpan={activeTab === 'pending' ? 6 : 5}>
                          <button
                            type="button"
                            className="owner-rr-detail-toggle"
                            onClick={() => setExpandedId(expanded ? null : r.requestId)}
                            aria-expanded={expanded}
                          >
                            {expanded ? 'Hide application details' : 'Show application details'}
                          </button>
                          {expanded && (
                            <div className="owner-rr-detail-panel">
                              <dl className="owner-rr-detail-dl">
                                <div>
                                  <dt>Monthly income</dt>
                                  <dd>{formatMoney(r.monthlyIncome)}</dd>
                                </div>
                                <div>
                                  <dt>Employment</dt>
                                  <dd>{r.employmentStatus || '—'}</dd>
                                </div>
                                <div>
                                  <dt>Flags</dt>
                                  <dd>
                                    {r.hasPets && <span className="owner-rr-chip">Pets</span>}
                                    {r.smokes && <span className="owner-rr-chip">Smokes</span>}
                                    {r.creditCheckAgreed && <span className="owner-rr-chip owner-rr-chip--ok">Credit check</span>}
                                    {!r.hasPets && !r.smokes && !r.creditCheckAgreed && '—'}
                                  </dd>
                                </div>
                              </dl>
                              {r.message && (
                                <blockquote className="owner-rr-quote">&ldquo;{r.message}&rdquo;</blockquote>
                              )}
                            </div>
                          )}
                        </td>
                      </tr>
                    )}
                  </Fragment>
                )
              })}
            </tbody>
          </table>
        </div>
      )}
    </div>
  )
}

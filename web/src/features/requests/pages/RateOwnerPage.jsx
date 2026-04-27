import { useCallback, useEffect, useMemo, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { fetchRenterRentalRequests } from '../api/rentalRequests'
import { fetchOwnerRatingSummary, submitOwnerRating } from '../../ratings/api/ratings'
import '../styles/RateOwnerPage.css'

const OVERALL_LABELS = ['Poor', 'Fair', 'Good', 'Very good', 'Excellent']

const ASPECTS = [
  { key: 'responsivenessRating', label: 'Responsiveness' },
  { key: 'listingAccuracyRating', label: 'Listing accuracy' },
  { key: 'communicationRating', label: 'Communication' },
  { key: 'fairnessRating', label: 'Fairness' },
]

function StarRow({ value, onChange, size = 'large', idPrefix, allowClear = false }) {
  const stars = [1, 2, 3, 4, 5]
  return (
    <div className={`rate-owner-stars rate-owner-stars--${size}`} role="group" aria-label="Star rating">
      {stars.map((n) => (
        <button
          key={n}
          type="button"
          id={idPrefix ? `${idPrefix}-${n}` : undefined}
          className={`rate-owner-star-btn${value != null && n <= value ? ' rate-owner-star-btn--on' : ''}`}
          onClick={() => {
            if (allowClear && n === value) onChange(null)
            else onChange(n)
          }}
          aria-pressed={value === n}
          aria-label={`${n} star${n > 1 ? 's' : ''}`}
        >
          ★
        </button>
      ))}
    </div>
  )
}

function memberSinceYear(iso) {
  if (!iso) return null
  const d = new Date(iso)
  if (Number.isNaN(d.getTime())) return null
  return d.getFullYear()
}

export default function RateOwnerPage() {
  const { requestId } = useParams()
  const navigate = useNavigate()
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [requestRow, setRequestRow] = useState(null)
  const [ownerSummary, setOwnerSummary] = useState(null)
  const [overall, setOverall] = useState(null)
  const [comment, setComment] = useState('')
  const [aspects, setAspects] = useState({
    responsivenessRating: null,
    listingAccuracyRating: null,
    communicationRating: null,
    fairnessRating: null,
  })
  const [submitting, setSubmitting] = useState(false)
  const [submitError, setSubmitError] = useState('')

  const rid = requestId != null ? parseInt(String(requestId), 10) : NaN

  const load = useCallback(async () => {
    const token = localStorage.getItem('accessToken')
    const raw = localStorage.getItem('user')
    let user = null
    try {
      user = raw ? JSON.parse(raw) : null
    } catch {
      user = null
    }
    if (!token || !user || String(user.role || '').toUpperCase() !== 'RENTER') {
      navigate('/login', { replace: true, state: { from: `/my-requests/${requestId}/rate` } })
      return
    }

    try {
      setLoading(true)
      setError('')
      const list = await fetchRenterRentalRequests()
      const rows = Array.isArray(list) ? list : []
      const match = rows.find((r) => Number(r.requestId) === rid)
      if (!match) {
        setRequestRow(null)
        setError('We could not find this rental request.')
        return
      }
      const st = String(match.status || '').toUpperCase()
      if (st !== 'APPROVED') {
        setRequestRow(null)
        setError('You can only rate the owner after your request has been approved.')
        return
      }
      if (match.ratingSubmitted) {
        setRequestRow(null)
        setError('You have already submitted a rating for this application.')
        return
      }
      const oid = match.ownerId ?? match.owner_id
      if (!oid) {
        setRequestRow(null)
        setError('Owner information is missing for this listing.')
        return
      }
      setRequestRow(match)
      const summary = await fetchOwnerRatingSummary(oid)
      setOwnerSummary(summary)
    } catch (e) {
      console.error(e)
      setError(e.message || 'Unable to load this page.')
      setRequestRow(null)
    } finally {
      setLoading(false)
    }
  }, [navigate, requestId, rid])

  useEffect(() => {
    if (Number.isNaN(rid)) {
      setLoading(false)
      setError('Invalid request.')
      return
    }
    load()
  }, [load, rid])

  const ownerName = useMemo(() => {
    if (!requestRow) return ''
    const fn = requestRow.ownerFirstname ?? requestRow.owner_firstname
    const ln = requestRow.ownerLastname ?? requestRow.owner_lastname
    const n = `${fn || ''} ${ln || ''}`.trim()
    return n || 'Property owner'
  }, [requestRow])

  const ownerInitials = useMemo(() => {
    const parts = ownerName.split(/\s+/).filter(Boolean)
    if (parts.length >= 2) {
      return (parts[0][0] + parts[parts.length - 1][0]).toUpperCase()
    }
    return ownerName.slice(0, 2).toUpperCase() || '?'
  }, [ownerName])

  const handleAspect = (key, val) => {
    setAspects((a) => ({ ...a, [key]: val }))
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    setSubmitError('')
    if (overall == null) {
      setSubmitError('Please choose an overall rating (1–5 stars).')
      return
    }
    try {
      setSubmitting(true)
      const body = {
        rentalRequestId: rid,
        rating: overall,
        comment: comment.trim() || undefined,
      }
      ASPECTS.forEach(({ key }) => {
        const v = aspects[key]
        if (v != null) body[key] = v
      })
      await submitOwnerRating(body)
      navigate('/my-requests', { replace: true, state: { rated: true } })
    } catch (err) {
      setSubmitError(err.message || 'Could not submit rating.')
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="rate-owner-page">
      <header className="rate-owner-topnav">
        <button type="button" className="rate-owner-back" onClick={() => navigate('/my-requests')}>
          ← Back to my requests
        </button>
      </header>

      <main className="rate-owner-main">
        {loading && <p className="rate-owner-status">Loading…</p>}
        {!loading && error && (
          <div className="rate-owner-panel rate-owner-panel--error">
            <p>{error}</p>
            <button type="button" className="rate-owner-btn-secondary" onClick={() => navigate('/my-requests')}>
              Return to my requests
            </button>
          </div>
        )}

        {!loading && !error && requestRow && (
          <form className="rate-owner-form" onSubmit={handleSubmit}>
            <h1 className="rate-owner-page-title">Rate property owner</h1>
            <p className="rate-owner-page-lead">
              Share your experience with <strong>{ownerName}</strong> for{' '}
              <strong>{requestRow.listingTitle || 'this listing'}</strong>. Your feedback helps other renters.
            </p>

            <section className="rate-owner-card">
              <h2 className="rate-owner-card-title">Property owner</h2>
              <div className="rate-owner-profile">
                <div className="rate-owner-avatar" aria-hidden>
                  {ownerInitials}
                </div>
                <div className="rate-owner-profile-text">
                  <p className="rate-owner-profile-name">{ownerName}</p>
                  <p className="rate-owner-profile-sub">{requestRow.listingTitle || 'Rental listing'}</p>
                  <div className="rate-owner-profile-meta">
                    <span className="rate-owner-meta-item">
                      <span className="rate-owner-meta-icon" aria-hidden>
                        ★
                      </span>
                      {ownerSummary?.averageRating != null
                        ? `${Number(ownerSummary.averageRating).toFixed(1)} (${ownerSummary.reviewCount ?? 0} reviews)`
                        : (ownerSummary?.reviewCount ?? 0) > 0
                          ? `${ownerSummary.reviewCount} reviews`
                          : 'No reviews yet'}
                    </span>
                    {memberSinceYear(ownerSummary?.memberSince) != null && (
                      <span className="rate-owner-meta-item">
                        <span className="rate-owner-meta-icon" aria-hidden>
                          <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" strokeWidth="2">
                            <rect x="3" y="4" width="18" height="18" rx="2" />
                            <path d="M16 2v4M8 2v4M3 10h18" />
                          </svg>
                        </span>
                        Member since {memberSinceYear(ownerSummary.memberSince)}
                      </span>
                    )}
                  </div>
                </div>
              </div>
            </section>

            <section className="rate-owner-card">
              <div className="rate-owner-section-head">
                <h2 className="rate-owner-card-title">Overall rating</h2>
                <span className="rate-owner-hint">Click to rate (1–5)</span>
              </div>
              <StarRow value={overall} onChange={setOverall} size="large" idPrefix="overall" />
              <div className="rate-owner-scale-labels">
                {OVERALL_LABELS.map((label, i) => (
                  <span key={label} className="rate-owner-scale-item">
                    <span className="rate-owner-scale-num">{i + 1}</span>
                    <span className="rate-owner-scale-text">{label}</span>
                  </span>
                ))}
              </div>
            </section>

            <section className="rate-owner-card">
              <h2 className="rate-owner-card-title">Your feedback</h2>
              <textarea
                className="rate-owner-textarea"
                rows={5}
                maxLength={2000}
                placeholder="Tell us about your experience… What went well? What could be improved?"
                value={comment}
                onChange={(e) => setComment(e.target.value)}
                aria-describedby="rate-owner-feedback-note"
              />
              <p id="rate-owner-feedback-note" className="rate-owner-footnote">
                <span className="rate-owner-info-icon" aria-hidden>
                  ⓘ
                </span>
                Your feedback helps improve transparency for future renters.
              </p>
            </section>

            <section className="rate-owner-card">
              <h2 className="rate-owner-card-title">Rate specific aspects</h2>
              <p className="rate-owner-aspects-lead">Optional — add detail beyond your overall score.</p>
              <ul className="rate-owner-aspects">
                {ASPECTS.map(({ key, label }) => (
                  <li key={key} className="rate-owner-aspect-row">
                    <span className="rate-owner-aspect-label">{label}</span>
                    <StarRow
                      value={aspects[key]}
                      onChange={(v) => handleAspect(key, v)}
                      size="small"
                      idPrefix={key}
                      allowClear
                    />
                  </li>
                ))}
              </ul>
            </section>

            {submitError && (
              <p className="rate-owner-submit-error" role="alert">
                {submitError}
              </p>
            )}

            <div className="rate-owner-actions">
              <button type="submit" className="rate-owner-btn-primary" disabled={submitting}>
                {submitting ? 'Submitting…' : 'Submit rating'}
              </button>
              <button
                type="button"
                className="rate-owner-btn-secondary"
                disabled={submitting}
                onClick={() => navigate('/my-requests')}
              >
                Cancel
              </button>
            </div>
          </form>
        )}
      </main>
    </div>
  )
}

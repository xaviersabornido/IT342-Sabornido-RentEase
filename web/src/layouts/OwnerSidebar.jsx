import { useEffect, useState } from 'react'
import { NavLink, useNavigate, useLocation } from 'react-router-dom'
import { fetchOwnerRatingSummary } from '../api/ratings'
import { resolveStoredUserId } from './ownerUserId'
import './OwnerLayout.css'

const nav = [
  {
    to: '/dashboard',
    key: 'dashboard',
    label: 'Dashboard',
    icon: (
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.75" strokeLinecap="round" strokeLinejoin="round" aria-hidden>
        <path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z" />
        <polyline points="9 22 9 12 15 12 15 22" />
      </svg>
    ),
  },
  {
    to: '/rental-requests',
    key: 'requests',
    label: 'Rental Requests',
    icon: (
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.75" strokeLinecap="round" strokeLinejoin="round" aria-hidden>
        <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" />
        <polyline points="14 2 14 8 20 8" />
        <line x1="16" y1="13" x2="8" y2="13" />
        <line x1="16" y1="17" x2="8" y2="17" />
      </svg>
    ),
  },
  {
    to: '/my-listings',
    key: 'listings',
    label: 'My Listings',
    icon: (
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.75" strokeLinecap="round" strokeLinejoin="round" aria-hidden>
        <rect x="4" y="2" width="16" height="20" rx="2" ry="2" />
        <path d="M9 22v-4h6v4" />
        <path d="M8 6h.01M16 6h.01M12 6h.01M8 10h.01M16 10h.01M12 10h.01" />
      </svg>
    ),
  },
  {
    to: '/owner/settings',
    key: 'settings',
    label: 'Settings',
    icon: (
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.75" strokeLinecap="round" strokeLinejoin="round" aria-hidden>
        <circle cx="12" cy="12" r="3" />
        <path d="M12 1v2M12 21v2M4.22 4.22l1.42 1.42M18.36 18.36l1.42 1.42M1 12h2M21 12h2M4.22 19.78l1.42-1.42M18.36 5.64l1.42-1.42" />
      </svg>
    ),
  },
]

export default function OwnerSidebar({ user }) {
  const navigate = useNavigate()
  const location = useLocation()
  const displayName = user?.firstname ? `${user.firstname} ${user.lastname}`.trim() : user?.email || 'Owner'
  const [ratingLine, setRatingLine] = useState(null)
  const [ratingLoading, setRatingLoading] = useState(true)

  useEffect(() => {
    const uid = resolveStoredUserId(user)
    if (!uid) {
      setRatingLoading(false)
      setRatingLine(null)
      return
    }
    let cancelled = false
    setRatingLoading(true)
    fetchOwnerRatingSummary(uid)
      .then((data) => {
        if (cancelled) return
        const n = data?.reviewCount ?? 0
        const avg = data?.averageRating
        if (avg != null && n > 0) {
          setRatingLine(`${Number(avg).toFixed(1)} ★ · ${n} review${n === 1 ? '' : 's'}`)
        } else {
          setRatingLine('No renter reviews yet')
        }
      })
      .catch(() => {
        if (!cancelled) setRatingLine(null)
      })
      .finally(() => {
        if (!cancelled) setRatingLoading(false)
      })
    return () => {
      cancelled = true
    }
  }, [user, location.pathname])

  const handleLogout = () => {
    localStorage.removeItem('accessToken')
    localStorage.removeItem('user')
    navigate('/login')
  }

  return (
    <aside className="owner-sidebar" aria-label="Owner navigation">
      <div className="owner-sidebar-brand">
        <div className="owner-sidebar-logo">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round" aria-hidden>
            <path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z" />
            <polyline points="9 22 9 12 15 12 15 22" />
          </svg>
        </div>
        <span className="owner-sidebar-title">RentEase</span>
      </div>
      <nav className="owner-sidebar-nav">
        {nav.map((item) => (
          <NavLink
            key={item.key}
            to={item.to}
            className={({ isActive }) => `owner-nav-link${isActive ? ' owner-nav-link-active' : ''}`}
          >
            <span className="owner-nav-icon">{item.icon}</span>
            {item.label}
          </NavLink>
        ))}
      </nav>
      <div className="owner-sidebar-footer">
        <div className="owner-sidebar-rating" title="Average rating from renters who completed a review after an approved request">
          <span className="owner-sidebar-rating-label">Your host rating</span>
          <span className="owner-sidebar-rating-value" aria-live="polite">
            {ratingLoading ? '…' : ratingLine || '—'}
          </span>
        </div>
        <p className="owner-sidebar-user">{displayName}</p>
        <button type="button" className="owner-sidebar-logout" onClick={handleLogout}>
          Log out
        </button>
      </div>
    </aside>
  )
}

import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import './Dashboard.css'

export default function Dashboard() {
  const navigate = useNavigate()
  const [user, setUser] = useState({
    name: 'John Doe',
    email: 'john@example.com',
    role: 'Landlord'
  })

  const handleLogout = () => {
    localStorage.removeItem('accessToken')
    navigate('/login')
  }

  return (
    <div className="dashboard-page">
      {/* Header/Navbar */}
      <header className="dashboard-header">
        <div className="header-content">
          <div className="header-brand">
            <div className="brand-icon">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
                <path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z" />
                <polyline points="9 22 9 12 15 12 15 22" />
              </svg>
            </div>
            <h1 className="header-title">RentEase</h1>
          </div>
          <div className="header-actions">
            <button className="btn-profile">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2" />
                <circle cx="12" cy="7" r="4" />
              </svg>
              <span>{user.name}</span>
            </button>
            <button className="btn-logout" onClick={handleLogout}>Logout</button>
          </div>
        </div>
      </header>

      {/* Main Content */}
      <main className="dashboard-main">
        {/* Greeting */}
        <section className="greeting-section">
          <div className="greeting-content">
            <h2 className="greeting-title">Welcome back, {user.name.split(' ')[0]}! 👋</h2>
            <p className="greeting-subtitle">Here's what's happening with your properties today</p>
          </div>
        </section>

        {/* Stats Grid */}
        <section className="stats-section">
          <div className="stat-card stat-card-1">
            <div className="stat-icon">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                <path d="M3 12a9 9 0 0 1 9-9 9.75 9.75 0 0 1 6.74 2.74L21 8" />
                <path d="M21 3v5h-5" />
                <path d="M21 12a9 9 0 0 1-9 9 9.75 9.75 0 0 1-6.74-2.74L3 16" />
                <path d="M3 21v-5h5" />
              </svg>
            </div>
            <div className="stat-info">
              <h3 className="stat-label">Active Listings</h3>
              <p className="stat-value">12</p>
            </div>
          </div>

          <div className="stat-card stat-card-2">
            <div className="stat-icon">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z" />
              </svg>
            </div>
            <div className="stat-info">
              <h3 className="stat-label">Pending Requests</h3>
              <p className="stat-value">5</p>
            </div>
          </div>

          <div className="stat-card stat-card-3">
            <div className="stat-icon">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2z" />
                <polyline points="12 6 12 12 16 14" />
              </svg>
            </div>
            <div className="stat-info">
              <h3 className="stat-label">Scheduled Tours</h3>
              <p className="stat-value">8</p>
            </div>
          </div>

          <div className="stat-card stat-card-4">
            <div className="stat-icon">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                <line x1="12" y1="1" x2="12" y2="23" />
                <path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6" />
              </svg>
            </div>
            <div className="stat-info">
              <h3 className="stat-label">Monthly Revenue</h3>
              <p className="stat-value">$4,280</p>
            </div>
          </div>
        </section>

        {/* Content Grid */}
        <div className="dashboard-grid">
          {/* Recent Activity */}
          <section className="card recent-activity-card">
            <div className="card-header">
              <h3 className="card-title">Recent Activity</h3>
              <a href="#" className="card-link">View All</a>
            </div>
            <div className="activity-list">
              <div className="activity-item">
                <div className="activity-icon activity-icon-1">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                    <path d="M12 5v14M5 12h14" />
                  </svg>
                </div>
                <div className="activity-content">
                  <p className="activity-title">New rental request for Apartment 4B</p>
                  <p className="activity-time">2 hours ago</p>
                </div>
              </div>
              <div className="activity-item">
                <div className="activity-icon activity-icon-2">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                    <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2" />
                    <circle cx="12" cy="7" r="4" />
                  </svg>
                </div>
                <div className="activity-content">
                  <p className="activity-title">New 5-star review from Sarah M.</p>
                  <p className="activity-time">5 hours ago</p>
                </div>
              </div>
              <div className="activity-item">
                <div className="activity-icon activity-icon-3">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                    <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z" />
                  </svg>
                </div>
                <div className="activity-content">
                  <p className="activity-title">Message from John D.</p>
                  <p className="activity-time">1 day ago</p>
                </div>
              </div>
            </div>
          </section>

          {/* Quick Actions */}
          <section className="card quick-actions-card">
            <div className="card-header">
              <h3 className="card-title">Quick Actions</h3>
            </div>
            <div className="action-buttons">
              <button className="action-btn action-btn-primary">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                  <path d="M12 5v14M5 12h14" />
                </svg>
                <span>Add Listing</span>
              </button>
              <button className="action-btn action-btn-secondary">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                  <path d="M3 12a9 9 0 1 0 18 0 9 9 0 0 0-18 0" />
                  <path d="M12 6v6l4 2" />
                </svg>
                <span>Schedule Tour</span>
              </button>
              <button className="action-btn action-btn-tertiary">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                  <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z" />
                </svg>
                <span>View Messages</span>
              </button>
            </div>
          </section>
        </div>
      </main>
    </div>
  )
}

import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import './Login.css'
import './Register.css'
import { register } from '../api/auth'

export default function Register() {
  const [firstname, setFirstname] = useState('')
  const [lastname, setLastname] = useState('')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [confirmPassword, setConfirmPassword] = useState('')
  const [role, setRole] = useState('RENTER')
  const [error, setError] = useState('')

  const navigate = useNavigate()

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')
    
    // Frontend validation
    if (!firstname.trim()) {
      setError('First name is required')
      return
    }
    if (!lastname.trim()) {
      setError('Last name is required')
      return
    }
    if (!email.trim()) {
      setError('Email is required')
      return
    }
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
      setError('Please enter a valid email address')
      return
    }
    if (password.length < 8) {
      setError('Password must be at least 8 characters')
      return
    }
    if (password !== confirmPassword) {
      setError('Passwords do not match')
      return
    }
    if (!['RENTER', 'OWNER'].includes(role)) {
      setError('Please select a valid role')
      return
    }

    try {
      await register({ email, password, firstname, lastname, role })
      // registration succeeded, go back to login page
      navigate('/login')
    } catch (err) {
      console.error('registration error', err)
      setError(err.message || 'Unable to register')
    }
  }

  return (
    <div className="login-page">
      <div className="login-container register-container">
        {/* Left: Branding (same as Login) */}
        <div className="login-brand">
          <div className="brand-icon">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
              <path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z" />
              <polyline points="9 22 9 12 15 12 15 22" />
            </svg>
          </div>
          <h1 className="brand-name">RentEase</h1>
          <p className="brand-tagline">
            Simplify your rental experience with our comprehensive property management platform.
          </p>
          <div className="brand-features">
            <div className="feature">
              <span className="feature-icon">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                  <circle cx="11" cy="11" r="8" />
                  <line x1="21" y1="21" x2="16.65" y2="16.65" />
                </svg>
              </span>
              <span className="feature-label">Find</span>
            </div>
            <div className="feature">
              <span className="feature-icon">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                  <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2" />
                  <circle cx="9" cy="7" r="4" />
                  <path d="M23 21v-2a4 4 0 0 0-3-3.87" />
                  <path d="M16 3.13a4 4 0 0 1 0 7.75" />
                </svg>
              </span>
              <span className="feature-label">Connect</span>
            </div>
            <div className="feature">
              <span className="feature-icon">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                  <path d="M21 2l-2 2m-7.61 7.61a5.5 5.5 0 1 1-7.778 7.778 5.5 5.5 0 0 1 7.777-7.777zm0 0L15.5 7.5m0 0l3 3L22 7l-3-3m-3.5 3.5L19 4" />
                </svg>
              </span>
              <span className="feature-label">Rent</span>
            </div>
          </div>
        </div>

        {/* Right: Registration Form */}
        <div className="login-form-wrap register-form-wrap">
          <div className="login-form-card register-form-card">
            <h2 className="form-title">Create Account</h2>
            <p className="form-subtitle">Join RentEase to find or list rentals</p>

            <form onSubmit={handleSubmit} className="login-form">
              <div className="form-row">
                <div className="form-group">
                  <label htmlFor="firstname">First Name</label>
                  <div className="input-wrap">
                    <input
                      id="firstname"
                      type="text"
                      placeholder="Juan"
                      value={firstname}
                      onChange={(e) => setFirstname(e.target.value)}
                      required
                      autoComplete="given-name"
                    />
                  </div>
                </div>
                <div className="form-group">
                  <label htmlFor="lastname">Last Name</label>
                  <div className="input-wrap">
                    <input
                      id="lastname"
                      type="text"
                      placeholder="Dela Cruz"
                      value={lastname}
                      onChange={(e) => setLastname(e.target.value)}
                      required
                      autoComplete="family-name"
                    />
                  </div>
                </div>
              </div>

              <div className="form-group">
                <label htmlFor="email">Email Address</label>
                <div className="input-wrap">
                  <input
                    id="email"
                    type="email"
                    placeholder="Enter your email"
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    required
                    autoComplete="email"
                  />
                  <span className="input-icon">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                      <path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z" />
                      <polyline points="22,6 12,13 2,6" />
                    </svg>
                  </span>
                </div>
              </div>

              <div className="form-group">
                <label htmlFor="role">I am a</label>
                <div className="input-wrap input-wrap-select">
                  <select
                    id="role"
                    value={role}
                    onChange={(e) => setRole(e.target.value)}
                    required
                    className="role-select"
                  >
                    <option value="RENTER">Renter – looking for a place</option>
                    <option value="OWNER">Owner – listing properties</option>
                  </select>
                </div>
              </div>

              <div className="form-group">
                <label htmlFor="password">Password</label>
                <div className="input-wrap">
                  <input
                    id="password"
                    type="password"
                    placeholder="At least 8 characters"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    required
                    minLength={8}
                    autoComplete="new-password"
                  />
                  <span className="input-icon">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                      <rect x="3" y="11" width="18" height="11" rx="2" ry="2" />
                      <path d="M7 11V7a5 5 0 0 1 10 0v4" />
                    </svg>
                  </span>
                </div>
              </div>

              <div className="form-group">
                <label htmlFor="confirmPassword">Confirm Password</label>
                <div className="input-wrap">
                  <input
                    id="confirmPassword"
                    type="password"
                    placeholder="Confirm your password"
                    value={confirmPassword}
                    onChange={(e) => setConfirmPassword(e.target.value)}
                    required
                    minLength={8}
                    autoComplete="new-password"
                  />
                  <span className="input-icon">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                      <rect x="3" y="11" width="18" height="11" rx="2" ry="2" />
                      <path d="M7 11V7a5 5 0 0 1 10 0v4" />
                    </svg>
                  </span>
                </div>
              </div>

              {error && <p className="form-error">{error}</p>}

              <button type="submit" className="btn-signin">Create Account</button>
            </form>

            <p className="form-footer">
              Already have an account?{' '}
              <Link to="/login" className="link-signup">Sign in</Link>
            </p>
          </div>
        </div>
      </div>
    </div>
  )
}

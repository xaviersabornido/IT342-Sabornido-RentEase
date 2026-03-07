import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import './Login.css'
import { login } from '../api/auth'

export default function Login() {
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [remember, setRemember] = useState(false)

  const navigate = useNavigate()
  const [error, setError] = useState('')

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')
    try {
      const data = await login({ email, password })
      // persist tokens to localStorage for later requests
      if (data?.accessToken) {
        localStorage.setItem('accessToken', data.accessToken)
      }
      // navigate to dashboard
      navigate('/dashboard')
      console.log('login success', data)
    } catch (err) {
      console.error('login failed', err)
      setError(err.message || 'Login failed')
    }
  }

  return (
    <div className="login-page">
      <div className="login-container">
        {/* Left: Branding */}
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

        {/* Right: Form */}
        <div className="login-form-wrap">
          <div className="login-form-card">
            <h2 className="form-title">Welcome Back</h2>
            <p className="form-subtitle">Sign in to your account</p>

            <form onSubmit={handleSubmit} className="login-form">
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
                <label htmlFor="password">Password</label>
                <div className="input-wrap">
                  <input
                    id="password"
                    type="password"
                    placeholder="Enter your password"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    required
                    autoComplete="current-password"
                  />
                  <span className="input-icon">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                      <rect x="3" y="11" width="18" height="11" rx="2" ry="2" />
                      <path d="M7 11V7a5 5 0 0 1 10 0v4" />
                    </svg>
                  </span>
                </div>
              </div>

              <div className="form-options">
                <label className="checkbox-wrap">
                  <input
                    type="checkbox"
                    checked={remember}
                    onChange={(e) => setRemember(e.target.checked)}
                  />
                  <span className="checkmark" />
                  Remember me
                </label>
                <Link to="/forgot-password" className="link-forgot">Forgot password?</Link>
              </div>

              <button type="submit" className="btn-signin">Sign In</button>
            </form>
            {error && <p className="form-error">{error}</p>}

            <p className="form-footer">
              Don&apos;t have an account?{' '}
              <Link to="/register" className="link-signup">Sign up</Link>
            </p>
          </div>
        </div>
      </div>
    </div>
  )
}

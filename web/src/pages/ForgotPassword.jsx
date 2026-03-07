import { Link } from 'react-router-dom'

export default function ForgotPassword() {
  return (
    <div style={{ padding: 48, textAlign: 'center', fontFamily: 'system-ui' }}>
      <h1>Forgot password?</h1>
      <p>Password reset – coming soon.</p>
      <Link to="/login">Back to Sign In</Link>
    </div>
  )
}

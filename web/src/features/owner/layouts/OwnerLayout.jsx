import { useEffect, useMemo } from 'react'
import { Outlet, useLocation, useNavigate } from 'react-router-dom'
import OwnerSidebar from './OwnerSidebar'
import '../styles/OwnerLayout.css'

export default function OwnerLayout() {
  const navigate = useNavigate()
  const location = useLocation()

  const user = useMemo(() => {
    const raw = localStorage.getItem('user')
    if (!raw) return null
    try {
      return JSON.parse(raw)
    } catch {
      return null
    }
  }, [location.pathname])

  useEffect(() => {
    if (!localStorage.getItem('accessToken') || !user) {
      navigate('/login', { replace: true, state: { from: location.pathname } })
      return
    }
    if (String(user.role || '').toUpperCase() !== 'OWNER') {
      navigate('/dashboard', { replace: true })
    }
  }, [user, navigate, location.pathname])

  if (!user || String(user.role || '').toUpperCase() !== 'OWNER') {
    return null
  }

  return (
    <div className="owner-layout">
      <OwnerSidebar user={user} />
      <div className="owner-main">
        <Outlet />
      </div>
    </div>
  )
}

import { Routes, Route, Navigate } from 'react-router-dom'
import Login from './features/auth/pages/Login'
import Register from './features/auth/pages/Register'
import ForgotPassword from './features/auth/pages/ForgotPassword'
import Dashboard from './features/listings/pages/Dashboard'
import PropertyDetails from './features/listings/pages/PropertyDetails'
import ListProperty from './features/listings/pages/ListProperty'
import EditListing from './features/listings/pages/EditListing'
import MyListings from './features/listings/pages/MyListings'
import RentalRequestsPage from './features/requests/pages/RentalRequestsPage'
import MyRentalRequestsPage from './features/requests/pages/MyRentalRequestsPage'
import RateOwnerPage from './features/requests/pages/RateOwnerPage'
import OwnerSettingsPlaceholder from './features/owner/pages/OwnerSettingsPlaceholder'
import OwnerLayout from './features/owner/layouts/OwnerLayout'
import './App.css'

function App() {
  return (
    <Routes>
      <Route path="/" element={<Navigate to="/dashboard" replace />} />
      <Route path="/login" element={<Login />} />
      <Route path="/register" element={<Register />} />
      <Route path="/forgot-password" element={<ForgotPassword />} />
      <Route path="/dashboard" element={<Dashboard />} />
      <Route path="/listings/new" element={<ListProperty />} />
      <Route path="/listings/:id/edit" element={<EditListing />} />
      <Route path="/listings/:id" element={<PropertyDetails />} />
      <Route path="/my-requests/:requestId/rate" element={<RateOwnerPage />} />
      <Route path="/my-requests" element={<MyRentalRequestsPage />} />
      <Route element={<OwnerLayout />}>
        <Route path="/my-listings" element={<MyListings />} />
        <Route path="/rental-requests" element={<RentalRequestsPage />} />
        <Route path="/owner/settings" element={<OwnerSettingsPlaceholder />} />
      </Route>
    </Routes>
  )
}

export default App

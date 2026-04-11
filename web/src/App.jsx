import { Routes, Route, Navigate } from 'react-router-dom'
import Login from './pages/Login'
import Register from './pages/Register'
import ForgotPassword from './pages/ForgotPassword'
import Dashboard from './pages/Dashboard'
import PropertyDetails from './pages/PropertyDetails'
import ListProperty from './pages/ListProperty'
import EditListing from './pages/EditListing'
import MyListings from './pages/MyListings'
import RentalRequestsPage from './pages/RentalRequestsPage'
import MyRentalRequestsPage from './pages/MyRentalRequestsPage'
import RateOwnerPage from './pages/RateOwnerPage'
import OwnerSettingsPlaceholder from './pages/OwnerSettingsPlaceholder'
import OwnerLayout from './layouts/OwnerLayout'
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

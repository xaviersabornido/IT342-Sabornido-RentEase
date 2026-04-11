import './MyListings.css'

export default function OwnerSettingsPlaceholder() {
  return (
    <div className="my-listings-page">
      <header className="my-listings-header">
        <div className="my-listings-header-text">
          <h1 className="my-listings-title">Settings</h1>
          <p className="my-listings-subtitle">Account and notification preferences</p>
        </div>
      </header>
      <div className="my-listings-divider" />
      <div className="my-listings-empty">
        <p>Owner settings will appear here.</p>
      </div>
    </div>
  )
}

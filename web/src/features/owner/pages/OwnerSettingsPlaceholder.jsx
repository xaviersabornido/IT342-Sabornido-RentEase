import '../styles/OwnerSettingsPlaceholder.css'

export default function OwnerSettingsPlaceholder() {
  return (
    <div className="owner-settings-page">
      <header className="owner-settings-header">
        <div>
          <h1 className="owner-settings-title">Settings</h1>
          <p className="owner-settings-subtitle">Account and notification preferences</p>
        </div>
      </header>
      <div className="owner-settings-divider" />
      <div className="owner-settings-empty">
        <p>Owner settings will appear here.</p>
      </div>
    </div>
  )
}

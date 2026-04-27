/** Resolve current user id from stored profile or JWT `sub` (for older sessions without `user.id`). */
export function resolveStoredUserId(user) {
  if (user?.id) return String(user.id)
  const token = localStorage.getItem('accessToken')
  if (!token) return null
  try {
    const part = token.split('.')[1]
    if (!part) return null
    const b64 = part.replace(/-/g, '+').replace(/_/g, '/')
    const pad = b64.length % 4 === 0 ? '' : '='.repeat(4 - (b64.length % 4))
    const json = JSON.parse(atob(b64 + pad))
    return json.sub ? String(json.sub) : null
  } catch {
    return null
  }
}

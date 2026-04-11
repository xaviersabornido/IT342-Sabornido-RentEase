const API_BASE = import.meta.env.VITE_API_BASE || 'http://localhost:8080/api/v1'

async function checkResponse(res) {
  const body = await res.json()
  if (!res.ok || !body.success) {
    let msg = body?.error?.message || body?.message || res.statusText
    if (body?.error?.details && typeof body.error.details === 'object') {
      const details = body.error.details
      msg = Object.entries(details)
        .map(([field, message]) => `${field}: ${message}`)
        .join('\n') || msg
    }
    const err = new Error(msg)
    err.payload = body
    throw err
  }
  return body.data
}

function authHeaders() {
  const token = localStorage.getItem('accessToken')
  const headers = { 'Content-Type': 'application/json' }
  if (token) headers.Authorization = `Bearer ${token}`
  return headers
}

export async function createRentalRequest(payload) {
  const res = await fetch(`${API_BASE}/requests`, {
    method: 'POST',
    headers: authHeaders(),
    credentials: 'include',
    body: JSON.stringify(payload),
  })
  return checkResponse(res)
}

export async function fetchRenterRentalRequests() {
  const res = await fetch(`${API_BASE}/requests/user`, {
    method: 'GET',
    headers: authHeaders(),
    credentials: 'include',
  })
  return checkResponse(res)
}

export async function fetchOwnerRentalRequests() {
  const res = await fetch(`${API_BASE}/requests/owner`, {
    method: 'GET',
    headers: authHeaders(),
    credentials: 'include',
  })
  return checkResponse(res)
}

export async function approveRentalRequest(id) {
  const res = await fetch(`${API_BASE}/requests/${id}/approve`, {
    method: 'PUT',
    headers: authHeaders(),
    credentials: 'include',
  })
  return checkResponse(res)
}

export async function declineRentalRequest(id) {
  const res = await fetch(`${API_BASE}/requests/${id}/decline`, {
    method: 'PUT',
    headers: authHeaders(),
    credentials: 'include',
  })
  return checkResponse(res)
}

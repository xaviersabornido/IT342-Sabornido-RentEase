// helper functions for authentication API calls
const API_BASE = import.meta.env.VITE_API_BASE || 'http://localhost:8080/api/v1';

async function checkResponse(res) {
  const body = await res.json();
  if (!res.ok || !body.success) {
    // pick up message from body.error
    let msg = body?.error?.message || body?.message || res.statusText;
    
    // if there are validation details, format them nicely
    if (body?.error?.details && typeof body.error.details === 'object') {
      const details = body.error.details;
      const errorMessages = Object.entries(details)
        .map(([field, message]) => `${field}: ${message}`)
        .join('\n');
      msg = errorMessages || msg;
    }
    
    const err = new Error(msg);
    err.payload = body;
    throw err;
  }
  return body.data;
}

export async function register({ email, password, firstname, lastname, role }) {
  const res = await fetch(`${API_BASE}/auth/register`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ email, password, firstname, lastname, role }),
    credentials: 'include',
  });
  return checkResponse(res);
}

export async function login({ email, password }) {
  const res = await fetch(`${API_BASE}/auth/login`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ email, password }),
    credentials: 'include',
  });
  return checkResponse(res);
}

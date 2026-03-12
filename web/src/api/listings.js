const API_BASE = import.meta.env.VITE_API_BASE || 'http://localhost:8080/api/v1';

async function checkResponse(res) {
  const body = await res.json();
  if (!res.ok || !body.success) {
    let msg = body?.error?.message || body?.message || res.statusText;

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

export async function fetchListings() {
  const res = await fetch(`${API_BASE}/listings`, {
    method: 'GET',
    headers: { 'Content-Type': 'application/json' },
  });
  return checkResponse(res);
}

export async function fetchListingById(id) {
  const res = await fetch(`${API_BASE}/listings/${id}`, {
    method: 'GET',
    headers: { 'Content-Type': 'application/json' },
  });
  return checkResponse(res);
}

export async function createListing({
  title,
  price,
  location,
  description,
  propertyType,
  amenities,
  imageUrls,
  bedrooms,
  bathrooms,
  areaSqFt,
  parkingSpaces,
  availableFrom,
  leaseTermMonths,
  deposit,
  furnished,
  petsAllowed,
  utilitiesEstimate,
}) {
  const token = localStorage.getItem('accessToken');
  const headers = {
    'Content-Type': 'application/json',
  };
  if (token) {
    headers.Authorization = `Bearer ${token}`;
  }

  const res = await fetch(`${API_BASE}/listings`, {
    method: 'POST',
    headers,
    credentials: 'include',
    body: JSON.stringify({
      title,
      price: Number(price),
      location,
      description,
      propertyType,
      amenities,
      imageUrls,
      bedrooms: bedrooms != null && bedrooms !== '' ? Number(bedrooms) : null,
      bathrooms: bathrooms != null && bathrooms !== '' ? Number(bathrooms) : null,
      areaSqFt: areaSqFt != null && areaSqFt !== '' ? Number(areaSqFt) : null,
      parkingSpaces: parkingSpaces != null && parkingSpaces !== '' ? Number(parkingSpaces) : null,
      availableFrom: availableFrom || null,
      leaseTermMonths: leaseTermMonths != null && leaseTermMonths !== '' ? Number(leaseTermMonths) : null,
      deposit: deposit != null && deposit !== '' ? Number(deposit) : null,
      furnished,
      petsAllowed,
      utilitiesEstimate: utilitiesEstimate != null && utilitiesEstimate !== '' ? Number(utilitiesEstimate) : null,
    }),
  });

  return checkResponse(res);
}


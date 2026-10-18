const API_BASE = import.meta.env.VITE_API_URL || "http://localhost:8080/api";

export async function fetchItems() {
  const res = await fetch(`${API_BASE}/items`);
  if (!res.ok) throw new Error("Failed to fetch items");
  return res.json();
}

export async function fetchItemById(id) {
  const res = await fetch(`${API_BASE}/items/${id}`);
  if (!res.ok) throw new Error("Failed to fetch item");
  return res.json();
}

export async function createItem(itemData) {
  const res = await fetch(`${API_BASE}/items`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(itemData),
  });
  if (!res.ok) throw new Error("Failed to save item");
  return res.json();
}

export async function deleteItem(id) {
  const res = await fetch(`${API_BASE}/items/${id}`, {
    method: "DELETE",
  });
  if (!res.ok) throw new Error("Failed to delete item");
}

export async function fetchClaims() {
  const res = await fetch(`${API_BASE}/claims`);
  if (!res.ok) throw new Error("Failed to fetch claims");
  return res.json();
}

export async function createClaim(claimData) {
  const res = await fetch(`${API_BASE}/claims`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(claimData),
  });
  if (!res.ok) throw new Error("Failed to submit claim");
  return res.json();
}

export async function updateClaimStatus(claimId, status) {
  const res = await fetch(`${API_BASE}/claims/${claimId}/status`, {
    method: "PATCH",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ status }),
  });
  if (!res.ok) throw new Error("Failed to update claim status");
  return res.json();
}

export async function fetchMatchesForItem(itemId) {
  try {
    const res = await fetch(`${API_BASE}/matches/item/${itemId}`);
    if (!res.ok) return [];
    return await res.json();
  } catch {
    return [];
  }
}

export async function loginUser(credentials) {
  const res = await fetch(`${API_BASE}/auth/login`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(credentials),
  });
  const data = await res.json();
  if (!res.ok) {
    throw new Error(data.error || "Failed to log in");
  }
  return data;
}

export async function registerUser(userData) {
  const res = await fetch(`${API_BASE}/auth/register`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(userData),
  });
  const data = await res.json();
  if (!res.ok) {
    throw new Error(data.error || "Failed to create account");
  }
  return data;
}

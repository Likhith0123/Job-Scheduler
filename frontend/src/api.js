const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080';
const AUTH_URL = import.meta.env.VITE_AUTH_URL || 'http://localhost:8081';

const API_KEY_STORAGE = 'chronoflow_api_key';

export function getApiKey() {
  return localStorage.getItem(API_KEY_STORAGE) || '';
}

export function setApiKey(key) {
  localStorage.setItem(API_KEY_STORAGE, key.trim());
}

export function clearApiKey() {
  localStorage.removeItem(API_KEY_STORAGE);
}

async function parseResponse(res) {
  if (!res.ok) {
    const text = await res.text();
    throw new Error(text || res.statusText);
  }
  if (res.status === 204) {
    return null;
  }
  return res.json();
}

export async function gatewayFetch(path, options = {}) {
  const apiKey = getApiKey();
  const headers = {
    'Content-Type': 'application/json',
    ...(apiKey ? { 'X-API-Key': apiKey } : {}),
    ...options.headers,
  };
  const res = await fetch(`${API_URL}${path}`, { ...options, headers });
  return parseResponse(res);
}

export async function authFetch(path, options = {}) {
  const headers = {
    'Content-Type': 'application/json',
    ...options.headers,
  };
  const res = await fetch(`${AUTH_URL}${path}`, { ...options, headers });
  return parseResponse(res);
}

export const jobsApi = {
  list: () => gatewayFetch('/api/jobs'),
  get: (id) => gatewayFetch(`/api/jobs/${id}`),
  create: (body) => gatewayFetch('/api/jobs', { method: 'POST', body: JSON.stringify(body) }),
  pause: (id) => gatewayFetch(`/api/jobs/${id}/pause`, { method: 'POST' }),
  resume: (id) => gatewayFetch(`/api/jobs/${id}/resume`, { method: 'POST' }),
  remove: (id) => gatewayFetch(`/api/jobs/${id}`, { method: 'DELETE' }),
  runs: (id) => gatewayFetch(`/api/jobs/${id}/runs`),
};

export const authApi = {
  createTenant: (name, rateLimitPerMinute) =>
    authFetch('/api/auth/tenants', {
      method: 'POST',
      body: JSON.stringify({ name, rateLimitPerMinute }),
    }),
  createKey: (tenantId, label) =>
    authFetch('/api/auth/keys', {
      method: 'POST',
      body: JSON.stringify({ tenantId, label }),
    }),
  listTenants: () => authFetch('/api/auth/tenants'),
};

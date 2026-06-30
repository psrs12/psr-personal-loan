import { API } from './config.js';

function getSession() {
  return {
    token: sessionStorage.getItem('sessionToken'),
    applicationId: sessionStorage.getItem('applicationId'),
  };
}

function authHeaders() {
  const { token } = getSession();
  return token ? { Authorization: `Bearer ${token}` } : {};
}

async function handleResponse(res) {
  if (!res.ok) {
    let detail = `HTTP ${res.status}`;
    try {
      const body = await res.json();
      detail = body.detail ?? body.message ?? detail;
    } catch (_) {}
    throw new Error(detail);
  }
  if (res.status === 204) return null;
  return res.json();
}

export async function validateInvitation(token) {
  const res = await fetch(`${API.appManagement}/invitations/validate`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ token }),
  });
  return handleResponse(res);
}

export async function createApplication(payload) {
  const res = await fetch(`${API.appManagement}/applications`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json', ...authHeaders() },
    body: JSON.stringify(payload),
  });
  return handleResponse(res);
}

export async function login(applicationId, last4SSN, dateOfBirth) {
  const res = await fetch(`${API.appManagement}/applications/login`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ applicationId, last4SSN, dateOfBirth }),
  });
  return handleResponse(res);
}

export async function getApplication(applicationId) {
  const res = await fetch(`${API.appManagement}/applications/${applicationId}`, {
    headers: authHeaders(),
  });
  return handleResponse(res);
}

export async function getDeclarations(applicationId) {
  const res = await fetch(`${API.offerAcceptance}/applications/${applicationId}/declarations`, {
    headers: authHeaders(),
  });
  return handleResponse(res);
}

export async function submitESign(applicationId, declarationsAccepted) {
  const res = await fetch(`${API.offerAcceptance}/applications/${applicationId}/esign`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json', ...authHeaders() },
    body: JSON.stringify({ declarationsAccepted }),
  });
  return handleResponse(res);
}

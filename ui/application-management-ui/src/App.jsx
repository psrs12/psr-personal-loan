import React from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import InvitePage from './pages/InvitePage.jsx';
import LoginPage from './pages/LoginPage.jsx';
import StatusPage from './pages/StatusPage.jsx';

export default function App() {
  return (
    <Routes>
      <Route path="/invite" element={<InvitePage />} />
      <Route path="/portal/login" element={<LoginPage />} />
      <Route path="/portal/status" element={<StatusPage />} />
      <Route path="*" element={<Navigate to="/portal/login" replace />} />
    </Routes>
  );
}

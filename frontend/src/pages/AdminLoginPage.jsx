import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import BrandHeader from '../components/BrandHeader';
import MessageBox from '../components/MessageBox';
import { API_URL } from '../lib/api';
import { saveSession } from '../lib/session';

export default function AdminLoginPage() {
  const navigate = useNavigate();
  const [form, setForm] = useState({ username: 'admin', password: 'admin' });
  const [message, setMessage] = useState('');

  const handleLogin = async (event) => {
    event.preventDefault();
    setMessage('');

    try {
      const response = await fetch(`${API_URL}/auth/login`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ username: form.username, password: form.password })
      });

      const data = await response.json();
      if (!response.ok) {
        throw new Error(data.message || 'Invalid admin credentials.');
      }

      if (data.role !== 'ADMIN') {
        throw new Error('This account is not an admin account.');
      }

      saveSession(data);
      navigate('/admin/dashboard');
    } catch (error) {
      setMessage(error.message);
    }
  };

  return (
    <div className="auth-shell admin-shell">
      <div className="auth-card small-card admin-card">
        <BrandHeader eyebrow="Admin access" title="Backstage login" variant="admin" />

        <MessageBox message={message} />

        <form className="auth-form" onSubmit={handleLogin}>
          <label>
            <span>Username</span>
            <input type="text" value={form.username} onChange={(e) => setForm({ ...form, username: e.target.value })} required />
          </label>

          <label>
            <span>Password</span>
            <input type="password" value={form.password} onChange={(e) => setForm({ ...form, password: e.target.value })} required />
          </label>

          <button type="submit" className="primary-btn">Admin login</button>
          <button type="button" className="secondary-btn" onClick={() => navigate('/')}>Back to user portal</button>
        </form>
      </div>
    </div>
  );
}

import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import BrandHeader from '../components/BrandHeader';
import MessageBox from '../components/MessageBox';
import { API_URL } from '../lib/api';
import { saveSession } from '../lib/session';

export default function AuthPage() {
  const navigate = useNavigate();
  const [mode, setMode] = useState('signin');
  const [message, setMessage] = useState('');
  const [signup, setSignup] = useState({ name: '', username: '', email: '', password: '' });
  const [signin, setSignin] = useState({ identifier: '', password: '' });

  const submitSignin = async (event) => {
    event.preventDefault();
    setMessage('');

    try {
      const payload = {
        username: signin.identifier,
        email: signin.identifier.includes('@') ? signin.identifier : '',
        password: signin.password
      };

      const response = await fetch(`${API_URL}/auth/login`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
      });

      const data = await response.json();
      if (!response.ok) {
        throw new Error(data.message || 'Login failed.');
      }

      if (data.role === 'ADMIN') {
        saveSession(data);
        navigate('/admin/dashboard');
        return;
      }

      saveSession(data);
      navigate('/events');
    } catch (error) {
      setMessage(error.message);
    }
  };

  const submitSignup = async (event) => {
    event.preventDefault();
    setMessage('');

    try {
      const response = await fetch(`${API_URL}/auth/register`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          name: signup.name,
          username: signup.username,
          email: signup.email,
          password: signup.password,
          role: 'USER'
        })
      });

      const data = await response.json();
      if (!response.ok) {
        throw new Error(data.message || 'Registration failed.');
      }

      saveSession(data);
      setMessage('Account created successfully.');
      navigate('/events');
    } catch (error) {
      setMessage(error.message);
    }
  };

  return (
    <div className="auth-shell">
      <div className="auth-card auth-card-single">
        <BrandHeader eyebrow="Campus events" title="Event & Venue Management" />

        <div className="tab-bar" aria-label="Authentication switch">
          <button type="button" className={mode === 'signin' ? 'active' : ''} onClick={() => setMode('signin')}>Sign in</button>
          <button type="button" className={mode === 'signup' ? 'active' : ''} onClick={() => setMode('signup')}>Sign up</button>
        </div>

        <MessageBox message={message} />

        {mode === 'signin' ? (
          <form className="auth-form" onSubmit={submitSignin}>
            <label>
              <span>Username or email</span>
              <input type="text" value={signin.identifier} onChange={(e) => setSignin({ ...signin, identifier: e.target.value })} required />
            </label>

            <label>
              <span>Password</span>
              <input type="password" value={signin.password} onChange={(e) => setSignin({ ...signin, password: e.target.value })} required />
            </label>

            <button type="submit" className="primary-btn">Login</button>
            <div className="inline-link-row">
              <span>Admin login:</span>
              <button type="button" className="link-btn" onClick={() => navigate('/admin')}>Go to admin</button>
            </div>
          </form>
        ) : (
          <form className="auth-form" onSubmit={submitSignup}>
            <label>
              <span>Full name</span>
              <input type="text" value={signup.name} onChange={(e) => setSignup({ ...signup, name: e.target.value })} required />
            </label>

            <label>
              <span>Username</span>
              <input type="text" value={signup.username} onChange={(e) => setSignup({ ...signup, username: e.target.value })} required />
            </label>

            <label>
              <span>Email</span>
              <input type="email" value={signup.email} onChange={(e) => setSignup({ ...signup, email: e.target.value })} required />
            </label>

            <label>
              <span>Password</span>
              <input type="password" value={signup.password} onChange={(e) => setSignup({ ...signup, password: e.target.value })} required />
            </label>

            <button type="submit" className="primary-btn">Create account</button>
          </form>
        )}
      </div>
    </div>
  );
}

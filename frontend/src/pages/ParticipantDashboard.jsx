import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import EventCard from '../components/EventCard';
import MessageBox from '../components/MessageBox';
import { API_URL } from '../lib/api';
import { clearSession, readSession } from '../lib/session';

export default function ParticipantDashboard() {
  const navigate = useNavigate();
  const [events, setEvents] = useState([]);
  const [message, setMessage] = useState('');
  const user = readSession();

  useEffect(() => {
    const currentUser = readSession();
    if (!currentUser || currentUser.role === 'ADMIN') {
      navigate('/');
      return;
    }

    const fetchData = async () => {
      try {
        const response = await fetch(`${API_URL}/events?userId=${currentUser.id}`);
        const data = await response.json();
        if (!response.ok) {
          throw new Error(data.message || 'Unable to load events.');
        }
        setEvents(data);
      } catch (error) {
        setMessage(error.message);
      }
    };

    fetchData();
  }, [navigate]);

  const handleRegister = async (eventId) => {
    try {
      const response = await fetch(`${API_URL}/events/${eventId}/register`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ studentId: user.id })
      });

      const data = await response.json();
      if (!response.ok) {
        throw new Error(data.message || 'Registration failed.');
      }

      setMessage('You are now registered for this event.');
      const refreshed = await fetch(`${API_URL}/events?userId=${user.id}`);
      setEvents(await refreshed.json());
    } catch (error) {
      setMessage(error.message);
    }
  };

  if (!user || user.role === 'ADMIN') {
    return null;
  }

  return (
    <div className="app-shell">
      <header className="topbar dashboard-header">
        <div>
          <p className="eyebrow">Participant portal</p>
          <h1>Available events</h1>
        </div>
        <div className="user-badge">
          <span>{user.role}</span>
          <strong>{user.name}</strong>
          <button type="button" className="secondary-btn" onClick={() => { clearSession(); navigate('/'); }}>Logout</button>
        </div>
      </header>

      <MessageBox message={message} />

      <div className="summary-row">
        <div className="summary-tile">
          <span className="summary-label">Upcoming</span>
          <strong>{events.length}</strong>
        </div>
        <div className="summary-tile">
          <span className="summary-label">Attending</span>
          <strong>{events.reduce((sum, event) => sum + (Number(event.registeredCount) || 0), 0)}</strong>
        </div>
      </div>

      <main className="dashboard-grid single-grid">
        {events.map((event) => (
          <EventCard
            key={event.id}
            event={event}
            mode="participant"
            isUserRegistered={Boolean(event.userRegistered)}
            onAttend={() => handleRegister(event.id)}
          />
        ))}
      </main>
    </div>
  );
}

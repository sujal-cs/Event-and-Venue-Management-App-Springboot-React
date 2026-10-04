import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import EventCard from '../components/EventCard';
import MessageBox from '../components/MessageBox';
import { API_URL } from '../lib/api';
import { clearSession, readSession } from '../lib/session';

export default function AdminDashboard() {
  const navigate = useNavigate();
  const [events, setEvents] = useState([]);
  const [venues, setVenues] = useState([]);
  const [message, setMessage] = useState('');
  const [editingEventId, setEditingEventId] = useState(null);
  const [selectedEvent, setSelectedEvent] = useState(null);
  const [eventForm, setEventForm] = useState({ title: '', description: '', venueId: '', startDateTime: '', endDateTime: '' });
  const [venueForm, setVenueForm] = useState({ name: '', location: '', capacity: '' });
  const user = readSession();

  const formatDateTimeLocal = (value) => {
    if (!value) return '';
    const date = new Date(value);
    if (Number.isNaN(date.getTime())) return '';
    const pad = (num) => String(num).padStart(2, '0');
    return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}T${pad(date.getHours())}:${pad(date.getMinutes())}`;
  };

  const refreshEvents = async () => {
    const response = await fetch(`${API_URL}/events`);
    setEvents(await response.json());
  };

  const refreshVenues = async () => {
    const response = await fetch(`${API_URL}/venues`);
    const venueData = await response.json();
    setVenues(venueData);
    setEventForm((prev) => {
      if (venueData.length === 0) {
        return { ...prev, venueId: '' };
      }

      const hasCurrentVenue = venueData.some((venue) => String(venue.id) === String(prev.venueId));
      return { ...prev, venueId: hasCurrentVenue ? prev.venueId : String(venueData[0].id) };
    });
  };

  useEffect(() => {
    const currentUser = readSession();
    if (!currentUser || currentUser.role !== 'ADMIN') {
      navigate('/admin');
      return;
    }

    const fetchData = async () => {
      try {
        const [eventsRes, venuesRes] = await Promise.all([
          fetch(`${API_URL}/events`),
          fetch(`${API_URL}/venues`)
        ]);

        const eventData = await eventsRes.json();
        const venueData = await venuesRes.json();
        setEvents(eventData);
        setVenues(venueData);

        setEventForm((prev) => {
          if (venueData.length > 0 && !prev.venueId) {
            return { ...prev, venueId: String(venueData[0].id) };
          }
          return prev;
        });
      } catch (error) {
        setMessage('Unable to load venue and event data.');
      }
    };

    fetchData();
  }, [navigate]);

  const handleVenueSubmit = async (event) => {
    event.preventDefault();
    setMessage('');

    try {
      const response = await fetch(`${API_URL}/venues?adminId=${user.id}`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          name: venueForm.name,
          location: venueForm.location,
          capacity: Number(venueForm.capacity)
        })
      });

      const data = await response.json();
      if (!response.ok) {
        throw new Error(data.message || 'Venue creation failed.');
      }

      setVenueForm({ name: '', location: '', capacity: '' });
      await refreshVenues();
      setMessage('Venue created successfully.');
    } catch (error) {
      setMessage(error.message);
    }
  };

  const handleVenueDelete = async (venueId) => {
    if (!window.confirm('Delete this venue? It cannot be removed if it is connected to existing events.')) {
      return;
    }

    try {
      const response = await fetch(`${API_URL}/venues/${venueId}?adminId=${user.id}`, {
        method: 'DELETE'
      });

      if (!response.ok) {
        const data = await response.json().catch(() => ({}));
        throw new Error(data.message || 'Venue deletion failed.');
      }

      await refreshVenues();
      await refreshEvents();
      setMessage('Venue deleted successfully.');
    } catch (error) {
      setMessage(error.message);
    }
  };

  const handleEventSubmit = async (event) => {
    event.preventDefault();
    setMessage('');

    try {
      const payload = {
        title: eventForm.title,
        description: eventForm.description,
        venueId: Number(eventForm.venueId),
        startDateTime: eventForm.startDateTime,
        endDateTime: eventForm.endDateTime
      };

      const endpoint = editingEventId
        ? `${API_URL}/events/${editingEventId}?adminId=${user.id}`
        : `${API_URL}/events?adminId=${user.id}`;

      const method = editingEventId ? 'PUT' : 'POST';
      const response = await fetch(endpoint, {
        method,
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
      });

      const data = await response.json();
      if (!response.ok) {
        throw new Error(data.message || (editingEventId ? 'Event update failed.' : 'Event creation failed.'));
      }

      await refreshEvents();
      setEventForm({ title: '', description: '', venueId: eventForm.venueId || (venues[0]?.id ? String(venues[0].id) : ''), startDateTime: '', endDateTime: '' });
      setEditingEventId(null);
      setMessage(editingEventId ? 'Event updated successfully.' : 'Event created successfully.');
    } catch (error) {
      setMessage(error.message);
    }
  };

  const handleViewEvent = (event) => {
    setSelectedEvent(event);

    const attendeeNames = Array.isArray(event.attendeeNames) && event.attendeeNames.length > 0
      ? event.attendeeNames.join(', ')
      : 'No users registered yet.';

    setMessage(`Event: ${event.title} • ${event.venueName} • ${new Date(event.startDateTime).toLocaleString()} - ${new Date(event.endDateTime).toLocaleString()} • Attendees: ${attendeeNames}`);
  };

  const handleEditEvent = (event) => {
    setEditingEventId(event.id);
    setEventForm({
      title: event.title,
      description: event.description || '',
      venueId: String(event.venueId),
      startDateTime: formatDateTimeLocal(event.startDateTime),
      endDateTime: formatDateTimeLocal(event.endDateTime)
    });
    setMessage(`Editing event: ${event.title}`);
    window.scrollTo({ top: 0, behavior: 'smooth' });
  };

  const handleDeleteEvent = async (eventId) => {
    if (!window.confirm('Delete this event?')) {
      return;
    }

    try {
      const response = await fetch(`${API_URL}/events/${eventId}?adminId=${user.id}`, {
        method: 'DELETE'
      });

      if (!response.ok) {
        const data = await response.json().catch(() => ({}));
        throw new Error(data.message || 'Event deletion failed.');
      }

      await refreshEvents();
      if (editingEventId === eventId) {
        setEditingEventId(null);
        setEventForm({ title: '', description: '', venueId: venues[0] ? String(venues[0].id) : '', startDateTime: '', endDateTime: '' });
      }
      if (selectedEvent && selectedEvent.id === eventId) {
        setSelectedEvent(null);
      }
      setMessage('Event deleted successfully.');
    } catch (error) {
      setMessage(error.message);
    }
  };

  if (!user || user.role !== 'ADMIN') {
    return null;
  }

  return (
    <div className="app-shell">
      <header className="topbar dashboard-header">
        <div>
          <p className="eyebrow">Admin workspace</p>
          <h1>Venue and event management</h1>
        </div>
        <div className="user-badge">
          <span>{user.role}</span>
          <strong>{user.name}</strong>
          <button type="button" className="secondary-btn" onClick={() => { clearSession(); navigate('/admin'); }}>Logout</button>
        </div>
      </header>

      <MessageBox message={message} />

      <div className="summary-row">
        <div className="summary-tile">
          <span className="summary-label">Venues</span>
          <strong>{venues.length}</strong>
        </div>
        <div className="summary-tile">
          <span className="summary-label">Events</span>
          <strong>{events.length}</strong>
        </div>
        <div className="summary-tile">
          <span className="summary-label">Attendees</span>
          <strong>{events.reduce((sum, event) => sum + (Number(event.registeredCount) || 0), 0)}</strong>
        </div>
      </div>

      <main className="dashboard-grid admin-grid">
        <section className="panel form-panel">
          <div className="panel-heading-row">
            <div>
              <p className="section-label">Venue management</p>
              <h2>Register venue</h2>
            </div>
          </div>
          <form onSubmit={handleVenueSubmit}>
            <label>
              <span>Venue name</span>
              <input type="text" name="name" value={venueForm.name} onChange={(e) => setVenueForm({ ...venueForm, name: e.target.value })} required />
            </label>

            <label>
              <span>Location</span>
              <input type="text" name="location" value={venueForm.location} onChange={(e) => setVenueForm({ ...venueForm, location: e.target.value })} required />
            </label>

            <label>
              <span>Capacity</span>
              <input type="number" min="1" value={venueForm.capacity} onChange={(e) => setVenueForm({ ...venueForm, capacity: e.target.value })} required />
            </label>

            <button type="submit" className="primary-btn">Save venue</button>
          </form>

          <div className="venue-list">
            <div className="mini-header">
              <span>Saved venues</span>
              <strong>{venues.length}</strong>
            </div>

            {venues.length === 0 ? (
              <p className="muted-text">No venues added yet.</p>
            ) : (
              venues.map((venue) => (
                <div key={venue.id} className="venue-row">
                  <div>
                    <strong>{venue.name}</strong>
                    <span>{venue.location}</span>
                    <small>Capacity {venue.capacity}</small>
                  </div>
                  <button type="button" className="action-btn danger" onClick={() => handleVenueDelete(venue.id)}>
                    Delete
                  </button>
                </div>
              ))
            )}
          </div>
        </section>

        <section className="panel form-panel">
          <div className="panel-heading-row">
            <div>
              <p className="section-label">Event management</p>
              <h2>{editingEventId ? 'Edit event' : 'Add event'}</h2>
            </div>
          </div>
          <form onSubmit={handleEventSubmit}>
            <label>
              <span>Event title</span>
              <input type="text" value={eventForm.title} onChange={(e) => setEventForm({ ...eventForm, title: e.target.value })} required />
            </label>

            <label>
              <span>Description</span>
              <textarea rows="3" value={eventForm.description} onChange={(e) => setEventForm({ ...eventForm, description: e.target.value })} />
            </label>

            <label>
              <span>Venue</span>
              <select value={eventForm.venueId} onChange={(e) => setEventForm({ ...eventForm, venueId: e.target.value })} required>
                {venues.map((venue) => (
                  <option key={venue.id} value={venue.id}>{venue.name} - {venue.location}</option>
                ))}
              </select>
            </label>

            <div className="two-col">
              <label>
                <span>Start time</span>
                <input type="datetime-local" value={eventForm.startDateTime} onChange={(e) => setEventForm({ ...eventForm, startDateTime: e.target.value })} required />
              </label>

              <label>
                <span>End time</span>
                <input type="datetime-local" value={eventForm.endDateTime} onChange={(e) => setEventForm({ ...eventForm, endDateTime: e.target.value })} required />
              </label>
            </div>

            <button type="submit" className="primary-btn">{editingEventId ? 'Update event' : 'Create event'}</button>
            {editingEventId && (
              <button type="button" className="secondary-btn" onClick={() => {
                setEditingEventId(null);
                setEventForm({ title: '', description: '', venueId: venues[0] ? String(venues[0].id) : '', startDateTime: '', endDateTime: '' });
                setMessage('Edit cancelled.');
              }}>Cancel edit</button>
            )}
          </form>
        </section>
      </main>

      {selectedEvent && (
        <section className="panel detail-panel">
          <div className="detail-header">
            <div>
              <p className="section-label">Event details</p>
              <h2>{selectedEvent.title}</h2>
            </div>
            <span className="chip accent-chip">{selectedEvent.registeredCount || 0} attending</span>
          </div>

          <div className="detail-box">
            <div className="detail-meta-grid">
              <div>
                <span className="meta-label">Venue</span>
                <strong>{selectedEvent.venueName}</strong>
              </div>
              <div>
                <span className="meta-label">Schedule</span>
                <strong>{new Date(selectedEvent.startDateTime).toLocaleString()} - {new Date(selectedEvent.endDateTime).toLocaleString()}</strong>
              </div>
            </div>

            <div className="detail-description">
              <span className="meta-label">Description</span>
              <p>{selectedEvent.description || 'No description provided.'}</p>
            </div>

            <div className="attendee-list-wrap">
              <div className="attendee-header">
                <span className="meta-label">Attending users</span>
                <span className="meta-count">{selectedEvent.attendeeNames?.length || 0}</span>
              </div>

              {Array.isArray(selectedEvent.attendeeNames) && selectedEvent.attendeeNames.length > 0 ? (
                <div className="attendee-list">
                  {selectedEvent.attendeeNames.map((name, index) => (
                    <span key={`${name}-${index}`} className="attendee-pill">{name}</span>
                  ))}
                </div>
              ) : (
                <p className="muted-text">No users have registered for this event yet.</p>
              )}
            </div>
          </div>
        </section>
      )}

      <section className="panel list-panel event-list-panel">
        <div className="panel-heading-row list-heading">
          <div>
            <p className="section-label">Schedule</p>
            <h2>Upcoming events</h2>
          </div>
        </div>

        <div className="event-list">
          {events.length === 0 ? (
            <div className="empty-state">No events scheduled yet. Create your first event from the form above.</div>
          ) : (
            events.map((event) => (
              <EventCard
                key={event.id}
                event={event}
                mode="admin"
                onView={() => handleViewEvent(event)}
                onEdit={() => handleEditEvent(event)}
                onDelete={() => handleDeleteEvent(event.id)}
              />
            ))
          )}
        </div>
      </section>
    </div>
  );
}

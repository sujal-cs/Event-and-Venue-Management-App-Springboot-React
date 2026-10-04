export default function EventCard({ event, mode = 'participant', onView, onEdit, onDelete, onAttend, isUserRegistered = false }) {
  const actionButtons =
    mode === 'admin' ? (
      <div className="inline-actions">
        <button type="button" className="action-btn ghost" onClick={onView}>View</button>
        <button type="button" className="action-btn ghost" onClick={onEdit}>Edit</button>
        <button type="button" className="action-btn danger" onClick={onDelete}>Delete</button>
      </div>
    ) : (
      <button
        type="button"
        className={isUserRegistered ? 'action-btn is-active' : 'action-btn'}
        onClick={onAttend}
        disabled={isUserRegistered}
      >
        {isUserRegistered ? 'Attending' : 'Attend'}
      </button>
    );

  return (
    <article className="event-card admin-event-card">
      <div className="event-header">
        <div>
          <p className="event-kicker">{event.venueName}</p>
          <h3>{event.title}</h3>
        </div>
        <span className="chip">{event.registeredCount} going</span>
      </div>

      <p className="event-description">{event.description || 'No description available.'}</p>

      <div className="event-meta">
        <span>
          {new Date(event.startDateTime).toLocaleString()} - {new Date(event.endDateTime).toLocaleString()}
        </span>
      </div>

      <div className="card-footer row-actions">
        <span>{event.registeredCount} attending</span>
        {actionButtons}
      </div>
    </article>
  );
}

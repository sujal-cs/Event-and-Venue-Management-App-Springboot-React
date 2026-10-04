export default function MessageBox({ message }) {
  if (!message) return null;
  return <div className="message-box">{message}</div>;
}

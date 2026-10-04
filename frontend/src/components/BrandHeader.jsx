export default function BrandHeader({ eyebrow, title, variant = 'default' }) {
  const mark = variant === 'admin' ? 'A' : 'E';

  return (
    <div className="brand-mark-row compact-brand">
      <div className="brand-mark">{mark}</div>
      <div>
        <p className="eyebrow">{eyebrow}</p>
        <h2>{title}</h2>
      </div>
    </div>
  );
}

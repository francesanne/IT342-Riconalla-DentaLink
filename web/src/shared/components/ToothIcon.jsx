export default function ToothIcon({ size = 24, color, className, style }) {
  return (
    <svg
      xmlns="http://www.w3.org/2000/svg"
      width={size}
      height={size}
      viewBox="0 0 24 24"
      fill="currentColor"
      aria-hidden="true"
      className={className}
      style={color ? { color, ...style } : style}
    >
      <path d="M12 2.5C10.2 2.1 7.8 2.4 5.8 3.8 4.1 5.0 3 6.6 3 8.2 3 10.8 4.4 12.3 5.4 13.7 6.0 14.7 6.6 17.1 7.1 19.0 7.6 20.9 8.5 22 10 22c1 0 1.5-1 2-2.5.5 1.5 1 2.5 2 2.5 1.5 0 2.4-1.1 2.9-3 .5-1.9 1.1-4.3 1.7-5.3C19.6 12.3 21 10.8 21 8.2 21 6.6 19.9 5.0 18.2 3.8 16.2 2.4 13.8 2.1 12 2.5z" />
    </svg>
  );
}

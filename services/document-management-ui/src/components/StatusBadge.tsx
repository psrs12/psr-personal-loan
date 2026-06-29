interface Props { status: string }

const STATUS_MAP: Record<string, { label: string; cls: string }> = {
  PENDING:   { label: 'Pending',   cls: 'bg-gray-100 text-gray-600' },
  UPLOADED:  { label: 'Uploaded',  cls: 'bg-yellow-100 text-yellow-700' },
  SCANNING:  { label: 'Scanning',  cls: 'bg-blue-100 text-blue-700' },
  VERIFIED:  { label: 'Verified',  cls: 'bg-teal-100 text-teal-700' },
  REJECTED:  { label: 'Rejected',  cls: 'bg-red-100 text-red-700' },
  COMPLETED: { label: 'Complete',  cls: 'bg-green-100 text-green-700' },
}

export default function StatusBadge({ status }: Props) {
  const { label, cls } = STATUS_MAP[status] ?? { label: status, cls: 'bg-gray-100 text-gray-600' }
  return <span className={`text-xs font-medium px-2 py-0.5 rounded-full ${cls}`}>{label}</span>
}

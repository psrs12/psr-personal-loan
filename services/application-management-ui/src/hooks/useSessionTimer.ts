import { useState, useEffect } from 'react'

export function useSessionTimer(expiresAt: string) {
  const [remaining, setRemaining] = useState('')
  const [isExpired, setIsExpired] = useState(false)

  useEffect(() => {
    const tick = () => {
      const diff = new Date(expiresAt).getTime() - Date.now()
      if (diff <= 0) {
        setIsExpired(true)
        setRemaining('00:00')
        return
      }
      const m = Math.floor(diff / 60000)
      const s = Math.floor((diff % 60000) / 1000)
      setRemaining(`${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`)
    }
    tick()
    const id = setInterval(tick, 1000)
    return () => clearInterval(id)
  }, [expiresAt])

  return { remaining, isExpired }
}

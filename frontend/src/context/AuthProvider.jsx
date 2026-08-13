import { useCallback, useMemo, useState } from 'react'
import { TOKEN_KEY } from '../api/client'
import { AuthContext } from './auth-context'

const USER_ID_KEY = 'cms.userId'

export function AuthProvider({ children }) {
  // Read straight from localStorage on first render so a page refresh
  // doesn't log the user back out.
  const [token, setToken] = useState(() => localStorage.getItem(TOKEN_KEY))
  const [userId, setUserId] = useState(() => localStorage.getItem(USER_ID_KEY))

  const signIn = useCallback((auth) => {
    localStorage.setItem(TOKEN_KEY, auth.token)
    localStorage.setItem(USER_ID_KEY, auth.userId)
    setToken(auth.token)
    setUserId(String(auth.userId))
  }, [])

  const signOut = useCallback(() => {
    localStorage.removeItem(TOKEN_KEY)
    localStorage.removeItem(USER_ID_KEY)
    setToken(null)
    setUserId(null)
  }, [])

  const value = useMemo(
    () => ({ token, userId, signIn, signOut, isAuthenticated: Boolean(token) }),
    [token, userId, signIn, signOut],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

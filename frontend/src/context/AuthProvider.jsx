import { useCallback, useMemo, useState } from 'react'
import { clearStored, readStored, TOKEN_KEY, USER_ID_KEY, writeStored } from '../lib/storage'
import { AuthContext } from './auth-context'

export function AuthProvider({ children }) {
  // Read straight from storage on first render so a page refresh
  // doesn't log the user back out.
  const [token, setToken] = useState(() => readStored(TOKEN_KEY))
  const [userId, setUserId] = useState(() => readStored(USER_ID_KEY))

  const signIn = useCallback((auth) => {
    writeStored(TOKEN_KEY, auth.token)
    writeStored(USER_ID_KEY, String(auth.userId))
    setToken(auth.token)
    setUserId(String(auth.userId))
  }, [])

  const signOut = useCallback(() => {
    clearStored(TOKEN_KEY)
    clearStored(USER_ID_KEY)
    setToken(null)
    setUserId(null)
  }, [])

  const value = useMemo(
    () => ({ token, userId, signIn, signOut, isAuthenticated: Boolean(token) }),
    [token, userId, signIn, signOut],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

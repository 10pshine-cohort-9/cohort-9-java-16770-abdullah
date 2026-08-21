export const TOKEN_KEY = 'cms.token'
export const USER_ID_KEY = 'cms.userId'

// Browsers can block storage entirely (private mode, blocked cookies), and
// then every localStorage call throws. Fall back to not persisting rather
// than letting that take the whole app down - the session still works for
// the current tab, it just won't survive a refresh.

export function readStored(key) {
  try {
    return localStorage.getItem(key)
  } catch {
    return null
  }
}

export function writeStored(key, value) {
  try {
    localStorage.setItem(key, value)
  } catch {
    // Nothing we can do; keep going with in-memory state only.
  }
}

export function clearStored(key) {
  try {
    localStorage.removeItem(key)
  } catch {
    // Nothing was persisted, so there's nothing to clear.
  }
}

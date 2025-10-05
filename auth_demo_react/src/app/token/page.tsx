'use client'

import { useAuth } from '@clerk/nextjs'
import { useState, useEffect } from 'react'

export default function TokenPage() {
  const { getToken, isSignedIn } = useAuth()
  const [token, setToken] = useState<string | null>(null)
  const [loading, setLoading] = useState(false)

  const fetchToken = async () => {
    if (!isSignedIn) return
    
    setLoading(true)
    try {
      // Request token using the 'brunotesting' template for longer valididty
      const jwt = await getToken({
        template: 'brunotesting'
      })
      setToken(jwt)
    } catch (error) {
      console.error('Error fetching token:', error)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    if (isSignedIn) {
      fetchToken()
    }
  }, [isSignedIn])

  if (!isSignedIn) {
    return (
      <div className="container mx-auto p-8">
        <h1 className="text-2xl font-bold mb-4">JWT Token for Bruno Testing</h1>
        <p>Please sign in to generate a JWT token for API testing.</p>
      </div>
    )
  }

  return (
    <div className="container mx-auto p-8">
      <h1 className="text-2xl font-bold mb-4">JWT Token for Bruno Testing</h1>
      
      <div className="mb-4">
        <button
          onClick={fetchToken}
          disabled={loading}
          className="bg-blue-500 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded disabled:opacity-50"
        >
          {loading ? 'Loading...' : 'Refresh Token'}
        </button>
      </div>

      {token && (
        <div className="space-y-4">
          <div>
            <h2 className="text-lg font-semibold mb-2">JWT Token:</h2>
            <div className="bg-gray-100 p-4 rounded-lg break-all font-mono text-sm">
              {token}
            </div>
          </div>
          
          <div>
            <h3 className="text-md font-semibold mb-2">For Bruno:</h3>
            <p className="text-sm text-gray-600 mb-2">
              Copy the token above and use it in your Bruno requests as:
            </p>
            <div className="bg-gray-100 p-2 rounded font-mono text-sm">
              Authorization: Bearer {token}
            </div>
          </div>

          <div className="mt-4">
            <button
              onClick={() => navigator.clipboard.writeText(token)}
              className="bg-green-500 hover:bg-green-700 text-white font-bold py-2 px-4 rounded"
            >
              Copy Token to Clipboard
            </button>
          </div>
        </div>
      )}
    </div>
  )
}

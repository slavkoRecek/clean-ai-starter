import Link from "next/link";

export default function Home() {
  return (
    <div className="container mx-auto p-8">
      <main className="max-w-2xl mx-auto">
        <h1 className="text-3xl font-bold mb-6">Clerk JWT Test App</h1>
        
        <div className="space-y-4">
          <p className="text-gray-600">
            This app helps you generate JWT tokens from Clerk for testing your backend API with Bruno and test WebSocket entity change notifications.
          </p>

          <div className="bg-blue-50 border border-blue-200 rounded-lg p-4">
            <h2 className="font-semibold text-blue-900 mb-2">Available Features:</h2>
            <ol className="list-decimal list-inside space-y-1 text-blue-800 text-sm">
              <li>Sign in using the buttons in the header</li>
              <li>Generate JWT tokens for Bruno API testing</li>
              <li>Test WebSocket entity change messages in real-time</li>
              <li>Acknowledge entity change notifications</li>
            </ol>
          </div>

          <div className="mt-8 grid grid-cols-1 md:grid-cols-2 gap-4">
            <Link
              href="/token"
              className="bg-blue-500 hover:bg-blue-600 text-white font-semibold py-3 px-6 rounded-lg inline-block text-center transition-colors"
            >
              Generate JWT Token →
            </Link>
            <Link
              href="/messages"
              className="bg-green-500 hover:bg-green-600 text-white font-semibold py-3 px-6 rounded-lg inline-block text-center transition-colors"
            >
              View Entity Messages →
            </Link>
          </div>
        </div>
      </main>
    </div>
  );
}

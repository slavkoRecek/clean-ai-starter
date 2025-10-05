# Clerk JWT Test App Setup

This Next.js app generates JWT tokens from Clerk for testing your backend API with Bruno.

## Setup Instructions

### 1. Get Clerk API Keys
1. Go to [Clerk Dashboard](https://dashboard.clerk.com/)
2. Create a new application or use existing one
3. Go to API Keys section
4. Copy your Publishable Key and Secret Key

### 2. Configure Environment Variables
Edit `.env.local` and replace the placeholder values:

```env
NEXT_PUBLIC_CLERK_PUBLISHABLE_KEY=pk_test_your_actual_publishable_key_here
CLERK_SECRET_KEY=sk_test_your_actual_secret_key_here
```

### 3. Run the Application
```bash
npm run dev
```

The app will be available at `http://localhost:3000`

## How to Use

1. **Sign Up/Sign In**: Use the buttons in the header to create an account or sign in
2. **Generate Token**: Go to `/token` page to generate your JWT token
3. **Copy for Bruno**: Copy the generated token and use it in Bruno requests as:
   ```
   Authorization: Bearer your_jwt_token_here
   ```

## Features

- ✅ Clerk authentication with App Router
- ✅ JWT token generation
- ✅ Copy-to-clipboard functionality
- ✅ Token refresh capability
- ✅ Clean UI for easy token extraction

## Architecture

- Uses `clerkMiddleware()` for authentication middleware
- `<ClerkProvider>` wraps the entire app in `layout.tsx`
- Token page uses `useAuth()` hook to get JWT tokens
- Follows latest Clerk Next.js App Router patterns
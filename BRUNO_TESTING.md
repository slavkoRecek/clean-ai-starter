# Bruno API Testing

## Installation

```bash
brew install bruno
```

## Import Collection

1. Open Bruno
2. Click "Open Collection"
3. Select the `bruno/clean-ai-starter/` folder in this repository
4. The collection will load with all existing API requests

## Generate OIDC Token

### Setup Clerk Auth Demo App

```bash
cd clerk_auth_demo
npm install
npm run dev
```

The app runs at `http://localhost:3000`

### Get Token for Testing

1. Open `http://localhost:3000`
2. Sign up or sign in using the Clerk authentication
3. Navigate to `http://localhost:3000/token`
4. Click "Copy Token to Clipboard"
5. The token is automatically generated using the 'brunotesting' template for extended validity

### Use Token in Bruno

1. In Bruno, select the appropriate environment:
   - **local**: For testing against local development server (http://localhost:8080)
   - **fly-main**: For testing against deployed Fly.io instance (https://logbook-backend.fly.dev)
2. Update the `oidcToken` variable with your copied token
3. All requests will automatically use this token in the Authorization header

## Testing Workflow

1. Start the database: `docker compose up -d`
2. Start the backend with local profile: `./gradlew quarkusDev -Dquarkus.profile=local`
3. Generate fresh token from clerk_auth_demo
4. Update Bruno environment with new token
5. Execute API requests from the collection

The collection includes requests for Profile, Notes, and Files modules.

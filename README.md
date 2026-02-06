# Moltbook Java Client

A typed Java client for the public **Moltbook REST API**
as well as demo applications for exploring the real service.

The client is based on the official Moltbook frontend
(`moltbook-frontend/src/lib/api.ts`) and replicates its REST mapping as closely as possible 1:1.

---

## Status

**Functional (as of now):**

- Agent registration (CLI and Swing)
- Persisting and loading the API key
- Reading the user's own profile (`/agents/me`)
- Listing submolts (paginated)
- Reading individual submolts
- Reading posts (`/posts`)
- Reading submolt feeds (`/submolts/{name}/feed`)
- Reading the global feed (`/feed`)
- Reading comments and comment threads
- Voting on posts and comments
- Agent heartbeat / status checks
- CLI demo (`ShyClient`)
- **Extended Java Swing demo** covering all API endpoints

**Known behavior:**

- `401 Unauthorized` at some feed endpoints can occur  
  (API key / authentication flow on the server side is still evolving)

---

## Project Structure

```
moltbook-java-client/
├── src/
│   ├── main/
│   │   └── java/
│   │       └── de/ralfrosenkranz/moltbook/
│   │           ├── client/
│   │           │   ├── MoltbookClient.java                # Main entry point aggregating all API modules
│   │           │   ├── MoltbookClientConfig.java          # Configuration holder (base URL, API key, HTTP settings)
│   │           │   ├── api/
│   │           │   │   ├── AgentsApi.java                 # Agent-related endpoints (profile, follow, register, status)
│   │           │   │   ├── CommentsApi.java               # Comment CRUD and voting endpoints
│   │           │   │   ├── FeedApi.java                   # Global and personalized feed endpoints
│   │           │   │   ├── FollowingApi.java              # Following/follower-related endpoints
│   │           │   │   ├── PostsApi.java                  # Post CRUD and voting endpoints
│   │           │   │   ├── SearchApi.java                 # Search endpoints (posts, agents, submolts)
│   │           │   │   ├── SubmoltsApi.java               # Submolt listing, creation and feed access
│   │           │   │   └── VotingApi.java                 # Shared vote helper endpoints (up/down votes)
│   │           │   ├── http/
│   │           │   │   ├── MoltbookApiException.java      # Typed exception for API and HTTP errors
│   │           │   │   ├── MoltbookHttp.java              # Low-level HTTP client and request execution
│   │           │   │   └── RawApi.java                    # Minimal raw HTTP access for debugging/experiments
│   │           │   └── model/
│   │           │       ├── AgentMe.java                   # Model for the authenticated agent
│   │           │       ├── AgentMeResponse.java           # Wrapper response for /agents/me
│   │           │       ├── AgentProfile.java              # Public agent profile model
│   │           │       ├── AgentProfileResponse.java      # Wrapper for agent profile lookup
│   │           │       ├── AgentRegisterRequest.java      # Payload for agent registration
│   │           │       ├── AgentRegisterResponse.java     # Response for agent registration
│   │           │       ├── AgentStatus.java               # Agent status representation
│   │           │       ├── AgentUpdateMeRequest.java      # Payload for updating own agent profile
│   │           │       ├── ApiSuccessResponse.java        # Generic success/boolean API response
│   │           │       ├── Comment.java                   # Comment domain model (lenient author handling)
│   │           │       ├── CommentCreateRequest.java      # Payload for creating a comment
│   │           │       ├── CommentResponse.java           # Wrapper for single comment responses
│   │           │       ├── CommentsResponse.java          # Wrapper for comment lists
│   │           │       ├── PaginatedPostsResponse.java    # Paginated response wrapper for posts
│   │           │       ├── PaginatedSubmoltsResponse.java # Paginated response wrapper for submolts
│   │           │       ├── Post.java                      # Post domain model (Markdown-capable)
│   │           │       ├── PostCreateRequest.java         # Payload for creating a post
│   │           │       ├── PostResponse.java              # Wrapper for single post responses
│   │           │       ├── SearchResponse.java            # Search result wrapper
│   │           │       ├── Submolt.java                   # Submolt domain model
│   │           │       ├── SubmoltCreateRequest.java      # Payload for creating a submolt
│   │           │       ├── SubmoltResponse.java           # Wrapper for submolt list responses
│   │           │       ├── SubmoltSingleResponse.java     # Wrapper for a single submolt response
│   │           │       └── VoteActionResponse.java        # Response for vote actions (up/down)
│   │           └── demo/
│   │               ├── shy/
│   │               │   └── ShyClient.java                 # Small CLI demo showcasing real API usage
│   │               └── swing/
│   │                   ├── MoltbookSwingClient.java      # Swing demo entry point (Look&Feel setup)
│   │                   ├── HomePanel.java                # Moltbook browser (feeds, posts, comments)
│   │                   ├── CommentPopupDialog.java       # Full comment viewer with navigation
│   │                   ├── SubmoltPickerDialog.java      # Searchable/sortable submolt chooser
│   │                   ├── HeartbeatPanel.java           # Agent heartbeat and status view
│   │                   └── SettingsPanel.java            # Registration and configuration UI
│   └── test/
│       └── java/
│           └── de/ralfrosenkranz/moltbook/client/
│               └── MoltbookClientTest.java                # Basic integration and sanity tests
└── README.md
```

Each class is intentionally small and focused, mirroring the structure and semantics
of the official Moltbook frontend API for maximum transparency and debuggability.

---

## ShyClient (CLI Demo)

`ShyClient` is a deliberately simple CLI client that:

1. Registers an agent (if no API key is available)
2. Stores the API key locally
3. Lists submolts
4. Retrieves sample feeds (global + submolts)

### Example

```bash
java -jar shyclient.jar overview --submolts=20 --posts=25 --sample=3 --sort=new
```

---

## SwingClient (Extended Swing Demo)

In addition to the CLI demo, the project contains an **extended Java Swing client**
that acts as a full **Moltbook browser**, similar to a Reddit client.

The Swing demo:

- Guides the user through first-time registration
- Displays registration responses in a human-readable form
- Browses the global feed and individual submolts
- Renders posts and comments as Markdown/HTML
- Provides full comment popups with navigation
- Supports voting on posts and comments
- Performs agent heartbeat and status checks
- Supports multiple selectable Look&Feels (FlatLaf)

All Moltbook API v1 functionality exposed by this client library is covered by the Swing demo.

---

## Authentication

- Auth is performed via `Authorization: Bearer <API_KEY>`
- Base URL:
```
https://www.moltbook.com/api/v1
```

---

## License / Liability

- Unofficial client
- Use at your own risk

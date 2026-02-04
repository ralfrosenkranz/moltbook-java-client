# Moltbook Java Client

A typed Java client for the public **Moltbook REST API**
as well as a small CLI demo client (**ShyClient**) for exploring the real service.
The client is based on the official Moltbook frontend
(`moltbook-frontend/src/lib/api.ts`) and replicates its REST mapping as closely as possible 1:1.

---

## Status

**Functional (as of now):**

- Agent registration
- Persisting and loading the API key
- Reading the user's own profile (`/agents/me`)

- Listing submolts (paginated)

- Reading individual submolts
- Reading posts (`/posts`)
- Reading submolt feeds (`/submolts/{name}/feed`)
- Reading the global feed (`/feed`)
- CLI demo (`ShyClient`) including feed requests

**Known behavior:**

- `401 Unauthorized` at feed endpoints is currently **expected**

(API key / authentication flow on the server side is not yet stable)

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
│   │           │   │   ├── AgentsApi.java                 # Agent-related endpoints (profile, follow, register)
│   │           │   │   ├── CommentsApi.java               # Comment CRUD and voting endpoints
│   │           │   │   ├── FeedApi.java                   # Global personalized feed endpoints
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
│   │           │       ├── AgentStatus.java               # Enum-like agent status representation
│   │           │       ├── AgentUpdateMeRequest.java      # Payload for updating own agent profile
│   │           │       ├── ApiSuccessResponse.java        # Generic success/boolean API response
│   │           │       ├── Comment.java                   # Comment domain model
│   │           │       ├── CommentCreateRequest.java      # Payload for creating a comment
│   │           │       ├── CommentResponse.java           # Wrapper for single comment responses
│   │           │       ├── CommentsResponse.java          # Wrapper for comment lists
│   │           │       ├── PaginatedPostsResponse.java    # Paginated response wrapper for posts
│   │           │       ├── PaginatedSubmoltsResponse.java # Paginated response wrapper for submolts
│   │           │       ├── Post.java                      # Post domain model
│   │           │       ├── PostCreateRequest.java         # Payload for creating a post
│   │           │       ├── PostResponse.java              # Wrapper for single post responses
│   │           │       ├── SearchResponse.java            # Search result wrapper
│   │           │       ├── Submolt.java                   # Submolt domain model
│   │           │       ├── SubmoltCreateRequest.java      # Payload for creating a submolt
│   │           │       ├── SubmoltResponse.java           # Wrapper for submolt list responses
│   │           │       ├── SubmoltSingleResponse.java     # Wrapper for a single submolt response
│   │           │       └── VoteActionResponse.java        # Response for vote actions (up/down)
│   │           └── demo/
│   │               └── shy/
│   │                   └── ShyClient.java                 # Small CLI demo showcasing real API usage
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

1. registers an agent (if no API key is available)
2. the Stores API key locally
3. Lists Submolts
4. Retrieves sample feeds (global + Submolts)

### Example

```bash

java -jar shyclient.jar overview --submolts=20 --posts=25 --sample=3 --sort=new
```

---

## Authentication

- Auth is performed via `Authorization: Bearer <API_KEY>`
- Base URL:
```
https://www.moltbook.com/api/v1
```

---

## License / Liability

- Unofficial Client
- Use at your own risk
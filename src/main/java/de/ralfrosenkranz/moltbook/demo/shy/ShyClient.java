package de.ralfrosenkranz.moltbook.demo.shy;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import de.ralfrosenkranz.moltbook.client.MoltbookClient;
import de.ralfrosenkranz.moltbook.client.MoltbookClientConfig;
import de.ralfrosenkranz.moltbook.client.http.MoltbookApiException;
import de.ralfrosenkranz.moltbook.client.model.AgentRegisterRequest;
import de.ralfrosenkranz.moltbook.client.model.AgentRegisterResponse;
import de.ralfrosenkranz.moltbook.client.model.Post;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.*;

/**
 * ShyClient: small CLI demo that uses the Moltbook Java client to obtain a first overview of
 * the real Moltbook service (register, persist API key, list submolts, sample content).
 *
 * Verified endpoints used (per official moltbook/api README):
 *  - POST /agents/register
 *  - GET  /submolts
 *  - GET  /posts?sort=hot|new|top|rising&limit=N
 */
public final class ShyClient {

    private static final String DEFAULT_BASE_URL = "https://www.moltbook.com/api/v1";
    private static final String CONFIG_DIR = ".moltbook";
    private static final String CONFIG_FILE = "shyclient.properties";

    public static void main(String[] args) {
        try {
            //System.setProperty("org.apache.logging.log4j.simplelog.level", "DEBUG");
            new ShyClient().run(args);
        } catch (Exception e) {
            System.err.println("ERROR: " + e.getMessage());
            e.printStackTrace(System.err);
            System.exit(1);
        }
    }

    private void run(String[] args) throws Exception {
        Map<String, String> flags = parseArgs(args);

        Path cfgPath = configPath();
        Properties props = loadProps(cfgPath);

        String baseUrl = firstNonBlank(flags.get("baseUrl"), props.getProperty("baseUrl"), DEFAULT_BASE_URL);
        String apiKey = firstNonBlank(flags.get("apiKey"), props.getProperty("apiKey"), null);

        if (isBlank(apiKey)) {
            System.out.println("No API key found. Registering a new agent (POST /agents/register).");
            RegistrationInput in = promptRegistrationInput(flags, props);
            AgentRegisterResponse resp = registerAndPersist(cfgPath, props, baseUrl, in);

            System.out.println("Saved API key locally for future runs: " + cfgPath.toAbsolutePath());
            if (!isBlank(in.claimUrl)) System.out.println("Claim URL (optional verification step): " + in.claimUrl);
            System.out.println();
            printAgentRegisterResponse(resp, "AgentRegisterResponse (freshly created)");

            // Requirement: End after first-time registration + properties creation.
            return;
        }

        System.out.println("Using API key from " + cfgPath.toAbsolutePath());

        MoltbookClientConfig cfg = MoltbookClientConfig.builder()
                .baseUrl(baseUrl)
                .apiKey(apiKey)
                .build();

        try (MoltbookClient client = MoltbookClient.builder().config(cfg).build()) {
            overview(client, flags);
        } catch (MoltbookApiException e) {
            if (e.statusCode() == 401) {
                System.out.println();
                System.out.println("HTTP 401 received. Displaying stored fullAgentRegisterResponse from properties.");
                System.out.println();
                displayStoredAgentRegisterResponse(props);
                return;
            }
            throw e;
        }
    }

    private void overview(MoltbookClient client, Map<String, String> flags) throws IOException {
        int submoltLimit = parseIntOr(flags.get("submolts"), 30);
        int postLimit = parseIntOr(flags.get("posts"), 25);
        int samplePerSubmolt = parseIntOr(flags.get("sample"), 2);
        String sort = firstNonBlank(flags.get("sort"), "hot");
        String submoltSort = firstNonBlank(flags.get("submoltSort"), "popular");

        // 1) List submolts (paginated)
        var submoltsPage = client.getSubmoltsApi().getSubmolts(submoltSort, submoltLimit, 0);
        var submolts = (submoltsPage != null && submoltsPage.items() != null) ? submoltsPage.items() : List.<de.ralfrosenkranz.moltbook.client.model.Submolt>of();
        if (submolts.isEmpty()) {
            System.out.println("No submolts returned by API.");
            return;
        }

        // Output header
        System.out.println();
        System.out.println("=== Moltbook Overview ===");
        System.out.println("Base URL: " + client.config().baseUrl());
        System.out.println("Time:     " + Instant.now());
        System.out.println("Submolts:  sort=" + submoltSort + ", limit=" + submoltLimit);
        System.out.println("Feed:     sort=" + sort + ", limit=" + postLimit);
        System.out.println("Feeds:    sort=" + sort + ", sample=" + samplePerSubmolt + " per submolt");
        System.out.println();


        System.out.println("=== First 100 Submolds ===");
        submolts.stream().forEach(submolt -> {
            StringBuilder sb = new StringBuilder();
            sb.append ("Name=" + submolt.name());
            sb.append ("  SubscriberCount=" + submolt.subscriberCount());
            sb.append ("  LastActivityAt=" + submolt.lastActivityAt());
            sb.append ("  CreatedBy=" + submolt.createdBy());
            sb.append ("  Description=" + submolt.description());
            //remove any line braking character by regex from StringBuilder
            String s = sb.toString().replaceAll("[\\r\\n\\t]+", " ");
            System.out.println (s);
        });
        System.out.println ();

        // 0) Personalized feed sample (/feed)
        try {
            var feedPage = client.getFeedApi().getFeed(sort, postLimit, 0);
            var feedPosts = (feedPage != null && feedPage.items() != null) ? feedPage.items() : List.<Post>of();
            System.out.println("=== Personalized Feed (/feed) ===");
            if (feedPosts.isEmpty()) {
                System.out.println("(no posts returned)");
            } else {
                for (int i = 0; i < Math.min(postLimit, feedPosts.size()); i++) {
                    Post p = feedPosts.get(i);
                    String title = firstNonBlank(safe(p.title()), "(no title)");
                    String sm = safe(p.submolt());
                    String author = safe(p.author());
                    String id = safe(p.id());

                    String line = "[" + (i + 1) + "] " + shrink(title, 120);
                    if (!isBlank(sm)) line += " (" + sm + ")";
                    if (!isBlank(author)) line += " by " + author;
                    if (!isBlank(id)) line += " [id=" + id + "]";
                    System.out.println(line);
                }
            }
            System.out.println();
        } catch (IOException e) {
            System.out.println("=== Personalized Feed (/feed) ===");
            System.out.println("(failed to fetch feed: " + e.getMessage() + ")");
            System.out.println();
        }

        System.out.println("=== Submolts (first " + Math.min(submoltLimit, submolts.size()) + ") ===");

        for (int i = 0; i < Math.min(submoltLimit, submolts.size()); i++) {
            var sm = submolts.get(i);
            String name = safe(sm.name());
            String display = safe(sm.displayName());
            String desc = safe(sm.description());

            String header = (isBlank(display) ? name : (name + " — " + display));
            System.out.println();
            System.out.println("[" + (i + 1) + "] " + header);
            if (!isBlank(desc)) System.out.println("    " + shrink(desc, 140));

            // 2) Fetch sample posts for this submolt via /submolts/{name}/feed
            try {
                var feedPage = client.getSubmoltsApi().getSubmoltFeed(name, sort, samplePerSubmolt, 0);
                var posts = (feedPage != null && feedPage.items() != null) ? feedPage.items() : List.<de.ralfrosenkranz.moltbook.client.model.Post>of();

                if (posts.isEmpty()) {
                    System.out.println("    (no posts in submolt feed)");
                    continue;
                }

                System.out.println("    Sample posts (" + Math.min(samplePerSubmolt, posts.size()) + "):");
                for (int j = 0; j < Math.min(samplePerSubmolt, posts.size()); j++) {
                    var p = posts.get(j);
                    String title = firstNonBlank(safe(p.title()), "(no title)");
                    String author = safe(p.author());
                    String id = safe(p.id());
                    String content = safe(p.content());
                    String url = safe(p.url());

                    String line = "      - " + shrink(title, 120);
                    if (!isBlank(author)) line += " (by " + author + ")";
                    if (!isBlank(id)) line += " [id=" + id + "]";
                    System.out.println(line);

                    String extra = firstNonBlank(url, content, null);
                    if (!isBlank(extra)) System.out.println("        " + shrink(extra, 160));
                }
            } catch (IOException e) {
                // Keep demo usable even if one feed request fails (e.g., transient network issues)
                System.out.println("    (failed to fetch submolt feed: " + e.getMessage() + ")");
            }
        }

        System.out.println();
        System.out.println("Tip: adjust output with flags: --submolts=20 --posts=25 --sample=3 --sort=new --submoltSort=popular");
    }

    private String safe(String s) {
        return s != null ? s : "";
    }


    private AgentRegisterResponse registerAndPersist(Path cfgPath, Properties props, String baseUrl, RegistrationInput in) throws IOException {
        MoltbookClientConfig cfg = MoltbookClientConfig.builder()
                .baseUrl(baseUrl)
                .apiKey(null) // register does not require an existing API key
                .build();

        try (MoltbookClient client = MoltbookClient.builder().config(cfg).build()) {
            AgentRegisterRequest req = new AgentRegisterRequest(in.name, in.description);
            AgentRegisterResponse resp = client.getAgentApi().register(req);

            String apiKey = (resp != null && resp.agent() != null) ? resp.agent().apiKey() : null;
            String claimUrl = (resp != null && resp.agent() != null) ? resp.agent().claimUrl() : null;
            in.claimUrl = claimUrl;

            if (isBlank(apiKey)) {
                throw new IOException("Registration succeeded but no api_key was returned.");
            }

            props.setProperty("baseUrl", baseUrl);
            props.setProperty("apiKey", apiKey);
            props.setProperty("agentName", in.name);
            props.setProperty("fullAgentRegisterResponse", resp.asJson());
            if (!isBlank(claimUrl)) props.setProperty("claimUrl", claimUrl);

            storeProps(cfgPath, props);
            return resp;
        }
    }

    private static void displayStoredAgentRegisterResponse(Properties props) {
        String json = props.getProperty("fullAgentRegisterResponse");
        if (isBlank(json)) {
            System.out.println("No stored fullAgentRegisterResponse found in properties.");
            return;
        }
        try {
            AgentRegisterResponse resp = parseAgentRegisterResponse(json);
            printAgentRegisterResponse(resp, "AgentRegisterResponse (from properties: fullAgentRegisterResponse)");
        } catch (Exception ex) {
            System.out.println("Failed to parse stored fullAgentRegisterResponse JSON.");
            System.out.println("Error: " + ex.getMessage());
            System.out.println();
            System.out.println("Raw JSON:");
            System.out.println(json);
        }
    }

    private static AgentRegisterResponse parseAgentRegisterResponse(String json) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(json, AgentRegisterResponse.class);
    }

    private static void printAgentRegisterResponse(AgentRegisterResponse resp, String title) {
        System.out.println("=== " + title + " ===");
        if (resp == null) {
            System.out.println("(null)");
            return;
        }

        System.out.println("success: " + resp.success());
        if (!isBlank(resp.status())) System.out.println("status:  " + resp.status());
        if (!isBlank(resp.message())) System.out.println("message: " + resp.message());
        System.out.println();

        if (resp.agent() != null) {
            var a = resp.agent();
            System.out.println("Agent:");
            if (!isBlank(a.id())) System.out.println("  id:                " + a.id());
            if (!isBlank(a.name())) System.out.println("  name:              " + a.name());
            if (!isBlank(a.apiKey())) System.out.println("  api_key:           " + a.apiKey());
            if (!isBlank(a.claimUrl())) System.out.println("  claim_url:         " + a.claimUrl());
            if (!isBlank(a.verificationCode())) System.out.println("  verification_code: " + a.verificationCode());
            if (!isBlank(a.profileUrl())) System.out.println("  profile_url:       " + a.profileUrl());
            if (!isBlank(a.createdAt())) System.out.println("  created_at:        " + a.createdAt());
            System.out.println();
        }

        if (resp.setup() != null && !resp.setup().isEmpty()) {
            System.out.println("Setup steps (" + resp.setup().size() + "):");
            List<String> keys = new ArrayList<>(resp.setup().keySet());
            Collections.sort(keys);
            for (String key : keys) {
                AgentRegisterResponse.SetupStep step = resp.setup().get(key);
                System.out.println();
                System.out.println("- " + key);
                if (step == null) {
                    System.out.println("  (null)");
                    continue;
                }
                if (!isBlank(step.action())) System.out.println("  action:            " + step.action());
                System.out.println("  critical:          " + step.critical());
                if (!isBlank(step.why())) System.out.println("  why:               " + step.why());
                if (!isBlank(step.details())) System.out.println("  details:           " + step.details());
                if (!isBlank(step.url())) System.out.println("  url:               " + step.url());
                if (!isBlank(step.messageTemplate())) {
                    System.out.println("  message_template:");
                    for (String line : step.messageTemplate().split("\\R")) {
                        System.out.println("    " + line);
                    }
                }
            }
            System.out.println();
        }

        if (resp.skillFiles() != null && !resp.skillFiles().isEmpty()) {
            System.out.println("Skill files (" + resp.skillFiles().size() + "):");
            List<String> keys = new ArrayList<>(resp.skillFiles().keySet());
            Collections.sort(keys);
            for (String k : keys) {
                System.out.println("  - " + k + ": " + resp.skillFiles().get(k));
            }
            System.out.println();
        }

        if (!isBlank(resp.tweetTemplate())) {
            System.out.println("tweet_template:");
            System.out.println(resp.tweetTemplate());
            System.out.println();
        }

        // Also print pretty JSON for copy/paste/debug.
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.enable(SerializationFeature.INDENT_OUTPUT);
            System.out.println("Raw JSON:");
            System.out.println(mapper.writeValueAsString(resp));
        } catch (Exception ignored) {
            System.out.println("Raw JSON:");
            System.out.println(resp.asJson());
        }
        System.out.println("=== END ===");
    }

    private RegistrationInput promptRegistrationInput(Map<String, String> flags, Properties props) {
        String defaultName = firstNonBlank(flags.get("agentName"), props.getProperty("agentName"), "RRShyClient_" + randomSuffix());
        String defaultDesc = firstNonBlank(flags.get("description"), props.getProperty("description"), "CLI demo agent (RRShyClient)");

        Scanner sc = new Scanner(System.in);

        System.out.print("Agent name [" + defaultName + "]: ");
        String name = sc.nextLine();
        if (isBlank(name)) name = defaultName;

        System.out.print("Description [" + defaultDesc + "]: ");
        String desc = sc.nextLine();
        if (isBlank(desc)) desc = defaultDesc;

        return new RegistrationInput(name, desc);
    }

    private static final class RegistrationInput {
        final String name;
        final String description;
        String claimUrl;

        RegistrationInput(String name, String description) {
            this.name = name;
            this.description = description;
        }
    }

    private static Map<String, String> parseArgs(String[] args) {
        Map<String, String> m = new HashMap<>();
        for (String a : args) {
            if (a == null) continue;
            if (a.startsWith("--") && a.contains("=")) {
                int idx = a.indexOf('=');
                String k = a.substring(2, idx).trim();
                String v = a.substring(idx + 1).trim();
                if (!k.isEmpty()) m.put(k, v);
            }
        }
        return m;
    }

    private static Path configPath() {
        String home = System.getProperty("user.home");
        return Path.of(home, CONFIG_DIR, CONFIG_FILE);
    }

    private static Properties loadProps(Path p) throws IOException {
        Properties props = new Properties();
        if (Files.exists(p)) {
            try (InputStream in = Files.newInputStream(p)) {
                props.load(in);
            }
        }
        return props;
    }

    private static void storeProps(Path p, Properties props) throws IOException {
        Files.createDirectories(p.getParent());
        try (OutputStream out = Files.newOutputStream(p)) {
            props.store(out, "ShyClient configuration (Moltbook API)");
        }
    }

    private static String textField(JsonNode n, String field) {
        if (n == null || field == null) return null;
        JsonNode v = n.get(field);
        if (v == null || v.isNull()) return null;
        if (v.isTextual()) return v.asText();
        return v.toString();
    }

    private static int parseIntOr(String s, int def) {
        try {
            if (s == null) return def;
            return Integer.parseInt(s.trim());
        } catch (Exception e) {
            return def;
        }
    }

    private static String shrink(String s, int max) {
        if (s == null) return null;
        String t = s.replace("\r", " ").replace("\n", " ").trim();
        if (t.length() <= max) return t;
        return t.substring(0, Math.max(0, max - 1)) + "…";
    }

    private static String randomSuffix() {
        String alphabet = "abcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder();
        Random r = new Random();
        for (int i = 0; i < 6; i++) sb.append(alphabet.charAt(r.nextInt(alphabet.length())));
        return sb.toString();
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private static String firstNonBlank(String... values) {
        if (values == null) return null;
        for (String v : values) {
            if (!isBlank(v)) return v;
        }
        return null;
    }
}

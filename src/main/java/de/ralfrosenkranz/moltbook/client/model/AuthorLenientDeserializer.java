package de.ralfrosenkranz.moltbook.client.model;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;

/**
 * Accepts either a string (id/handle) or a full object for the author field.
 */
public class AuthorLenientDeserializer extends JsonDeserializer<Author> {
    @Override
    public Author deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonToken t = p.currentToken();
        if (t == JsonToken.VALUE_STRING) {
            String v = p.getValueAsString();
            if (v == null) return null;
            return new Author(v, null, null, null, null);
        }
        if (t == JsonToken.START_OBJECT) {
            JsonNode node = p.getCodec().readTree(p);
            Author a = p.getCodec().treeToValue(node, Author.class);
            // If the object doesn't include an id but is still meaningful, keep it.
            return a;
        }
        if (t == JsonToken.VALUE_NULL) {
            return null;
        }
        // fallback: attempt to read as tree and convert
        JsonNode node = p.getCodec().readTree(p);
        if (node == null || node.isNull()) return null;
        if (node.isTextual()) {
            return new Author(node.asText(), null, null, null, null);
        }
        if (node.isObject()) {
            return p.getCodec().treeToValue(node, Author.class);
        }
        return null;
    }
}

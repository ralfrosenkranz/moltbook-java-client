package de.ralfrosenkranz.moltbook.client.model;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

/**
 * Moltbook's API sometimes returns the "submolt" field as a string (id/name)
 * and sometimes as an object. This deserializer accepts both.
 */
public final class SubmoltLenientDeserializer extends JsonDeserializer<Submolt> {

    @Override
    public Submolt deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonToken t = p.currentToken();
        if (t == null) {
            t = p.nextToken();
        }

        if (t == JsonToken.VALUE_STRING) {
            String s = p.getValueAsString();
            if (s == null || s.isBlank()) {
                return null;
            }
            // Best-effort mapping: id is known, the remaining fields are unknown.
            return new Submolt(s, null, null, null, null, null, null, null, null);
        }

        if (t == JsonToken.START_OBJECT) {
            ObjectMapper om = (ObjectMapper) p.getCodec();
            return om.readValue(p, Submolt.class);
        }

        // Unknown token → let Jackson handle default coercion rules.
        return (Submolt) ctxt.handleUnexpectedToken(Submolt.class, p);
    }
}

package com.group.docorofile.utils;

import org.springframework.beans.factory.annotation.Value;

public class JsonUtils {

    public String extractMessageFromException(String exceptionMessage) {
        try {
            // Find the JSON part in the exception message
            int jsonStart = exceptionMessage.indexOf("{");
            int jsonEnd = exceptionMessage.lastIndexOf("}") + 1;

            if (jsonStart != -1 && jsonEnd > jsonStart) {
                String jsonPart = exceptionMessage.substring(jsonStart, jsonEnd);

                // Extract message field from JSON (simple string parsing)
                String messageKey = "\"message\":\"";
                int messageStart = jsonPart.indexOf(messageKey);
                if (messageStart != -1) {
                    messageStart += messageKey.length();
                    int messageEnd = jsonPart.indexOf("\"", messageStart);
                    if (messageEnd != -1) {
                        return jsonPart.substring(messageStart, messageEnd);
                    }
                }
            }

            // Fallback to original message if parsing fails
            return "Failed.";
        } catch (Exception e) {
            return "Failed.";
        }
    }
}

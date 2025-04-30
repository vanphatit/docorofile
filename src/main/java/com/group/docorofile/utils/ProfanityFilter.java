package com.group.docorofile.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

public class ProfanityFilter {
    private static final Set<String> BAD_WORDS = new HashSet<>();

    static {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(ProfanityFilter.class.getResourceAsStream("/badWords.txt")))) {
            String line;
            while ((line = reader.readLine()) != null) {
                BAD_WORDS.add(line.trim().toLowerCase());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Kiểm tra message có chứa từ cấm hoặc biến thể không
    public static boolean containsBadWords(String message) {
        String lowerMsg = message.toLowerCase();

        for (String badWord : BAD_WORDS) {
            String regex = String.join("[\\W_]*", badWord.split("")); // cho phép dấu đặc biệt giữa các chữ
            regex = "(?i).*" + regex + ".*"; // không phân biệt hoa thường

            if (lowerMsg.matches(regex)) {
                return true;
            }
        }
        return false;
    }

    // Thay từ cấm và biến thể bằng dấu *
    public static String filterBadWords(String message) {
        String filtered = message;

        for (String badWord : BAD_WORDS) {
            String regex = String.join("[\\W_]*", badWord.split(""));
            regex = "(?i)" + regex;

            String replacement = "*".repeat(badWord.length());
            filtered = filtered.replaceAll(regex, replacement);
        }
        return filtered;
    }
}


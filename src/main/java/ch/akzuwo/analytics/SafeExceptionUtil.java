package ch.akzuwo.analytics;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.regex.Pattern;

public final class SafeExceptionUtil {

    private static final Pattern PASSWORD_PATTERN = Pattern.compile("(?i)(password|passwd|pwd|token|secret|api[_-]?key)=([^\\s&;]+)");
    private static final Pattern AUTH_BEARER_PATTERN = Pattern.compile("(?i)(Authorization\\s*:\\s*Bearer\\s+)[A-Za-z0-9._~+/=-]+");
    private static final Pattern JDBC_CREDENTIAL_PATTERN = Pattern.compile("(?i)(jdbc:mysql://)([^:@/\\s]+):([^@/\\s]+)@");
    private static final Pattern DISCORD_BOT_TOKEN_PATTERN = Pattern.compile("[MN][A-Za-z\\d]{23}\\.[\\w-]{6}\\.[\\w-]{27}");
    private static final Pattern UUID_PATTERN = Pattern.compile("\\b[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}\\b");
    private static final Pattern IP_PATTERN = Pattern.compile("\\b(?:\\d{1,3}\\.){3}\\d{1,3}\\b");
    private static final Pattern LONG_SECRET_PATTERN = Pattern.compile("\\b[A-Za-z0-9_\\-+/=]{48,}\\b");

    private SafeExceptionUtil() {
    }

    public static String stackTraceToString(Throwable throwable, int maxLength) {
        if (throwable == null) {
            return "";
        }
        StringWriter writer = new StringWriter();
        throwable.printStackTrace(new PrintWriter(writer));
        return truncate(sanitize(writer.toString()), maxLength);
    }

    public static String sanitize(String input) {
        if (input == null) {
            return "";
        }
        String sanitized = PASSWORD_PATTERN.matcher(input).replaceAll("$1=<redacted>");
        sanitized = AUTH_BEARER_PATTERN.matcher(sanitized).replaceAll("$1<redacted>");
        sanitized = JDBC_CREDENTIAL_PATTERN.matcher(sanitized).replaceAll("$1<redacted>:<redacted>@");
        sanitized = DISCORD_BOT_TOKEN_PATTERN.matcher(sanitized).replaceAll("<redacted-discord-token>");
        sanitized = UUID_PATTERN.matcher(sanitized).replaceAll("<redacted-uuid>");
        sanitized = IP_PATTERN.matcher(sanitized).replaceAll("<redacted-ip>");
        sanitized = LONG_SECRET_PATTERN.matcher(sanitized).replaceAll("<redacted-secret>");
        return sanitized;
    }

    public static String truncate(String input, int maxLength) {
        if (input == null) {
            return "";
        }
        if (maxLength <= 0 || input.length() <= maxLength) {
            return input;
        }
        return input.substring(0, maxLength) + "\n... <truncated>";
    }
}

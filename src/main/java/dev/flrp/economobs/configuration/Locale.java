package dev.flrp.economobs.configuration;

import dev.flrp.economobs.Economobs;
import net.md_5.bungee.api.ChatColor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Locale {

    private static final Pattern hexPattern = Pattern.compile("<#([A-Fa-f0-9]){6}>");
    private static final Economobs instance = Economobs.getInstance();

    public static String PREFIX;
    public static String COMMAND_DENIED;
    public static String ECONOMY_GIVEN;
    public static String ECONOMY_MAX;
    public static String ECONOMY_FAILED;
    public static String ECONOMY_TOGGLE;

    public static void load() {
        PREFIX = addMessage("prefix");
        COMMAND_DENIED = addMessage("command-denied");
        ECONOMY_GIVEN = addMessage("economy-given");
        ECONOMY_MAX = addMessage("economy-max");
        ECONOMY_FAILED = addMessage("economy-failed");
        ECONOMY_TOGGLE = addMessage("economy-toggle");
    }

    private static String addMessage(String identifier) {
        return instance.getLanguage().getConfiguration().getString(identifier);
    }

    public static String parse(String context) {
        Matcher matcher = hexPattern.matcher(context);
        while (matcher.find()) {
            final ChatColor hexColor = ChatColor.of(matcher.group().substring(1, matcher.group().length() - 1));
            final String before = context.substring(0, matcher.start());
            final String after = context.substring(matcher.end());
            context = before + hexColor + after;
            matcher = hexPattern.matcher(context);
        }
        return ChatColor.translateAlternateColorCodes('&', context);
    }

}

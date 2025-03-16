package fun.kaituo.tagmansion.util;

import org.bukkit.ChatColor;

public class Misc {
    public static String getCharacterDisplayName(Class<? extends PlayerData> characterClass) {
        try {
            return (String) characterClass.getField("displayName").get(null);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Failed to get static field displayName from " + characterClass.getSimpleName());
        }
    }

    public static String getCharacterChooseMessage(Class<? extends PlayerData> characterClass) {
        try {
            return (String) characterClass.getField("chooseMessage").get(null);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Failed to get static field chooseMessage from " + characterClass.getSimpleName());
        }
    }

    public static ChatColor getCharacterColor(Class<? extends PlayerData> characterClass) {
        try {
            return (ChatColor) characterClass.getField("color").get(null);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Failed to get static field color from " + characterClass.getSimpleName());
        }
    }

    public static boolean isCharacterHuman(Class<? extends PlayerData> characterClass) {
        if (Human.class.isAssignableFrom(characterClass)) {
            return true;
        }
        if (Hunter.class.isAssignableFrom(characterClass)) {
            return false;
        }
        throw new RuntimeException("Character class is neither Human nor Hunter: " + characterClass.getSimpleName());
    }
}

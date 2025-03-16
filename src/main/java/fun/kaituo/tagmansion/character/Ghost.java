package fun.kaituo.tagmansion.character;

import fun.kaituo.tagmansion.util.Hunter;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

@SuppressWarnings("unused")
public class Ghost extends Hunter {
    public static final String displayName = "幽鬼";
    public static final String chooseMessage = "我将引你们进入永恒的沉眠...";
    public static final ChatColor color = ChatColor.DARK_GRAY;

    public Ghost(Player p) {
        super(p);
    }
}

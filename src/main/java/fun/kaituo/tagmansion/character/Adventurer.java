package fun.kaituo.tagmansion.character;

import fun.kaituo.tagmansion.util.Human;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

@SuppressWarnings("unused")
public class Adventurer extends Human {
    public static final String displayName = "冒险家";
    public static final String chooseMessage = "与我一同寻觅宝藏吧！";
    public static final ChatColor color = ChatColor.WHITE;

    public Adventurer(Player p) {
        super(p);
    }
}

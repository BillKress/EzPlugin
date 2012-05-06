package scripts
import org.bukkit.event.EventHandler
import static org.bukkit.event.block.Action.*
import org.bukkit.event.player.PlayerInteractEvent
import static org.bukkit.Material.*


/** 
 * Diabetes
 * Emergency food from sugar, but pay for it in health 
 */
@EventHandler
public void playerEatsSugar(PlayerInteractEvent event) {
    def player=event.player

    // In this case we are only interested in trying to right-click nothing (eat)
    if(event.action != RIGHT_CLICK_AIR)
        return

    // The "Eat" attempt is only supporting sugar right now.
    if(event.item?.type == SUGAR) {
        def amount=event.item.amount

        // Eating animation not supported, let's pretend.
        player.sendMessage("Nom!");

        if( player.foodLevel++ > 19)
            player.foodLevel = 19

        player.health--

        if(player.itemInHand.amount-- == 1)
            player.itemInHand = null
    }
}


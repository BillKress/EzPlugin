package scripts


import org.bukkit.event.*
import org.bukkit.event.block.*
import org.bukkit.event.player.*
import org.bukkit.*


@EventHandler
public void breakWoodBlock(BlockBreakEvent event) {
    def block = event.block
    def player = event.player

    if(block.type != Material.CROPS || player.itemInHand.type != Material.SEEDS)
        return

    int nBroke = megaBreak(block.location, [Material.CROPS], player.itemInHand.amount)

    if(nBroke < player.itemInHand.amount)
        player.itemInHand.amount -= nBroke
    else
        player.setItemInHand(null)
}

public int megaBreak(Location firstLoc, types, int maxAmount) {
    int nBroke = 0;
    ArrayList locs= new ArrayList();

    locs.push(firstLoc)

    while(!locs.isEmpty()) {
        Location loc = locs.pop()
        if(!types.contains(loc.block.type) || !loc.block.data == 7)
            continue;
        loc.block.breakNaturally();
        nBroke++
        maxAmount--
        if(maxAmount == 0)
            return nBroke;
        for(int x : -3..3)
            for(int y : -3..3)
                for(int z : -3..3) {
                    if(x==0 && y==0 && z==0)
                        continue
                    def newLoc = loc.clone()
                    newLoc.add(x,y,z)
                    if(types.contains(newLoc.block.type) && newLoc.block.data == 7)
                        locs << newLoc
                }
    }
    return nBroke
}

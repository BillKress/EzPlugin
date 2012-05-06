package scripts

import org.bukkit.event.*
import org.bukkit.event.block.*
import org.bukkit.event.player.*
import org.bukkit.*
import static org.bukkit.Material.*
import tv.kress.bill.minecraft.ezplugin.*;


@EventHandler
def breakWoodBlock(BlockBreakEvent event) {
    if(event.block.type != LOG)
        return

    megaBreak(event.block.location, [LOG, LEAVES], 10, event.player)
}

def megaBreak(Location firstLoc, types, max, player, ArrayList testLocs = [], ArrayList doneLocs = []) {
    int nBroke = 0;

    def breakLocs = []

    if(firstLoc != null)
        testLocs << firstLoc

    while(testLocs && breakLocs.size < max) {
        Location loc = testLocs.pop()

        cube(loc, 1 ) {
            if(types.contains(it.block.type) && !breakLocs.contains(it) && !doneLocs.contains(it)) {
                breakLocs << it
                testLocs << it
            }
        }
    }
    for(Location loc : breakLocs) {
        nBroke++
        loc.block.breakNaturally();
    }

    print("broke " + nBroke + " blocks, remaining=" + testLocs.size)

    if(testLocs)
        player.server.scheduler.scheduleSyncDelayedTask(EzPlugin.myInstance, new Runnable() {
                    public void run() {
                        megaBreak(null, types, max, player, testLocs)
                    }
                }, 1)

    return nBroke
}

// Executes closure for each location around the one passed in (including the one passed in)
// out to a radius of radius (It's a cube, but yeah, radius.  Whatever)
def cube(Location loc, int radius, func) {
    for(int x : -radius..+radius)
        for(int y : -radius..radius)
            for(int z : -radius..radius) {
                def newLoc = loc.clone()
                newLoc.add(x,y,z)
                func(newLoc);
            }
}
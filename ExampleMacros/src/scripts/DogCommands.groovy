package scripts
import org.bukkit.command.CommandSender
import org.bukkit.entity.*

import tv.kress.bill.minecraft.ezplugin.Cmd

// Note the difference between "sit" and "stand"
// with "sit" I use the "Normal" method to prohibit the console
// with "Stand" I just request a "Player" and anyone trying from the
// console will get a "wrong location" error message.
@Cmd("sit")
def sitDogs(CommandSender sender) {
    if(!sender instanceof Player) {
        sender.sendMessage("Only players can execute this command");
        return;
    }
    doSit(sender, true);
}

// This doesn't work right now--bukkit bug (Actually they sit but scoot around on their butts)
@Cmd("stand")
def standDogs(Player sender) {
    myDogs(sender) {
        it.setSitting(false);
    }
}

@Cmd("spawn")
def spawnDog(Player sender) {
    player.getWorld().spawnCreature(player.getLocation(), EntityType.WOLF)
}

@Cmd("hellhound")
def hellHound(Player sender) {
    myDogs(sender) {
        it.setNoDamageTicks(100)
        it.setFireTicks(100)
    }
}

def myDogs(Player player, int range=8, Closure task) {
    def entities = player.getNearbyEntities(range, range, range);
    entities.each {
        if(it instanceof Wolf && player.equals(it.getOwner())) {
            task(it);
        }
    }
}
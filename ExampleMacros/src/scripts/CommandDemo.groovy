package scripts
import tv.kress.bill.minecraft.ezplugin.*
import org.bukkit.event.*
import groovy.transform.Field

// This variable will retain it's value between runs.
// After the first time, the initializer will be ignored.

@Field @Persist Integer             testTimes = new Integer(0);
@Field @Persist int                 runTimes = 0
@Field @Persist ArrayList<String>   list=new ArrayList<String>();

def onEnable() {
    log "onEnable method called this many times: " + ++runTimes
}

def onDisable() {
    print this.class.name + ": onDisable method called"
}

// One class can have any number of "Commands".  These will be executed by a user via "/ezp <command> args"
// If you don't ask for any variables, it will just ignore anything else on the command line
// Might be better to have it error...?

// Very simple command
@Cmd(value="test", description="Second command in the same file")
public void testCommand() {
    log "Test command has been executed " + ++testTimes + " times"
}

// You can have multiple commands and some can have argument arrays (String[] args)
// All args are passed in as an optional string array.  Args[0] is the command name.
@Cmd(
value="bill",
description="Test command to demonstrate commands and persistence",
permission="test.permission")

public void billCommand(sender, args) {
    // args[0] will always be "bill" here because that is the @command registered
    sender.sendMessage(sender.name + ", your command executed: " + args)

    if(args.length > 1)
        list.addAll(args[1..-1]);
    else {
        list.clear()
        log "Cleared previous commands"
    }
    log "command history:" + list
    sender.sendMessage( "This plugin has started " + runTimes + " times.");
}



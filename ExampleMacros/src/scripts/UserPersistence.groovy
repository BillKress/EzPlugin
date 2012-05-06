package scripts
import tv.kress.bill.minecraft.ezplugin.Cmd

// So far I'm not crazy about the user based persistence, I'd
// really rather not use the hash "p" (More accurately the hash
// should be invisible like with the @Persist annotation).

// What it saves you is having to think about where to store variables
// and having to remember to save the hash at the end.

// persistScope can be nested.  After exiting the scope, any values
// created, deleted or updated will be saved

// I'd really like this to work exactly like the @Persist annotation--without
// the inner class, just use @Persisit annotations on method variables to have
// them automatically persisted by user, but this is actually more flexible since
// you can supply any string to the scope and nest scopes.

@Cmd(
value="testup",
description="Test user persistence"
)

public void testUserPersistenceCommand(sender, args) {
    persistScope(sender.name) { p ->
        // persisted variables will be created on the fly if they are referenced.
        // If we read before writing the first time, the value will be null
        if( p.count == null )
            p.count = 0
        else
            p.count++

        sender.sendMessage "You executed this command " +p.count + " times"
        log sender.getName()+" executed this command " + p.count +" times"
    }
}

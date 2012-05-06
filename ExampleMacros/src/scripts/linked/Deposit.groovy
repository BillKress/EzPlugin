package scripts.linked
import groovy.transform.Field
import tv.kress.bill.minecraft.ezplugin.*

@Inject @Field
        Account account;

@Cmd("deposit")
public void testCommand(sender, String[] args) {
    account.modifyBalance(sender.name, 1)
    print "deposited 1"
}

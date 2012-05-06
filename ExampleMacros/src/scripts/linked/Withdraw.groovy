package scripts.linked
import groovy.transform.Field
import tv.kress.bill.minecraft.ezplugin.*

@Inject @Field
        Account account;

@Cmd("withdraw")
public void testCommand(sender, args) {
    if(account == null)
        account = Account.getInstance();
    account.modifyBalance(sender.name, -1)
    print "withdrew 1"
}

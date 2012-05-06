package scripts.linked
import groovy.transform.Field

import org.bukkit.command.CommandSender

import tv.kress.bill.minecraft.ezplugin.*


@Inject @Field
        Account account;

@Cmd("balance")
public void testCommand(CommandSender sender, String[] args) {
    if(account == null)
        account = Account.getInstance();

    int bal = account.modifyBalance(sender.name, 0)
    print sender.name + " has a current balance = " + bal
}

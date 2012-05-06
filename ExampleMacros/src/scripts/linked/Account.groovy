package scripts.linked
import groovy.transform.Field
import tv.kress.bill.minecraft.ezplugin.*


@Field @Persist def HashMap<String, Integer> balance = new HashMap<String, Integer>()

public int modifyBalance(String user, int difference) {
    Integer balanceInteger=balance.get(user)
    int i=balanceInteger == null ? 0 : balanceInteger
    i+=difference
    balance.put(user, i)
    return i;
}


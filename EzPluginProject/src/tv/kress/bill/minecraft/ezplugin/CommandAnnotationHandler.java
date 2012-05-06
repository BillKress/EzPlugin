package tv.kress.bill.minecraft.ezplugin;

import java.lang.reflect.*;
import java.util.*;

import org.bukkit.command.CommandSender;

class CommandAnnotationHandler {
    // Maps the command name to a container that can invoke the command
    private final HashMap<String, CommandContainer> commandMap = new HashMap<String, CommandContainer>();
    // Maps the GroovyObject to the string so that it can be looked up in
    // the above table and be removed from both
    private final HashMap<Object, List<String>> commandCleaner = new HashMap<Object, List<String>>();

    /**
     * Adds all the methods that are annotated to receive "Commands" to the
     * commandMap.
     * 
     * @param target
     */
    public List<String> addCommandsFor(Object target) {
        List<String> cmds = new ArrayList<String>();
        for (Method m : target.getClass().getMethods()) {
            if (m.isAnnotationPresent(tv.kress.bill.minecraft.ezplugin.Cmd.class)) {
                tv.kress.bill.minecraft.ezplugin.Cmd annotation = m.getAnnotation(tv.kress.bill.minecraft.ezplugin.Cmd.class);
                commandMap.put(annotation.value(), new CommandContainer(m, target, annotation.permission(), annotation.description()));
                cmds.add(annotation.value());
            }
        }
        commandCleaner.put(target, cmds);
        return cmds;
    }

    public void remove(Object target) {
        List<String> commandList = commandCleaner.remove(target);
        if (commandList != null)
            for (String command : commandList)
                commandMap.remove(command);
    }

    public String executeCommand(CommandSender sender, String[] args) {
        // Look to see if anyone has asked for this command.
        CommandContainer cc = commandMap.get(args[0]);
        if (cc == null)
            return "Command not found";

        return cc.executeCommand(sender, args);
    }

    public String[] listCommands() {
        ArrayList<String> ret = new ArrayList<String>();
        for (String cmd : commandMap.keySet()) {
            String desc = commandMap.get(cmd).description;
            ret.add("- " + cmd + ": " + desc);

        }
        return ret.toArray(new String[] {});
    }

    class CommandContainer {
        final Method method;
        final Object target;
        final String permission;
        final String description;

        public CommandContainer(Method method, Object target, String permission, String description) {
            this.method = method;
            this.target = target;
            this.permission = permission;
            this.description = description;
        }

        /**
         * Forwards the command to the interested party. Ensures permissions are
         * owned by entity if requested.
         * 
         * @param sender
         * @param args
         * @return
         */
        public String executeCommand(CommandSender sender, String[] args) {
            try {
                if (permission.length() != 0 && !sender.hasPermission("permission")) {
                    System.out.println("Player " + sender.getName() + " lacks permission " + permission + " for command " + args[0]);
                    return "You do not have the permissions to execute that command";
                }
                int paramSize = method.getParameterTypes().length;
                Object[] parameters = new Object[paramSize];
                if (paramSize > 0) {
                    if (method.getParameterTypes()[0].isAssignableFrom(sender.getClass()))
                        parameters[0] = sender;
                    else
                        return "This command cannot be executed from your location (Probably console)";

                }

                if (paramSize > 1)
                    parameters[1] = args;
                method.invoke(target, parameters);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
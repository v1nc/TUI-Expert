package de.reckendrees.systems.tui.expert.commands;

import android.content.Context;
import android.os.Build;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import de.reckendrees.systems.tui.expert.commands.main.specific.APICommand;
import de.reckendrees.systems.tui.expert.commands.main.specific.APIRootCommand;
import de.reckendrees.systems.tui.expert.commands.main.specific.HideableCommand;
import de.reckendrees.systems.tui.expert.commands.main.specific.RootCommand;
import de.reckendrees.systems.tui.expert.managers.xml.XMLPrefsManager;
import de.reckendrees.systems.tui.expert.managers.xml.options.Expert;
import de.reckendrees.systems.tui.expert.tuils.Tuils;

public class CommandGroup {

    private String packageName;
    private CommandAbstraction[] commands;
    private String[] commandNames;

    public CommandGroup(Context c, String packageName) {
        this.packageName = packageName;

        List<String> cmds;
        try {
            cmds = Tuils.getClassesInPackage(packageName, c);
        } catch (IOException e) {
            return;
        }

        List<CommandAbstraction> cmdAbs = new ArrayList<>();
        Iterator<String> iterator = cmds.iterator();
        Boolean use_root = XMLPrefsManager.getBoolean(Expert.use_root);
        while (iterator.hasNext()) {
            String s = iterator.next();
            CommandAbstraction ca = buildCommand(s);
            if(
                    ca != null &&
                    ( !(ca instanceof APIRootCommand) || ((APIRootCommand) ca).willWorkOn(Build.VERSION.SDK_INT)) &&
                    ( !(ca instanceof APICommand) || ((APICommand) ca).willWorkOn(Build.VERSION.SDK_INT)) &&
                    (use_root || !(ca instanceof RootCommand)) &&
                    (!(ca instanceof HideableCommand) || ((HideableCommand) ca).show())
            ) {
                cmdAbs.add(ca);
            } else {
                iterator.remove();
            }
        }

        Collections.sort(cmds);
        commandNames = new String[cmds.size()];
        cmds.toArray(commandNames);

        Collections.sort(cmdAbs, (o1, o2) -> o2.priority() - o1.priority());
        commands = new CommandAbstraction[cmdAbs.size()];
        cmdAbs.toArray(commands);
    }

    public CommandAbstraction getCommandByName(String name) {
        for(CommandAbstraction c : commands) {
            if(c.getClass().getSimpleName().equals(name)) {
                return c;
            }
        }

        return null;
    }

    private CommandAbstraction buildCommand(String name) {
        String fullCmdName = packageName + Tuils.DOT + name;
        try {
            Class<CommandAbstraction> clazz = (Class<CommandAbstraction>) Class.forName(fullCmdName);
            if(CommandAbstraction.class.isAssignableFrom(clazz)) {
                Constructor<CommandAbstraction> constructor = clazz.getConstructor();
                return constructor.newInstance();
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    public CommandAbstraction[] getCommands() {
        return commands;
    }

    public String[] getCommandNames() {
        return commandNames;
    }

}

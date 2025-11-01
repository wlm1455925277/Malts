package dev.jsinco.malts.commands;

import dev.jsinco.malts.commands.interfaces.SubCommandWrapper;
import dev.jsinco.malts.commands.subcommands.VaultOtherCommand;

public class VaultOtherBaseCommand extends SubCommandWrapper {
    public VaultOtherBaseCommand() {
        super(new VaultOtherCommand());
    }
}

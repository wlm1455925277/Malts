package dev.jsinco.malts.commands;

import dev.jsinco.malts.commands.interfaces.SubCommandWrapper;
import dev.jsinco.malts.commands.subcommands.SearchCommand;

public class VaultSearchBaseCommand extends SubCommandWrapper {
    public VaultSearchBaseCommand() {
        super(new SearchCommand());
    }
}

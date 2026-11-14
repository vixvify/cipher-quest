package com.cipherquest.levels.linux.commands;

import com.cipherquest.levels.linux.TerminalContext;
import java.util.List;

public final class HelpCommand implements Command {
    @Override public String execute(List<String> args, TerminalContext ctx) {
        return "Available commands:\n" +
                "  ls [options] [path]  - List directory contents (-a, -l, -la)\n" +
                "  cd <path>            - Change current directory\n" +
                "  pwd                  - Print working directory\n" +
                "  cat <file>           - Display file content\n" +
                "  find <path> [-name]  - Search files\n" +
                "  whoami               - Print current user\n" +
                "  uname [-a]           - Print system info\n" +
                "  echo <text>          - Print text\n" +
                "  clear                - Clear terminal screen\n" +
                "  ./<binary>           - Execute program";
    }
}

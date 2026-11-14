package com.cipherquest.levels.linux.commands;

import com.cipherquest.levels.linux.TerminalContext;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

public final class LsCommand implements Command {
    @Override public String execute(List<String> args, TerminalContext ctx) throws IOException {
        boolean showAll = false;
        boolean longFormat = false;
        String target = ".";
        for (String arg : args) {
            if (arg.startsWith("-") && !arg.equals("-")) {
                for (int i = 1; i < arg.length(); i++) {
                    if (arg.charAt(i) == 'a') showAll = true;
                    else if (arg.charAt(i) == 'l') longFormat = true;
                    else return "ls: invalid option -- '" + arg.charAt(i) + "'";
                }
            } else {
                target = arg;
            }
        }
        Path directory = ctx.resolve(target);
        if (!Files.exists(directory)) {
            return "ls: cannot access '" + target + "': No such file or directory";
        }
        if (!Files.isDirectory(directory)) {
            return longFormat ? format(ctx, directory, directory.getFileName().toString())
                    : directory.getFileName().toString();
        }

        List<Path> paths = new ArrayList<>();
        try (Stream<Path> stream = Files.list(directory)) {
            stream.forEach(paths::add);
        }
        paths.sort(Comparator.comparing(p -> p.getFileName().toString()));
        List<String> items = new ArrayList<>();
        if (showAll) {
            items.add(longFormat ? format(ctx, directory, ".") : ".");
            Path parent = directory.getParent() == null ? directory : directory.getParent();
            items.add(longFormat ? format(ctx, parent, "..") : "..");
        }
        for (Path path : paths) {
            String name = path.getFileName().toString();
            if (!showAll && name.startsWith(".")) continue;
            String display = name + (Files.isDirectory(path) ? "/" : "");
            items.add(longFormat ? format(ctx, path, display) : display);
        }
        if (!longFormat) return String.join("  ", items);
        return "total " + items.size() + (items.isEmpty() ? "" : "\n" + String.join("\n", items));
    }

    private static String format(TerminalContext ctx, Path path, String name) throws IOException {
        boolean directory = Files.isDirectory(path);
        String permissions = directory ? "drwxr-xr-x" :
                (ctx.isExecutable(path) ? "-rwxr-xr-x" : "-rw-r--r--");
        long size = directory ? 4096 : Files.size(path);
        return String.format("%s  1 agent agent  %4d  Nov 14 12:00  %s",
                permissions, size, name);
    }
}

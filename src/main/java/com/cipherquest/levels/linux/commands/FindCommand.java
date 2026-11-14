package com.cipherquest.levels.linux.commands;

import com.cipherquest.levels.linux.TerminalContext;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public final class FindCommand implements Command {
    @Override public String execute(List<String> args, TerminalContext ctx) throws IOException {
        String start = ".";
        String namePattern = null;
        for (int i = 0; i < args.size(); i++) {
            String arg = args.get(i);
            if (arg.equals("-name")) {
                if (i + 1 == args.size()) return "find: missing argument to '-name'";
                namePattern = args.get(++i);
            } else if (arg.startsWith("-")) {
                return "find: unknown option '" + arg + "'";
            } else {
                start = arg;
            }
        }
        Path root = ctx.resolve(start);
        if (!Files.exists(root)) {
            return "find: '" + start + "': No such file or directory";
        }

        final Pattern matcher = namePattern == null ? null : compileGlob(namePattern);
        List<String> matches = new ArrayList<>();
        try (Stream<Path> stream = Files.walk(root)) {
            stream.filter(path -> matcher == null || matcher.matcher(
                    path.getFileName() == null ? "" : path.getFileName().toString()).matches())
                    .map(Path::toString).sorted().forEach(matches::add);
        }
        return String.join("\n", matches);
    }

    private static Pattern compileGlob(String glob) {
        String[] parts = glob.split("\\*", -1);
        StringBuilder regex = new StringBuilder("^");
        for (int i = 0; i < parts.length; i++) {
            if (i > 0) regex.append(".*");
            regex.append(Pattern.quote(parts[i]));
        }
        return Pattern.compile(regex.append('$').toString());
    }
}

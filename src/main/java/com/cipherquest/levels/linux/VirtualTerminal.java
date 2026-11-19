package com.cipherquest.levels.linux;

import com.cipherquest.levels.ChallengeDefinition;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Base64;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public final class VirtualTerminal implements AutoCloseable {
    public static final String HOME = "/home/agent";
    private static final int MAX_COMMAND_LENGTH = 8_192;

    private Process process;
    private BufferedReader responses;
    private BufferedWriter requests;
    private String currentPath = HOME;
    private String lastOutput = "";
    private boolean targetExecuted;
    private boolean clearRequested;

    public synchronized void reset() {
        close();
        currentPath = HOME;
        lastOutput = "";
        targetExecuted = false;
        clearRequested = false;
    }

    public synchronized String execute(String command) {
        clearRequested = false;
        if (command == null || command.isBlank()) {
            return lastOutput = "";
        }
        if (command.length() > MAX_COMMAND_LENGTH) {
            return lastOutput = "Terminal: command exceeds " + MAX_COMMAND_LENGTH + " characters.";
        }

        try {
            if (process == null) {
                startWorker();
            }
            requests.write(Base64.getEncoder().encodeToString(command.getBytes(StandardCharsets.UTF_8)));
            requests.newLine();
            requests.flush();

            String[] reply = readResponse(6).split("\\t", -1);
            if (reply.length != 4) {
                throw new IOException("Invalid terminal reply");
            }
            String path = decode(reply[2]);
            String output = decode(reply[3]);
            currentPath = path;
            targetExecuted = "1".equals(reply[0]);
            clearRequested = "1".equals(reply[1]);
            return lastOutput = output;
        } catch (IOException | IllegalArgumentException e) {
            close();
            currentPath = HOME;
            targetExecuted = false;
            return lastOutput = "Terminal unavailable. Check Node.js 24+ and run npm ci --prefix terminal, "
                    + "then retry (the shell will restart in ~).";
        }
    }

    private void startWorker() throws IOException {
        ChallengeDefinition challenge = ChallengeDefinition.load(2);
        Path directory = Path.of(System.getProperty("cipherquest.terminal", "terminal")).toAbsolutePath().normalize();
        String node = System.getenv("CIPHERQUEST_NODE");
        ProcessBuilder worker = new ProcessBuilder(
                node == null || node.isBlank() ? "node" : node,
                "--permission", "--allow-fs-read=" + directory, "--max-old-space-size=128",
                directory.resolve("worker.mjs").toString(), HOME, challenge.required("target_binary"),
                challenge.required("task"));
        worker.directory(directory.toFile());
        worker.redirectError(ProcessBuilder.Redirect.INHERIT);
        worker.environment().remove("NODE_OPTIONS");
        process = worker.start();
        responses = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8));
        requests = new BufferedWriter(new OutputStreamWriter(process.getOutputStream(), StandardCharsets.UTF_8));
        if (!"READY".equals(readResponse(12))) {
            throw new IOException("Terminal did not start");
        }
    }

    private String readResponse(int timeoutSeconds) throws IOException {
        BufferedReader responseStream = responses;
        FutureTask<String> read = new FutureTask<>(responseStream::readLine);
        Thread.startVirtualThread(read);
        try {
            String reply = read.get(timeoutSeconds, TimeUnit.SECONDS);
            if (reply == null) {
                throw new EOFException("Terminal disconnected");
            }
            return reply;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Terminal request interrupted", e);
        } catch (ExecutionException | TimeoutException e) {
            throw new IOException("Terminal response unavailable", e);
        }
    }

    private static String decode(String value) {
        return new String(Base64.getDecoder().decode(value), StandardCharsets.UTF_8);
    }

    public synchronized boolean isTargetExecuted() { return targetExecuted; }
    public synchronized boolean isClearRequested() { return clearRequested; }
    public synchronized String getCurrentPath() { return currentPath; }
    public synchronized String getLastOutput() { return lastOutput; }

    @Override
    public synchronized void close() {
        if (process != null) {
            process.destroyForcibly();
            process = null;
        }
    }
}

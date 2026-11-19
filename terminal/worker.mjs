import { createInterface } from "node:readline";
import { Bash, InMemoryFs, defineCommand } from "just-bash";
import { seedMission } from "./mission.mjs";

const [home, target, objective] = process.argv.slice(2);
const fs = new InMemoryFs();
await seedMission(fs, home, target, objective);

let targetExecuted = false;
const clearance = defineCommand("cipherquest-clearance", async (_args, context) => {
  if (context.env.get("0") !== target) {
    return { stdout: "", stderr: "Start the mission program from its original path.\n", exitCode: 1 };
  }
  targetExecuted = true;
  return { stdout: "[+] ACCESS GRANTED • Checkpoint 2 cleared.\n", stderr: "", exitCode: 0 };
});
const uname = defineCommand("uname", async (args) => ({
  stdout: args.includes("-a")
    ? "Linux cipherquest-terminal 5.15.0 x86_64 GNU/Linux\n"
    : "Linux\n",
  stderr: "",
  exitCode: 0,
}));
const whoami = defineCommand("whoami", async () => ({ stdout: "agent\n", stderr: "", exitCode: 0 }));
const bash = new Bash({
  fs,
  cwd: home,
  env: { HOME: home, USER: "agent", LOGNAME: "agent", HOSTNAME: "cipherquest-terminal" },
  customCommands: [clearance, uname, whoami],
  executionLimits: { maxExecutionTimeMs: 1600, maxOutputBytes: 32768, maxCommandCount: 300 },
});

const encode = (value) => Buffer.from(value, "utf8").toString("base64");
let cwd = home;
let env;
process.stdout.write("READY\n");
const input = createInterface({ input: process.stdin, crlfDelay: Infinity });
for await (const line of input) {
  try {
    const command = Buffer.from(line, "base64").toString("utf8");
    const result = await bash.exec(command, { cwd, env });
    env = result.env;
    cwd = env?.PWD ?? cwd;
    const clear = result.stdout.includes("\u001b[2J");
    const output = (result.stdout + result.stderr).replace(/\u001b\[[0-?]*[ -\/]*[@-~]/g, "").trimEnd();
    process.stdout.write([+targetExecuted, +clear, encode(cwd), encode(output)].join("\t") + "\n");
  } catch {
    process.stdout.write([0, 0, encode(cwd), encode("Terminal: unable to execute this command.")].join("\t") + "\n");
  }
}

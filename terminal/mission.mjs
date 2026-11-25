export async function seedMission(fs, home, target, objective) {
  const targetDirectory = target.slice(0, target.lastIndexOf("/"));
  const targetName = target.slice(target.lastIndexOf("/") + 1);
  const files = {
    [home + "/readme.txt"]:
      [
        "SECURITY MEMO",
        "",
        "สำเนาบันทึกภารกิจอยู่ที่ ~/documents/mission.txt",
        "ตรวจสอบตำแหน่งไฟล์ล่าสุดได้ที่ /var/log/syslog",
      ].join("\n") + "\n",
    [home + "/notes.txt"]:
      [
        "NOTES",
        "ไฟล์โปรแกรมเป้าหมายยังคงอยู่ในเครื่องนี้",
        "ดู path ของไฟล์ที่ถูกบันทึกไว้ใน /var/log/syslog",
      ].join("\n") + "\n",
    [home + "/.bash_history"]: "cd /opt\nls -la\nls -la /var/log\n",
    [home + "/documents/mission.txt"]:
      ["MISSION BRIEFING", objective, "", "TARGET FILE: " + targetName].join(
        "\n",
      ) + "\n",
    "/etc/hostname": "cipherquest-terminal\n",
    "/etc/os-release": "NAME=CipherQuest Linux\nID=cipherquest\n",
    "/var/log/syslog":
      "systemd: security monitoring online\nkernel: " + target + "\n",
    [target]: "#!/bin/bash\ncipherquest-clearance\n",
  };
  for (const directory of [
    home + "/documents",
    "/etc",
    "/var/log",
    targetDirectory,
  ]) {
    await fs.mkdir(directory, { recursive: true });
  }
  for (const [path, content] of Object.entries(files)) {
    await fs.writeFile(path, content);
  }
  await fs.chmod(target, 0o755);
}

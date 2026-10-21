import sys, paramiko
sys.stdout.reconfigure(encoding="utf-8", errors="replace")

HOST = "188.40.89.176"
USER = "root"
PWD = "H54FSSnwvhTbih"

c = paramiko.SSHClient()
c.set_missing_host_key_policy(paramiko.AutoAddPolicy())
c.connect(HOST, username=USER, password=PWD, timeout=20, look_for_keys=False, allow_agent=False)

mode = sys.argv[1]
if mode == "run":
    cmd = " ".join(sys.argv[2:])
    stdin, stdout, stderr = c.exec_command(cmd)
    out = stdout.read().decode("utf-8", "replace")
    err = stderr.read().decode("utf-8", "replace")
    sys.stdout.write(out)
    if err:
        sys.stdout.write("\n[STDERR]\n" + err)
elif mode == "get":
    sftp = c.open_sftp()
    sftp.get(sys.argv[2], sys.argv[3])
    print("downloaded " + sys.argv[2])
elif mode == "put":
    sftp = c.open_sftp()
    sftp.put(sys.argv[2], sys.argv[3])
    print("uploaded -> " + sys.argv[3])
c.close()

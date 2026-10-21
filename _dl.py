import paramiko, os

LOCAL = "C:\\Users\\Cry\\Desktop\\SkyHand\\Plugins\\SHGambleBar\\_ref"
os.makedirs(LOCAL, exist_ok=True)

c = paramiko.SSHClient()
c.set_missing_host_key_policy(paramiko.AutoAddPolicy())
c.connect("188.40.89.176", username="root", password="H54FSSnwvhTbih", look_for_keys=False, allow_agent=False)
s = c.open_sftp()

d = "/skyhand/serveurs/opale/plugins/GambleBarPro"
print(s.listdir(d))
for f in s.listdir(d):
    s.get(d + "/" + f, os.path.join(LOCAL, f))
    print("ok", f)
s.get("/skyhand/serveurs/opale/plugins/GambleBarPro-2.0.6.jar", os.path.join(LOCAL, "GambleBarPro-2.0.6.jar"))
print("ok jar")
c.close()

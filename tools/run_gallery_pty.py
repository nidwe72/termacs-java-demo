#!/usr/bin/env python3
"""Launch WidgetGallery on an 80x30 pseudo-terminal, assert every P5 control
renders, then quit. Self-contained smoke test (exit 0 = pass). Finds the
termacs-java binding at $TERMACS_JAVA, else the sibling ../termacs-java
(override the lib with $TERMACS_JNI)."""
import os, pty, fcntl, termios, struct, time, select, sys, signal, re

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
JAVA_HOME = os.environ.get("JAVA_HOME", "/usr/lib/jvm/java-25-openjdk-amd64")
BINDING = os.environ.get("TERMACS_JAVA", os.path.join(os.path.dirname(ROOT), "termacs-java"))
JNI = os.environ.get("TERMACS_JNI", os.path.join(BINDING, "build/libtermacsjni.so"))
CP = os.path.join(BINDING, "build/classes") + ":" + os.path.join(ROOT, "build/classes")

m, s = os.openpty()
fcntl.ioctl(s, termios.TIOCSWINSZ, struct.pack("HHHH", 30, 80, 0, 0))
pid = os.fork()
if pid == 0:
    os.setsid()
    try: fcntl.ioctl(s, termios.TIOCSCTTY, 0)
    except OSError: pass
    os.dup2(s, 0); os.dup2(s, 1); os.dup2(s, 2)
    env = dict(os.environ, TERM="xterm-256color", JAVA_HOME=JAVA_HOME, COLUMNS="80", LINES="30")
    os.execve(JAVA_HOME + "/bin/java",
              ["java", "-Dtermacs.jni=" + JNI, "--enable-native-access=ALL-UNNAMED",
               "-cp", CP, "sciens.termacs.demo.WidgetGallery"], env)
    os._exit(127)
os.close(s)
fd = m
buf = bytearray()

def pump(seconds):
    end = time.time() + seconds
    while time.time() < end:
        r, _, _ = select.select([fd], [], [], 0.05)
        if r:
            try: buf.extend(os.read(fd, 65536))
            except OSError: return

pump(2.5)
os.kill(pid, signal.SIGWINCH)   # force tvision to read the winsize
pump(1.2)
os.write(fd, b"hi\r")           # type a note + Enter -> appends a line
pump(0.6)
text = bytes(buf).decode("utf-8", "replace")
os.write(fd, b"\x1b[21~")       # F10 -> File menu
pump(0.3)
os.write(fd, b"\r")             # Quit
pump(0.3)
os.write(fd, b"\r")             # confirm Yes
pump(1.0)
try: os.close(fd)
except OSError: pass
_, status = os.waitpid(pid, 0)

plain = re.sub(r"\x1b\[[0-9;?]*[A-Za-z]|\x1b[()][AB0]|\x1b[=>]", "", text)
checks = {
    "title":     "Widget Gallery" in plain,
    "buttons":   "[ Info ]" in plain and "[ Quit ]" in plain,
    "checkbox":  "Remember me" in plain,
    "radio":     "(*) M" in plain,
    "multi":     "Bold" in plain and "Italic" in plain,
    "combo":     "Choose..." in plain,
    "progress":  "%" in plain,
    "frame":     "Notes" in plain,
    "scroll":    "row 1 of 12" in plain,
}
for k, v in checks.items():
    print(f"  {k:9} {'OK' if v else 'MISSING'}")
print("exit:", os.waitstatus_to_exitcode(status) if os.WIFEXITED(status) else "signal")
sys.exit(0 if all(checks.values()) else 1)

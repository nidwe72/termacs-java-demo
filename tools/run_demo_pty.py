#!/usr/bin/env python3
"""Launch the Java TaskDemo on a real (pseudo) terminal sized 80x24, type a task,
then drive File-Quit, and capture the rendered frames. Proves the demo runs on a
real terminal and paints.

Finds the termacs-java binding via $TERMACS_JAVA, else the sibling ../termacs-java
(build it first: cmake -S . -B build && cmake --build build; javac the classes).
Override the JNI lib with $TERMACS_JNI."""
import os, pty, fcntl, termios, struct, time, select, sys, signal

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))   # the termacs-java-demo repo root
JAVA_HOME = os.environ.get("JAVA_HOME", "/usr/lib/jvm/java-25-openjdk-amd64")
BINDING = os.environ.get("TERMACS_JAVA", os.path.join(os.path.dirname(ROOT), "termacs-java"))
JNI = os.environ.get("TERMACS_JNI", os.path.join(BINDING, "build/libtermacsjni.so"))
CP = os.path.join(BINDING, "build/classes") + ":" + os.path.join(ROOT, "build/classes")

m, s = os.openpty()
fcntl.ioctl(s, termios.TIOCSWINSZ, struct.pack("HHHH", 24, 80, 0, 0))  # set size BEFORE exec
pid = os.fork()
if pid == 0:  # child -> the demo, with the pty slave as a controlling tty
    os.setsid()
    try: fcntl.ioctl(s, termios.TIOCSCTTY, 0)
    except OSError: pass
    os.dup2(s, 0); os.dup2(s, 1); os.dup2(s, 2)
    env = dict(os.environ, TERM="xterm-256color", JAVA_HOME=JAVA_HOME, COLUMNS="80", LINES="24")
    os.execve(JAVA_HOME + "/bin/java",
              ["java", "-Dtermacs.jni=" + JNI, "--enable-native-access=ALL-UNNAMED",
               "-cp", CP, "sciens.termacs.demo.TaskDemo"], env)
    os._exit(127)
os.close(s)
fd = m

# parent drives the pty
captured = bytearray()
def pump(seconds):
    end = time.time() + seconds
    while time.time() < end:
        r, _, _ = select.select([fd], [], [], 0.05)
        if r:
            try: captured.extend(os.read(fd, 65536))
            except OSError: return False
    return True

def send(b):
    os.write(fd, b); pump(0.3)   # drain while we type

pump(2.5)                 # JVM + tvision startup
os.kill(pid, signal.SIGWINCH)   # force tvision to (re)read the 80x24 winsize
pump(0.8)                 # first real paint
for ch in b"buy milk":    # type a task
    send(bytes([ch]))
send(b"\r")               # Enter -> submitted -> add task
pump(0.8)
snapshot = bytes(captured)  # frame with the task added
send(b"\x1b[21~")         # F10 -> open File menu
send(b"\r")               # activate "Quit" -> confirm dialog
send(b"\r")               # Enter -> Yes -> app.quit
pump(1.5)
try: os.close(fd)
except OSError: pass
_, status = os.waitpid(pid, 0)

open("/tmp/demo_pty.raw","wb").write(bytes(captured))
text = bytes(captured).decode("utf-8", "replace")
# strip ANSI escapes for a readable check
import re
plain = re.sub(r"\x1b\[[0-9;?]*[A-Za-z]|\x1b[()][AB0]|\x1b[=>]", "", text)
print("=== exit status:", status, "===")
ok_title = "Task Demo" in plain
ok_task  = "buy milk" in plain
ok_menu  = "File" in plain and "Help" in plain
print("rendered title 'Task Demo':", ok_title)
print("rendered typed task 'buy milk':", ok_task)
print("rendered menus File/Help:", ok_menu)
print("clean exit:", os.WIFEXITED(status))
sys.exit(0 if (ok_title and ok_task and ok_menu) else 1)

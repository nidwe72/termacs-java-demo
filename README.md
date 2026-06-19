# termacs-java-demo

A small **task-list demo** for termacs — exercises the spine of the API: a menu
bar, nested layout, several widgets, signals, async dialogs, a timer, theming,
and tab focus, running on a real Linux terminal. It also doubles as a manual
smoke test ("does it launch and paint?").

Depends only on the [termacs-java](https://github.com/nidwe72/termacs-java)
binding (which in turn pulls in termacs-core).

## Build & run

Build the [termacs-java](https://github.com/nidwe72/termacs-java) binding first,
then point at its `build/` outputs (here `$JAVA` = the binding checkout):

```bash
javac --release 17 -cp "$JAVA/build/classes" -d build/classes \
    src/main/java/sciens/termacs/demo/TaskDemo.java

java -Dtermacs.jni="$JAVA/build/libtermacsjni.so" \
    --enable-native-access=ALL-UNNAMED \
    -cp "$JAVA/build/classes:build/classes" \
    sciens.termacs.demo.TaskDemo
```

The window fills the whole terminal and reflows on resize. Add a task with the
input + **Add**/Enter; Enter on a row removes it (confirm dialog); quit via
**File ▸ Quit**.

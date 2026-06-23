package sciens.termacs.demo;

import sciens.termacs.*;
import java.time.LocalTime;

/** termacs Java/Linux demo: a tiny task list (spec §13). Runs on a real terminal. */
public final class TaskDemo {
    public static void main(String[] args) {
        Application app = new Application();          // real terminal (tvision)
        app.setTheme(Theme.PHOSPHOR_HARMONY);         // green/amber/gray (§5.6)
        app.setControlStyle(ControlStyle.BRACKETS);   // §5.12

        Window win = app.createWindow("termacs — Task Demo");
        win.resize(0, 0);   // {0,0} ⇒ fill the whole terminal

        MenuBar mb = win.setMenuBar();
        Menu file = mb.addMenu("File");
        Menu help = mb.addMenu("Help");

        VBox body = win.setContentVBox();
        body.setPadding(1);
        body.setSpacing(1);

        HBox row = body.addHBox();
        row.addLabel("Task:");
        LineEdit input = row.addLineEdit();
        input.setSizing(Axis.HORIZONTAL, Sizing.of().preferred(20).stretch(1));
        Button add = row.addButton("Add");

        ListView tasks = body.addListView();
        tasks.setSizing(Axis.VERTICAL, Sizing.of().min(3).stretch(1));

        StatusBar status = win.setStatusBar();

        Runnable addTask = () -> {
            String t = input.text();
            if (t.isEmpty()) return;
            tasks.addItem(t);
            input.setText("");
            input.setFocus();
            status.setText(tasks.count() + " task(s)");
        };
        add.onClicked(addTask);
        input.onSubmitted(addTask);

        tasks.onActivated(rowIdx ->
            app.dialogs().confirm(win, "Remove \"" + tasks.itemAt(rowIdx) + "\"?", yes -> {
                if (yes) { tasks.removeItem(rowIdx); status.setText(tasks.count() + " task(s)"); }
            }));

        file.addItem("Quit", () ->
            app.dialogs().confirm(win, "Quit termacs demo?", yes -> { if (yes) app.quit(0); }));
        help.addItem("About", () ->
            app.dialogs().info(win, "termacs demo\nTurbo Vision engine, modern API."));

        // clock in the status bar — timer callback runs on the UI thread
        app.startTimer(1000, () -> status.setRightText(LocalTime.now().withNano(0).toString()));

        status.setText("0 task(s)");
        win.show();
        input.setFocus();
        System.exit(app.run());
    }
}

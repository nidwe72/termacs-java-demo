package sciens.termacs.demo;

import sciens.termacs.*;
import java.time.LocalTime;

/** Showcases the full P5 widget set (§5.10) on a real terminal: button
 *  variants, CheckBox, OptionGroup (radio + multi), ComboBox, ProgressBar,
 *  TextArea in a Frame, and a ScrollView — plus menus, dialogs, theme toggle. */
public final class WidgetGallery {
    public static void main(String[] args) {
        Application app = new Application();
        final Theme[] theme = { Theme.DARK };
        app.setTheme(theme[0]);

        Window win = app.createWindow("termacs — Widget Gallery");
        win.resize(0, 0);

        MenuBar mb = win.setMenuBar();
        Menu file = mb.addMenu("File");
        Menu view = mb.addMenu("View");
        Menu help = mb.addMenu("Help");
        StatusBar status = win.setStatusBar();

        VBox body = win.setContentVBox();
        body.setPadding(1);
        body.setSpacing(0);

        // input row + live echo
        HBox inrow = body.addHBox();
        inrow.addLabel("Note:");
        LineEdit input = inrow.addLineEdit();
        input.setSizing(Axis.HORIZONTAL, Sizing.of().preferred(16).stretch(1));
        Button add = inrow.addButton("Add");
        add.setVariant(ButtonVariant.PRIMARY);
        add.setDefault(true);

        // button variants
        HBox brow = body.addHBox();
        Button info    = brow.addButton("Info");
        Button confirm = brow.addButton("Confirm");
        Button themeB  = brow.addButton("Theme");
        Button quiet   = brow.addButton("Quiet");   quiet.setVariant(ButtonVariant.QUIET);
        Button quit    = brow.addButton("Quit");    quit.setVariant(ButtonVariant.DANGER);

        // checkbox
        CheckBox remember = body.addCheckBox("Remember me");
        remember.setChecked(true);

        // radio (single) + multi
        HBox sizeRow = body.addHBox();
        sizeRow.addLabel("Size:");
        OptionGroup size = sizeRow.addRadioGroup();
        size.setOptions("S", "M", "L");
        size.setOrientation(Axis.HORIZONTAL);
        size.setSelectedIndex(1);

        HBox styleRow = body.addHBox();
        styleRow.addLabel("Style:");
        OptionGroup style = styleRow.addCheckGroup();
        style.setOptions("Bold", "Italic", "Under");
        style.setOrientation(Axis.HORIZONTAL);

        // combo
        HBox prioRow = body.addHBox();
        prioRow.addLabel("Priority:");
        ComboBox prio = prioRow.addComboBox();
        prio.setOptions("Low", "Medium", "High");
        prio.setPlaceholder("Choose...");

        // progress bar (animated)
        HBox progRow = body.addHBox();
        progRow.addLabel("Progress:");
        ProgressBar pb = progRow.addProgressBar();
        pb.setSizing(Axis.HORIZONTAL, Sizing.of().preferred(16).stretch(1));

        // text area inside a frame
        Frame notes = body.addFrame("Notes (Add appends a line)");
        TextArea ta = notes.addTextArea();
        ta.setSizing(Axis.VERTICAL, Sizing.of().min(3).stretch(1));
        ta.setText("editable, multi-line.");

        // scroll view over tall content
        Frame more = body.addFrame("Scroll (↑/↓)");
        ScrollView sv = more.addScrollView();
        sv.setSizing(Axis.VERTICAL, Sizing.of().min(3).stretch(1));
        VBox tall = sv.addVBox();
        for (int i = 1; i <= 12; i++) tall.addLabel("row " + i + " of 12");

        // wiring
        Runnable addNote = () -> {
            String t = input.text();
            if (t.isEmpty()) return;
            ta.appendLine(t);
            input.setText("");
            status.setText("added: " + t);
        };
        add.onClicked(addNote);
        input.onSubmitted(addNote);

        info.onClicked(() -> app.dialogs().info(win, "termacs Widget Gallery\nAll P5 controls, live."));
        confirm.onClicked(() -> app.dialogs().confirm(win, "Are you sure?",
            yes -> status.setText(yes ? "confirmed" : "cancelled")));
        Runnable toggle = () -> { theme[0] = (theme[0] == Theme.DARK) ? Theme.LIGHT : Theme.DARK; app.setTheme(theme[0]); status.setText("theme: " + theme[0]); };
        themeB.onClicked(toggle);
        quiet.onClicked(() -> status.setText("quiet clicked"));
        quit.onClicked(() -> app.dialogs().confirm(win, "Quit the gallery?", yes -> { if (yes) app.quit(0); }));

        remember.onToggled(on -> status.setText("remember: " + on));
        size.onSelectionChanged(i -> status.setText("size: " + i));
        style.onSelectionChanged(i -> status.setText("style[" + i + "]: " + style.isSelected(i)));
        prio.onSelectionChanged(i -> status.setText("priority: " + prio.selectedText()));

        file.addItem("Quit", () -> app.dialogs().confirm(win, "Quit?", yes -> { if (yes) app.quit(0); }));
        view.addItem("Toggle theme", toggle);
        help.addItem("About", () -> app.dialogs().info(win, "termacs Widget Gallery"));

        // animated progress + clock
        int[] p = {0};
        app.startTimer(300, () -> { p[0] = (p[0] + 4) % 104; pb.setValue(Math.min(p[0], 100)); });
        app.startTimer(1000, () -> status.setRightText(LocalTime.now().withNano(0).toString()));

        status.setText("Tab=focus · in fields: type, Shift/Ctrl+arrows select, Ctrl+C/X/V · F10=menu");
        win.show();
        input.setFocus();
        System.exit(app.run());
    }
}

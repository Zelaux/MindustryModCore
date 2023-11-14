package mmc.core;

import arc.ApplicationListener;
import arc.Core;
import arc.Input;
import arc.KeyBinds;
import arc.func.Cons;
import arc.graphics.Color;
import arc.input.KeyCode;
import arc.scene.ui.Dialog;
import arc.scene.ui.TextField;
import arc.scene.ui.layout.Collapser;
import arc.util.Disposable;
import arc.util.Strings;
import arc.util.Time;
import mindustry.Vars;
import mindustry.ui.Styles;

import static arc.Core.settings;
import static mindustry.Vars.headless;
import static mmc.ModVars.modLog;

public class ModUI  implements Disposable, ApplicationListener {

    private boolean inited=false;
    public ModUI(KeyBinds.KeyBind[] modBindings) {
        setKeybinds(modBindings);
    }

    public ModUI(){
    }

    protected void setKeybinds(KeyBinds.KeyBind... modBindings){
        Time.mark();
        KeyBinds.KeyBind[] keyBinds = Core.keybinds.getKeybinds();
        KeyBinds.KeyBind[] defs = new KeyBinds.KeyBind[keyBinds.length + modBindings.length];
        for (int i = 0; i < defs.length; i++) {
            if (i<keyBinds.length){
                defs[i]=keyBinds[i];
            } else {
                defs[i]= modBindings[i-keyBinds.length];
            }
        }
        modLog("Time to combine arrays: @ms",Time.elapsed());
        Core.keybinds.setDefaults(defs);
        settings.load();
    }

    @Override
    public void init() {
        if (headless) return;
        inited=true;
    }

    @Override
    public void dispose() {
        if (!inited){
        }
    }
    public static void showExceptionDialog(Throwable t) {
        showExceptionDialog("", t);
    }
    public static Dialog getInfoDialog(String title, String subTitle, String message, Color lineColor) {
        return new Dialog(title) {{
            setFillParent(true);
            cont.margin(15.0F);
            cont.add(subTitle);
            cont.row();
            cont.image().width(300.0F).pad(2.0F).height(4.0F).color(lineColor);
            cont.row();
            cont.add(message).pad(2.0F).growX().wrap().get().setAlignment(1);
            cont.row();
            cont.button("@ok", this::hide).size(120.0F, 50.0F).pad(4.0F);
            closeOnBack();
        }};
    }
    public static void showExceptionDialog(final String text, final Throwable exc) {
        new Dialog("") {{
            String message = Strings.getFinalMessage(exc);
            setFillParent(true);
            cont.margin(15.0F);
            cont.add("@error.title").colspan(2);
            cont.row();
            cont.image().width(300.0F).pad(2.0F).colspan(2).height(4.0F).color(Color.scarlet);
            cont.row();
            cont.add((text.startsWith("@") ? Core.bundle.get(text.substring(1)) : text) + (message == null ? "" : "\n[lightgray](" + message + ")")).colspan(2).wrap().growX().center().get().setAlignment(1);
            cont.row();
            Collapser col = new Collapser((base) -> {
                base.pane((t) -> {
                    t.margin(14.0F).add(Strings.neatError(exc)).color(Color.lightGray).left();
                });
            }, true);
            cont.button("@details", Styles.togglet, col::toggle).size(180.0F, 50.0F).checked((b) -> {
                return !col.isCollapsed();
            }).fillX().right();
            cont.button("@ok", this::hide).size(110.0F, 50.0F).fillX().left();
            cont.row();
            cont.add(col).colspan(2).pad(2.0F);
            closeOnBack();
        }}.show();
    }
    public static void showTextInput(String title, String text, String def, Cons<String> confirmed) {
        showTextInput(title, text, 32, def, confirmed);
    }

    public static void showTextInput(String titleText, String text, int textLength, String def, Cons<String> confirmed) {
        showTextInput(titleText, text, textLength, def, (t,c)->{return true;}, confirmed);
    }
    public static void showTextInput(final String titleText, final String dtext, final int textLength, final String def, final TextField.TextFieldFilter filter, final Cons<String> confirmed) {
        if (Vars.mobile) {
            Core.input.getTextInput(new Input.TextInput() {
                {
                    title = titleText.startsWith("@") ? Core.bundle.get(titleText.substring(1)) : titleText;
                    text = def;
                    numeric=filter== TextField.TextFieldFilter.digitsOnly;
//                    numeric = inumeric;
                    maxLength = textLength;
                    accepted = confirmed;
                }
            });
        } else {
            new Dialog(titleText) {{
                cont.margin(30.0F).add(dtext).padRight(6.0F);
                TextField field = cont.field(def, (t) -> {
                }).size(330.0F, 50.0F).get();
                field.setFilter((f, c) -> {
                    return field.getText().length() < textLength && filter.acceptChar(f, c);
                });
                buttons.defaults().size(120.0F, 54.0F).pad(4.0F);
                buttons.button("@cancel", this::hide);
                buttons.button("@ok", () -> {
                    confirmed.get(field.getText());
                    hide();
                }).disabled((b) -> {
                    return field.getText().isEmpty();
                });
                keyDown(KeyCode.enter, () -> {
                    String text = field.getText();
                    if (!text.isEmpty()) {
                        confirmed.get(text);
                        hide();
                    }

                });
                keyDown(KeyCode.escape, this::hide);
                keyDown(KeyCode.back, this::hide);
                show();
                Core.scene.setKeyboardFocus(field);
                field.setCursorPosition(def.length());
            }};
        }

    }
}

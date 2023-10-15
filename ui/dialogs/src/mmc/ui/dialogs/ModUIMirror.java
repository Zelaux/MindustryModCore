package mmc.ui.dialogs;

import arc.*;
import arc.func.*;
import arc.input.*;
import arc.scene.ui.*;
import mindustry.*;

class ModUIMirror{
    public static void showTextInput(String title, String text, String def, Cons<String> confirmed){
        showTextInput(title, text, 32, def, confirmed);
    }

    public static void showTextInput(String titleText, String text, int textLength, String def, Cons<String> confirmed){
        showTextInput(titleText, text, textLength, def, (t, c) -> {
            return true;
        }, confirmed);
    }

    public static void showTextInput(final String titleText, final String dtext, final int textLength, final String def, final TextField.TextFieldFilter filter, final Cons<String> confirmed){
        if(Vars.mobile){
            Core.input.getTextInput(new Input.TextInput(){
                {
                    title = titleText.startsWith("@") ? Core.bundle.get(titleText.substring(1)) : titleText;
                    text = def;
                    numeric = filter == TextField.TextFieldFilter.digitsOnly;
//                    numeric = inumeric;
                    maxLength = textLength;
                    accepted = confirmed;
                }
            });
        }else{
            new Dialog(titleText){{
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
                    if(!text.isEmpty()){
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

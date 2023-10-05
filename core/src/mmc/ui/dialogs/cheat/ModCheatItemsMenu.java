package mmc.ui.dialogs.cheat;

import arc.Core;
import arc.func.Boolf;
import arc.func.Cons;
import arc.graphics.Color;
import arc.input.KeyCode;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import arc.util.Strings;
import arc.util.Structs;
import mindustry.Vars;
import mindustry.gen.Icon;
import mindustry.gen.Tex;
import mindustry.type.Item;
import mindustry.type.ItemStack;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.BaseDialog;
import mindustry.world.blocks.storage.CoreBlock;
import mmc.core.ModUI;

import java.util.Iterator;

public class ModCheatItemsMenu extends BaseDialog {
    private Runnable hider;
    private final Cons<Seq<ItemStack>> confirm=(itemStacks -> {
        itemStacks.each(itemStack -> {
            CoreBlock.CoreBuild core= Vars.player.team().core();
            core.items.set(itemStack.item,itemStack.amount);

        });
    });
    private Runnable resetter;
    private Seq<ItemStack> stacks = new Seq();
    private Seq<ItemStack> originalStacks = new Seq();
    private Boolf<Item> validator = (i) -> {
        return true;
    };
    private Table items;
    private int capacity;

    public ModCheatItemsMenu() {
        super("Items manager");
        this.setFillParent(true);
        this.keyDown((key) -> {
            if (key == KeyCode.escape || key == KeyCode.back) {
                Core.app.post(this::hide);
            }

        });
        this.cont.pane((t) -> {
            this.items = t.margin(10.0F);
        }).left();
        this.shown(this::setup);
        this.hidden(() -> {
            confirm.get(this.stacks);
            if (this.hider != null) {
                this.hider.run();
            }

        });
        this.buttons.button("@back", Icon.left, this::hide).size(210.0F, 64.0F);
        this.buttons.button("@settings.reset", Icon.refresh, () -> {
//            this.resetter.run();
            this.reseed();
//            this.updater.run();
            this.setup();
        }).size(210.0F, 64.0F);
    }

    public void show(Runnable reseter,  Runnable hider) {
        Seq<ItemStack> stacks=new Seq<>();
        CoreBlock.CoreBuild core= Vars.player.team().core();
        if (core==null){
            ModUI.getInfoDialog("@error","@error.title","you haven't core", Color.scarlet);
            return;
        }
        core.items.each((item,amount)->{
            stacks.add(new ItemStack(item,amount));
        });
        this.capacity=Integer.MAX_VALUE;
        this.originalStacks = stacks;
        this.validator = (i)->true;
        this.resetter = reseter;
        this.capacity = capacity;
        this.hider = hider;
        this.reseed();
        this.show();
    }

    void setup() {
        this.items.clearChildren();
        this.items.left();
        float bsize = 40.0F;
        int i = 0;
        Iterator<ItemStack> var3 = this.stacks.iterator();

        while(true) {
            do {
                if (!var3.hasNext()) {
                    return;
                }

                ItemStack stack = var3.next();
                this.items.table(Tex.pane, (t) -> {
                    t.margin(4.0F).marginRight(8.0F).left();
                    t.button("-", Styles.cleart, () -> {
                        stack.amount = Math.max(stack.amount - this.step(stack.amount), 0);
                    }).size(bsize);
                    t.button("+", Styles.cleart, () -> {
                        stack.amount = Math.min(stack.amount + this.step(stack.amount), this.capacity);
                    }).size(bsize);
                    t.button(Icon.pencil, Styles.cleari, () -> {
                        Vars.ui.showTextInput("@configure", stack.item.localizedName, 10, String.valueOf(stack.amount), true, (str) -> {
                            if (Strings.canParsePositiveInt(str)) {
                                int amount = Strings.parseInt(str);
                                if (amount >= 0 && amount <= this.capacity) {
                                    stack.amount = amount;
                                    return;
                                }
                            }

                            Vars.ui.showInfo(Core.bundle.format("configure.invalid", this.capacity));
                        });
                    }).size(bsize);
                    t.image(stack.item.uiIcon).size(24.0F).padRight(4.0F).padLeft(4.0F);
                    t.label(() -> {
                        return String.valueOf(stack.amount);
                    }).left().width(90.0F);
                }).pad(2.0F).left().fillX();
                ++i;
            } while(i % 2 != 0 && (!Vars.mobile || !Core.graphics.isPortrait()));

            this.items.row();
        }
    }

    private void reseed() {
        this.stacks = this.originalStacks.map(ItemStack::copy);
        this.stacks.addAll(Vars.content.items().select((i) -> {
            return this.validator.get(i) && !this.stacks.contains((stack) -> {
                return stack.item == i;
            });
        }).map((i) -> {
            return new ItemStack(i, 0);
        }));
        this.stacks.sort(Structs.comparingInt((s) -> {
            return s.item.id;
        }));
    }

    private int step(int amount) {
        if (amount < 1000) {
            return 100;
        } else if (amount < 2000) {
            return 200;
        } else {
            return amount < 5000 ? 500 : 1000;
        }
    }

}

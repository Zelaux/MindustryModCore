package tests;

import arc.Events;
import mindustry.game.EventType;

public class Test2 {
    public static void main(String[] args) {
        Events.run(EventType.Trigger.update,()-> System.out.println("update"));
        Events.run(EventType.Trigger.draw,()-> System.out.println("draw"));

        Events.fire(EventType.Trigger.class);
        Events.fire(EventType.Trigger.update);
        Events.fire(EventType.Trigger.draw);

    }
}

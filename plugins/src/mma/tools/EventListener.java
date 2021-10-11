package mma.tools;

import arc.Events;
import arc.struct.Seq;

public class EventListener {
    final String commandName;
    boolean building = false;
    private Object[] objects = null;
    private Seq<Object> objectSeq = new Seq<>();

    public EventListener(String commandName) {
        this.commandName = commandName;
    }

    public boolean valid(Object[] objects) {
        if (commandName.equals(objects[0])) {
            this.objects = objects;
            return true;
        }
        return false;
    }

    public <T> T parameter(String name) {
        for (int i = 1; i < objects.length - 1; i += 2) {
            if (name.equals(objects[i])) {
                return (T) objects[i + 1];
            }
        }
        return null;
    }

    public <T> void addParameter(String name, T parameter) {
        objectSeq.add(name, parameter);
    }

    public void fire() {
        Seq<Object> objects = objectSeq.copy();
        objects.insert(0, commandName);
        Events.fire(objects.toArray(Object.class));
    }

}

package mma.tools;

import arc.Events;
import arc.struct.ObjectMap;
import arc.struct.Seq;

public class EventSender {
    public   final String commandName;
    private ObjectMap<String,Object> parametersMap = new ObjectMap<>();

    public EventSender(String commandName) {
        this.commandName = commandName;
    }
    public EventSender(EventReceiver receiver) {
        this(receiver.commandName);
    }

    public <T> void setParameter(String name, T parameter) {
        parametersMap.put(name, parameter);
    }

    public void fire() {
        Seq<Object> objects = new Seq<>();
        objects.add( commandName);
        for (ObjectMap.Entry<String, Object> entry : parametersMap) {
            objects.add(entry.key,entry.value);
        }
        Events.fire(objects.toArray(Object.class));
    }
}

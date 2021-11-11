package mma.tools;

import arc.*;
import arc.func.*;
import arc.struct.*;

public class EventReceiver{
    public final String commandName;
    private final ObjectMap<String, Object> parametersMap = new ObjectMap<>();

    public EventReceiver(EventSender sender){
        this(sender.commandName);
    }

    public EventReceiver(String commandName){
        this.commandName = commandName;
    }

    public boolean valid(Object[] objects){
        parametersMap.clear();
        if(commandName.equals(objects[0]) && (objects.length - 1) % 2 == 0){
            try{

                boolean valid = true;
                for(int i = 1; i < objects.length - 1 && valid; i += 2){
                    valid &= objects[i] instanceof String;
                    valid &= objects.length - 1 > i;
                }
                if(!valid){
                    throw new UnknownObjects();
                }
                parametersMap.clear();
                for(int i = 1; i < objects.length; i += 2){
                    parametersMap.put((String)objects[i], objects[i + 1]);
                }
                return true;
            }catch(UnknownObjects e){

            }
        }
        return false;
    }

    public boolean hasParameter(String name, Class<?> type){
        if(!parametersMap.containsKey(name)) return false;
        Object object = parametersMap.get(name);
        return (type == null || type.isInstance(object));
    }

    public <T> T getParameter(String name){
        return (T)parametersMap.getNull(name);
    }

    public void post(Cons<EventReceiver> cons){
        Events.on(Object[].class, objects1 -> {
            if(valid(objects1)){
                try{
                    cons.get(this);
                }catch(Exception exception){
                    exception.printStackTrace();
                }
            }
        });
    }

    private static class UnknownObjects extends Exception{

    }
}

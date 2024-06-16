package mmc.ui.tiledStructures;

import arc.func.*;
import arc.graphics.*;
import arc.math.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.*;
import arc.util.serialization.*;
import arc.util.serialization.Json.*;
import mindustry.*;
import mindustry.game.MapObjectives.*;
import mindustry.graphics.*;
import mindustry.io.*;
import mmc.ui.tiledStructures.TiledStructures.TiledStructure.*;
import mmc.ui.tiledStructures.TiledStructuresCanvas.StructureTilemap.*;

import java.lang.annotation.*;
import java.util.*;

import static mindustry.Vars.net;

@SuppressWarnings("rawtypes")
public class TiledStructures implements
    Iterable<TiledStructures.TiledStructure>,
        Eachable<TiledStructures.TiledStructure>,
        JsonSerializable{
    public transient final Seq<Prov<? extends TiledStructure>> allObjectiveTypes;

    /**
     * All objectives the executor contains. Do not modify directly, ever!
     * @see #eachRunning(Cons)
     */
    public Seq<TiledStructure> all = new Seq<>(4);
    /** @see #checkChanged() */
    protected transient boolean changed;

    public TiledStructures(Seq<Prov<? extends TiledStructure>> allObjectiveTypes){
        this.allObjectiveTypes = allObjectiveTypes;
    }

    private TiledStructures(){
        this(new Seq<>());
    }

    @SafeVarargs
    public final void registerStructures(Prov<? extends TiledStructure>... providers){
        for(var prov : providers){
            allObjectiveTypes.add(prov);

            TiledStructure instance = prov.get();
            Class<? extends TiledStructure> type = instance.getClass();
            JsonIO.classTag(Strings.camelize(instance.name()), type);
            JsonIO.classTag(instance.name(), type);
        }
    }

    /** Adds all given objectives to the executor as root objectives. */
    public void add(TiledStructure... objectives){
        for(var objective : objectives) flatten(objective);
    }

    /** Recursively adds the objective and its children. */
    private void flatten(TiledStructure objective){
        for(var child : objective.children) flatten((TiledStructure)child);

        objective.children.clear();
        all.add(objective);
    }

    /** Updates all objectives this executor contains. */
    public void update(){
        eachRunning(obj -> {

            //objectives cannot get completed on the client, but they do try to update for timers and such
            if(obj.update() && !net.client()){
                obj.completed = true;
                obj.done();
            }

            changed |= obj.changed;
            obj.changed = false;
        });
    }

    /** @return True if map rules should be synced. Reserved for {@link Vars#logic}; do not invoke directly! */
    public boolean checkChanged(){
        boolean has = changed;
        changed = false;

        return has;
    }

    /** @return Whether there are any qualified objectives at all. */
    public boolean any(){
        return all.count(TiledStructure::qualified) > 0;
    }

    public void clear(){
        if(all.size > 0) changed = true;
        all.clear();
    }

    /** Iterates over all qualified in-map objectives. */
    public void eachRunning(Cons<TiledStructure> cons){
        all.each(TiledStructure::qualified, cons);
    }

    /** Iterates over all qualified in-map objectives, with a filter. */
    public <T extends TiledStructure> void eachRunning(Boolf<? super TiledStructure> pred, Cons<T> cons){
        all.each(obj -> obj.qualified() && pred.get(obj), cons);
    }

    @Override
    public Iterator<TiledStructure> iterator(){
        return all.iterator();
    }

    @Override
    public void each(Cons<? super TiledStructure> cons){
        all.each(cons);
    }

    private String structureToString(TiledStructure<?> structure){
//        return structure.editorX + "_" + structure.editorY;
        return String.valueOf(all.indexOf(structure));
    }

    @Override
    public void write(Json json){
//        json.writeObjectStart();
        json.writeValue("all", all);
        json.writeObjectStart("wires");
        for(int i = 0; i < all.size; i++){
            TiledStructure<?> structure = all.get(i);
            String name = structureToString(structure);
            if(name.equals("-1")){
                Log.err("Cannot save wires for @(@, @)", structure.typeName(), structure.editorX, structure.editorY);
                continue;
            }
            json.writeArrayStart(name);
            for(ConnectionWire<?> inputWire : structure.inputWires){
                if(inputWire.obj == null) continue;
                String value = structureToString(inputWire.obj);
                if(value.equals("-1")){
                    Log.err("Cannot save wire with @(@, @) for @(@, @)", inputWire.obj.typeName(), inputWire.obj.editorX, inputWire.obj.editorY, structure.typeName(), structure.editorX, structure.editorY);
                    continue;
                }
                json.writeObjectStart();
                json.writeValue("obj", value);
                json.writeValue("input", inputWire.input);
                json.writeValue("output", inputWire.parentOutput);
                json.writeObjectEnd();
            }
            json.writeArrayEnd();
        }
        json.writeObjectEnd();
//        json.writeObjectEnd();
    }

    private TiledStructure<?> stringToStructure(String string){
        if(true){
            return all.get(Integer.parseInt(string));
        }
        String[] split = string.split("_");
        int x = Integer.parseInt(split[0]);
        int y = Integer.parseInt(split[1]);

        return all.find(it -> it.editorX == x && it.editorY == y);
    }

    @Override
    public void read(Json json, JsonValue jsonData){
        all.set(json.readValue("all", Seq.class, jsonData));
        JsonValue wires = jsonData.get("wires");
        if(wires == null) return;
        for(JsonValue structure : wires){
            TiledStructure<?> root = stringToStructure(structure.name);
            for(JsonValue jsonValue : structure){
                TiledStructure<?> obj = stringToStructure(jsonValue.getString("obj"));
                int input = jsonValue.getInt("input");
                int parentOutput = jsonValue.getInt("output");
                root.addParent(obj, input, parentOutput);
            }
        }
    }

    @Retention(RetentionPolicy.RUNTIME)
    public @interface CodeEdit{

    }

    /** Base abstract class for any in-map objective. */
    public static abstract class TiledStructure<T extends TiledStructure<T>>{
        /** Temporary container to store references since this class is static. Will immediately be flattened. */
        private transient final Seq<T> children = new Seq<>(2);

        public transient @Nullable
        @org.jetbrains.annotations.Nullable
        @Multiline String details;
        /** The parents of this objective. All parents must be done in order for this to be updated. */
        public transient Seq<ConnectionWire<T>> inputWires = new Seq<>(2);
        /** For the objectives UI dialog. Do not modify directly! */
        @CodeEdit
        public int editorX = -1, editorY = -1;
        public transient ConnectorStyle connectorStyle;
        /** Whether this objective has been done yet. This is internally set. */
        private boolean completed;
        /** Internal value. Do not modify! */
        private transient boolean depFinished, changed;

        public abstract int outputConnections();

        /** @return True if this objective is done and should be removed from the executor. */
        public abstract boolean update();

        /** Reset internal state, if any. */
        public void reset(){
        }

        /** Called once after {@link #update()} returns true, before this objective is removed. */
        public void done(){
            changed();
        }

        /** Notifies the executor that map rules should be synced. */
        protected void changed(){
            changed = true;
        }

        protected boolean requireDependencyFinishing(){
            return true;
        }

        /** @return True if all {@link #inputWires} are completed, rendering this objective able to execute. */
        public final boolean dependencyFinished(){
            if(depFinished) return true;

            for(var parent : inputWires){
                if(!parent.obj.isCompleted()) return false;
            }

            return depFinished = true;
        }

        /** @return True if this objective is done (practically, has been removed from the executor). */
        public final boolean isCompleted(){
            return completed;
        }

        /** @return Whether this objective should run at all. */
        public boolean qualified(){
            return !completed && (!requireDependencyFinishing() || dependencyFinished());
        }
        /*
         *//** @return This objective, with the given child's parents added with this, for chaining operations. *//*
        public TiledStructure child(TiledStructure child){
            child.parents.add(this);
            children.add(child);
            return this;
        }

        *//** @return This objective, with the given parent added to this objective's parents, for chaining operations. *//*
        public TiledStructure parent(TiledStructure parent){
            parents.add(parent);
            return this;
        }*/

        /** @return This objective, with the details message assigned to, for chaining operations. */
        public TiledStructure details(String details){
            this.details = details;
            return this;
        }

        /** @return Basic mission display text. If null, falls back to standard text. */
        public @Nullable String text(){
            return null;
        }

        /** @return Details that appear upon click. */
        public @Nullable String details(){
            return details;
        }

        /** @return The localized type-name of this objective, defaulting to the class simple name without the "Objective" prefix. */
        public abstract String typeName();

        public abstract String name();

        public abstract int inputConnections();

        public ConnectionWire addParent(TiledStructure structure, int input, int parentOutput){
            ConnectionWire wire = new ConnectionWire(structure, input, parentOutput);
            inputWires.add(wire);
            return wire;
        }

        public int objWidth(){
            return Mathf.num(outputConnections() > 0) + Mathf.num(inputConnections() > 0) + 3;
        }

        public int objHeight(){
            return 2;
        }

        public boolean hasFields(){
            return true;
        }

        public Color colorForInput(int input){
            return Pal.accent;
        }

        @org.jetbrains.annotations.Nullable
        public Cons2<TiledStructuresDialog, Table> editor(){
            return null;
        }

        @org.jetbrains.annotations.Nullable
        public Tooltip inputConnectorTooltip(int inputIndex){
            return null;
        }

        @org.jetbrains.annotations.Nullable
        public Tooltip outputConnectorTooltip(int outputIndex){
            return null;
        }

        public boolean enabledInput(int index){
            return true;
        }

        public boolean enabledOutput(int index){
            return true;
        }

        public ConnectorStyle connectorStyle(){
            if(connectorStyle == null) connectorStyle = ConnectorStyle.defaultStyle();
            return connectorStyle;
        }

        public static class ConnectionWire<T extends TiledStructure>{
            public T obj;
            public int input;
            public int parentOutput;

            public ConnectionWire(T obj, int input, int parentOutput){
                this.obj = obj;
                this.input = input;
                this.parentOutput = parentOutput;
            }

            @Override
            public boolean equals(Object o){
                if(this == o) return true;
                if(o == null || getClass() != o.getClass()) return false;

                ConnectionWire that = (ConnectionWire)o;

                if(input != that.input) return false;
                if(parentOutput != that.parentOutput) return false;
                return Objects.equals(obj, that.obj);
            }

            @Override
            public int hashCode(){
                int result = obj != null ? obj.hashCode() : 0;
                result = 31 * result + input;
                result = 31 * result + parentOutput;
                return result;
            }
        }
        /*public String typeName(){
            String className = getClass().getSimpleName().replace("Objective", "");
            return Core.bundle == null ? className : Core.bundle.get("objective." + className.toLowerCase() + ".name", className);
        }*/
    }
}

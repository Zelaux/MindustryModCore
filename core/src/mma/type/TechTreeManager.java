package mma.type;

import arc.struct.Seq;
import arc.util.Nullable;
import mindustry.content.TechTree;
import mindustry.ctype.UnlockableContent;
import mindustry.game.Objectives;
import mindustry.type.ItemStack;
import mindustry.type.SectorPreset;
/**
 * Here are copied methods from {@link mindustry.content.TechTree} for comfortable use in mods.
 * Just inherit this class and do TechTree just like Anuke.
 * */
public class TechTreeManager {
    public TechTree.TechNode context = null;

    @Nullable
    public TechTree.TechNode get(UnlockableContent content) {
        return TechTree.get(content);
    }

    public TechTree.TechNode getNotNull(UnlockableContent content) {
        return TechTree.getNotNull(content);
    }


    public TechTree.TechNode node(UnlockableContent content, Seq<Objectives.Objective> objectives, Runnable children) {
        return node(content, content.researchRequirements(), objectives, children);
    }

    public TechTree.TechNode node(UnlockableContent content, Runnable children) {
        return node(content, content.researchRequirements(), children);
    }

    public TechTree.TechNode node(UnlockableContent content, Seq<Objectives.Objective> objectives) {
        return node(content, content.researchRequirements(), objectives, () -> {
        });
    }
    public TechTree.TechNode nodeMap(UnlockableContent content, Runnable children) {
        return nodeMap(content, content.researchRequirements(),new Seq<>(), children);
    }

    public TechTree.TechNode nodeMap(UnlockableContent content, Seq<Objectives.Objective> objectives) {
        return nodeMap(content, content.researchRequirements(), objectives, () -> {
        });
    }
    public TechTree.TechNode nodeMap(UnlockableContent content, Seq<Objectives.Objective> objectives, Runnable children) {
        return nodeMap(content, content.researchRequirements(), objectives, children);
    }


    private TechTree.TechNode nodeMap(UnlockableContent content, ItemStack[] researchRequirements, Seq<Objectives.Objective> objectives, Runnable children) {

        if (!(context.content instanceof SectorPreset)) return node(content, researchRequirements, objectives, children);
        if (objectives!=null)  objectives.add(new Objectives.SectorComplete((SectorPreset) context.content));

        return node(content, researchRequirements, objectives, children);
    }

    public TechTree.TechNode node(UnlockableContent content, ItemStack[] requirements, Runnable children) {
        return node(content, requirements, (Seq) null, children);
    }

    public TechTree.TechNode node(UnlockableContent content, ItemStack[] requirements, Seq<Objectives.Objective> objectives, Runnable children) {
        TechTree.TechNode node;
        try {
            node = TechTree.getNotNull(content);
        } catch (RuntimeException runtimeException) {
            node = new TechTree.TechNode(context, content, requirements);
            if (objectives != null) {
                node.objectives.addAll(objectives);
            }
        }

        TechTree.TechNode prev = context;
        context = node;
        children.run();
        context = prev;
        return node;
    }

    public TechTree.TechNode node(UnlockableContent block) {
        return node(block, () -> {
        });
    }

    public TechTree.TechNode nodeProduce(UnlockableContent content, Seq<Objectives.Objective> objectives, Runnable children) {
        return node(content, content.researchRequirements(), objectives.and(new Objectives.Produce(content)), children);
    }

    public TechTree.TechNode nodeProduce(UnlockableContent content, Runnable children) {
        return nodeProduce(content, new Seq<>(), children);
    }

    public TechTree.TechNode nodeProduce(UnlockableContent content) {
        return nodeProduce(content, () -> {
        });
    }

}

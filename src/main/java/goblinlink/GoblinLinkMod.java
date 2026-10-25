package goblinlink;

import arc.Events;
import mindustry.content.Blocks;
import mindustry.content.Items;
import mindustry.content.TechTree;
import mindustry.game.EventType.ContentInitEvent;
import mindustry.mod.Mod;
import mindustry.type.ItemStack;
import mindustry.world.Block;

import static mindustry.type.ItemStack.with;

public class GoblinLinkMod extends Mod {

    public static Block goblinLinkBlock;

    public GoblinLinkMod() {
    }

    @Override
    public void loadContent() {
        super.loadContent();

        goblinLinkBlock = new GoblinLinkBlock("goblin-link");

        // Дерево исследований готово только после инициализации контента.
        Events.on(ContentInitEvent.class, e -> {
            TechTree.TechNode parent = TechTree.all.find(n -> n.content == Blocks.siliconSmelter);
            if (parent == null) return;

            ItemStack[] cost = with(
                Items.silicon, 250,
                Items.lead, 200,
                Items.copper, 400
            );

            new TechTree.TechNode(parent, goblinLinkBlock, cost);
        });
    }
}

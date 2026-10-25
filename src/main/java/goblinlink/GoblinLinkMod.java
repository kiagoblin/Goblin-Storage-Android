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
    public static Block goblinProcessor;
    public static Block goblinStorage;

    public GoblinLinkMod() {
    }

    @Override
    public void loadContent() {
        super.loadContent();

        goblinLinkBlock = new GoblinLinkBlock("goblin-link");

        // Процессор: ускоряет передачу предметов между Линком, ядром и соседями.
        goblinProcessor = new GoblinModuleBlock(
            "goblin-processor",
            with(Items.copper, 60, Items.lead, 80, Items.silicon, 120),
            1.0f,   // потребление энергии за тик (60 в секунду)
            0,      // бонус к ёмкости
            15f     // бонус к скорости, предметов в секунду
        );

        // Хранилище: увеличивает ёмкость Линка.
        goblinStorage = new GoblinModuleBlock(
            "goblin-storage",
            with(Items.copper, 150, Items.lead, 100, Items.silicon, 50),
            0.5f,   // потребление энергии за тик (30 в секунду)
            250,    // бонус к ёмкости на каждый вид предмета
            0f      // бонус к скорости
        );

        // Дерево исследований готово только после инициализации контента.
        Events.on(ContentInitEvent.class, e -> {
            TechTree.TechNode parent = TechTree.all.find(n -> n.content == Blocks.siliconSmelter);
            if (parent == null) return;

            ItemStack[] linkCost = with(
                Items.silicon, 250,
                Items.lead, 200,
                Items.copper, 400
            );

            TechTree.TechNode linkNode = new TechTree.TechNode(parent, goblinLinkBlock, linkCost);

            new TechTree.TechNode(linkNode, goblinProcessor, with(
                Items.silicon, 200,
                Items.lead, 250,
                Items.copper, 300
            ));

            new TechTree.TechNode(linkNode, goblinStorage, with(
                Items.silicon, 150,
                Items.lead, 250,
                Items.copper, 300
            ));
        });
    }
}

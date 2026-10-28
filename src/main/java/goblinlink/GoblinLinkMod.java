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

    // Стадии игры: ранняя (1x1), средняя (2x2), поздняя (3x3).
    public static Block[] processors = new Block[3];
    public static Block[] storages = new Block[3];
    public static Block[] nodes = new Block[3];

    public GoblinLinkMod() {
    }

    @Override
    public void loadContent() {
        super.loadContent();

        goblinLinkBlock = new GoblinLinkBlock("goblin-link");

        // ---------- Процессоры: скорость передачи (предметов в секунду) ----------
        // Параметры: имя, размер, цена, энергия за тик, бонус ёмкости, бонус скорости, радиус связи с Линком (клетки)
        processors[0] = new GoblinModuleBlock("goblin-processor", 1,
            with(Items.copper, 90, Items.lead, 120, Items.silicon, 180),
            1.0f, 0, 10f, 10f, 0f);
        processors[1] = new GoblinModuleBlock("goblin-processor-mid", 2,
            with(Items.titanium, 225, Items.silicon, 450, Items.graphite, 225, Items.lead, 150),
            3.0f, 0, 40f, 14f, 0f);
        processors[2] = new GoblinModuleBlock("goblin-processor-late", 3,
            with(Items.thorium, 375, Items.plastanium, 225, Items.surgeAlloy, 150, Items.silicon, 750),
            10.0f, 0, 150f, 20f, 0.10f);

        // ---------- Хранилища: ёмкость Линка (предметов каждого вида) ----------
        storages[0] = new GoblinModuleBlock("goblin-storage", 1,
            with(Items.copper, 225, Items.lead, 150, Items.silicon, 75),
            0.5f, 100, 0f, 10f, 0f);
        storages[1] = new GoblinModuleBlock("goblin-storage-mid", 2,
            with(Items.titanium, 300, Items.silicon, 375, Items.lead, 300, Items.metaglass, 150),
            1.5f, 600, 0f, 14f, 0f);
        storages[2] = new GoblinModuleBlock("goblin-storage-late", 3,
            with(Items.thorium, 450, Items.plastanium, 300, Items.surgeAlloy, 225, Items.silicon, 600),
            5.0f, 1500, 0f, 20f, 0.06f);

        // ---------- Узлы связи: радиус в 3-4 раза больше, чем у модулей ----------
        nodes[0] = new GoblinNodeBlock("goblin-node", 1,
            with(Items.copper, 150, Items.lead, 150, Items.silicon, 120),
            35f, 0f);
        nodes[1] = new GoblinNodeBlock("goblin-node-mid", 2,
            with(Items.titanium, 150, Items.silicon, 225, Items.lead, 150, Items.graphite, 120),
            60f, 0f);
        nodes[2] = new GoblinNodeBlock("goblin-node-late", 3,
            with(Items.thorium, 150, Items.plastanium, 120, Items.surgeAlloy, 90, Items.silicon, 300),
            120f, 0.04f);

        // Дерево исследований готово только после инициализации контента.
        Events.on(ContentInitEvent.class, e -> {
            TechTree.TechNode parent = TechTree.all.find(n -> n.content == Blocks.siliconSmelter);
            if (parent == null) return;

            TechTree.TechNode linkNode = new TechTree.TechNode(parent, goblinLinkBlock, with(
                Items.silicon, 250,
                Items.lead, 200,
                Items.copper, 400
            ));

            // Ранняя стадия открывается после Линка.
            TechTree.TechNode[] early = new TechTree.TechNode[3];
            early[0] = new TechTree.TechNode(linkNode, nodes[0], with(Items.silicon, 100, Items.lead, 150, Items.copper, 200));
            early[1] = new TechTree.TechNode(linkNode, processors[0], with(Items.silicon, 200, Items.lead, 250, Items.copper, 300));
            early[2] = new TechTree.TechNode(linkNode, storages[0], with(Items.silicon, 150, Items.lead, 250, Items.copper, 300));

            // Средняя стадия: после соответствующего блока ранней, поздняя: после средней.
            TechTree.TechNode[] mid = new TechTree.TechNode[3];
            mid[0] = new TechTree.TechNode(early[0], nodes[1], with(Items.titanium, 400, Items.silicon, 400, Items.graphite, 200));
            mid[1] = new TechTree.TechNode(early[1], processors[1], with(Items.titanium, 600, Items.silicon, 600, Items.graphite, 300));
            mid[2] = new TechTree.TechNode(early[2], storages[1], with(Items.titanium, 600, Items.silicon, 500, Items.metaglass, 300));

            new TechTree.TechNode(mid[0], nodes[2], with(Items.thorium, 500, Items.plastanium, 300, Items.surgeAlloy, 200));
            new TechTree.TechNode(mid[1], processors[2], with(Items.thorium, 800, Items.plastanium, 500, Items.surgeAlloy, 300));
            new TechTree.TechNode(mid[2], storages[2], with(Items.thorium, 800, Items.plastanium, 500, Items.surgeAlloy, 300));
        });
    }
}

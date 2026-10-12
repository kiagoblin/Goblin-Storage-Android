package goblinlink;

import mindustry.mod.Mod;
import mindustry.content.Blocks;
import mindustry.content.TechTree;
import static mindustry.type.ItemStack.with;
import mindustry.content.Items;

public class GoblinLinkMod extends Mod {

    public GoblinLinkMod() {
        // Конструктор мода (может быть пустым)
    }

    @Override
    public void loadContent() {
        super.loadContent();

        // Добавляем «Гоблин Линк» в дерево исследований после кремниевой плавильни (silicon-smelter)
        TechTree.addNode(Blocks.siliconSmelter, GoblinLinkContent.goblinLinkBlock, () -> {
            // Стоимость открытия в дереве технологий (например, кремний, свинец и медь)
            node.requirements = with(
                Items.silicon, 250,
                Items.lead, 200,
                Items.copper, 400
            );
        });
    }
}

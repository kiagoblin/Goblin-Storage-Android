package goblinlink;

import mindustry.mod.Mod;
import mindustry.type.Category;
import mindustry.type.ItemStack;
import mindustry.content.Items;

public class GoblinLink extends Mod {

    public static GoblinLinkBlock goblinLinkBlock;

    public GoblinLink() {
        // Конструктор по умолчанию без аргументов для Mindustry
    }

    @Override
    public void loadContent() {
        // Регистрируем блок под именем "goblin-link"
        goblinLinkBlock = new GoblinLinkBlock("goblin-link");
        goblinLinkBlock.requirements(Category.distribution, ItemStack.with(
            Items.copper, 100,
            Items.lead, 50,
            Items.silicon, 40
        ));
  
    }
}

package example;

import arc.util.Log;
import mindustry.content.Items;
import mindustry.mod.Mod;
import mindustry.type.Category;
import mindustry.type.ItemStack;
import mindustry.world.Block;
import mindustry.world.meta.BuildVisibility;

public class ExampleJavaMod extends Mod {

    public static Block goblinLink;

    public ExampleJavaMod() {
        Log.info("GOBLIN JAVA TEST LOADED!");
    }

    @Override
    public void loadContent() {

        goblinLink = new GoblinLink("goblin-link");

        goblinLink.localizedName = "GOBLIN LINK [JAVA TEST]";
        goblinLink.description = "Remote storage link - test version.";

        // Видим в меню строительства
        goblinLink.category = Category.effect;
        goblinLink.buildVisibility = BuildVisibility.shown;

        // Всегда разблокирован для теста
        goblinLink.alwaysUnlocked = true;
        goblinLink.hideDatabase = false;

        // Стоимость
        goblinLink.requirements = new ItemStack[]{
            new ItemStack(Items.copper, 100),
            new ItemStack(Items.lead, 100),
            new ItemStack(Items.graphite, 50)
        };

        Log.info("Goblin Link registered: " + goblinLink.name);
    }
}

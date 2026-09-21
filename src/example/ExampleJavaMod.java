package example;

import arc.util.Log;
import mindustry.content.Items;
import mindustry.type.Category;
import mindustry.world.Block;
import mindustry.mod.Mod;

public class ExampleJavaMod extends Mod {

    public static Block goblinLink;

    public ExampleJavaMod() {
        Log.info("Goblin Storage v0.3.1 loaded!");
    }

    @Override
    public void loadContent() {

        goblinLink = new GoblinLink("goblin-link");

        goblinLink.localizedName = "Goblin Link";
        goblinLink.description = "Remote storage link - test version.";

        goblinLink.category = Category.effect;
        goblinLink.buildVisibility = mindustry.world.meta.BuildVisibility.shown;

        goblinLink.requirements = new mindustry.type.ItemStack[]{
            new mindustry.type.ItemStack(Items.copper, 100),
            new mindustry.type.ItemStack(Items.lead, 100),
            new mindustry.type.ItemStack(Items.graphite, 50)
        };

        Log.info("Goblin Link registered!");
    }
}

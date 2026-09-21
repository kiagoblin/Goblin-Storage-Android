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
        goblinLink.description = "Goblin Storage remote storage link test.";

        // Категория строительства.
        goblinLink.category = Category.effect;

        // Показывать в меню строительства.
        goblinLink.buildVisibility = BuildVisibility.shown;

        // Разблокирован сразу.
        goblinLink.alwaysUnlocked = true;

        // Показывать в базе данных.
        goblinLink.hideDatabase = false;

        // Стоимость строительства.
        goblinLink.requirements = new ItemStack[]{
            new ItemStack(Items.copper, 100),
            new ItemStack(Items.lead, 100),
            new ItemStack(Items.graphite, 50)
        };

        Log.info("GOBLIN LINK REGISTERED: " + goblinLink.name);
    }
            }

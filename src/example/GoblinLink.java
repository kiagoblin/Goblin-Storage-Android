package example;

import arc.Core;
import mindustry.world.blocks.storage.StorageBlock;

public class GoblinLink extends StorageBlock {

    public GoblinLink(String name) {
        super(name);

        size = 2;
        itemCapacity = 1000;
        health = 500;

        // Используем существующую текстуру Mindustry
        region = Core.atlas.find("vault");

        // Правильная регистрация типа Building для v8
        buildType = () -> new GoblinLinkBuild();
    }

    public class GoblinLinkBuild extends StorageBuild {
    }
}

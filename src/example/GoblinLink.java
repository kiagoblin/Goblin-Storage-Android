package example;

import arc.Core;
import mindustry.world.blocks.storage.StorageBlock;

public class GoblinLink extends StorageBlock {

    public GoblinLink(String name) {
        super(name);

        size = 2;
        itemCapacity = 1000;
        health = 500;

        // Используем существующую текстуру Mindustry,
        // чтобы пока не создавать отдельный спрайт.
        region = Core.atlas.find("vault");
    }

    @Override
    public StorageBuild buildType() {
        return new GoblinLinkBuild();
    }

    public class GoblinLinkBuild extends StorageBuild {
    }
}

package example;

import arc.Core;
import mindustry.gen.Building;
import mindustry.type.Item;
import mindustry.world.blocks.storage.StorageBlock;

public class GoblinLink extends StorageBlock {

    public GoblinLink(String name) {
        super(name);

        size = 2;
        itemCapacity = 1000;
        health = 500;

        // Используем стандартную текстуру хранилища Mindustry.
        region = Core.atlas.find("vault");

        // Тип постройки.
        buildType = () -> new GoblinLinkBuild();
    }

    public class GoblinLinkBuild extends StorageBuild {

        @Override
        public void updateTile() {
            super.updateTile();

            // Ищем ядро своей команды.
            Building core = core();

            // Если ядра нет или Goblin Link пустой — ничего не делаем.
            if (core == null || items.total() <= 0) {
                return;
            }

            // Берём первый тип предмета, который находится в Goblin Link.
            Item item = items.first();

            if (item == null) {
                return;
            }

            // Проверяем, может ли ядро принять этот предмет.
            if (!core.acceptItem(this, item)) {
                return;
            }

            // Передаём один предмет в ядро.
            core.handleItem(this, item);

            // Удаляем его из Goblin Link.
            items.remove(item, 1);
        }
    }
}

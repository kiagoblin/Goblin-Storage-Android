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

        // Разрешаем блоку выполнять updateTile().
        update = true;

        // Стандартная текстура хранилища Mindustry.
        region = Core.atlas.find("vault");

        // Тип постройки.
        buildType = () -> new GoblinLinkBuild();
    }

    public class GoblinLinkBuild extends StorageBuild {

        @Override
        public void updateTile() {
            super.updateTile();

            // Если в Goblin Link ничего нет — выходим.
            if (items.total() <= 0) {
                return;
            }

            // Находим ядро нашей команды.
            Building core = core();

            // Если ядро не найдено — ждём следующий тик.
            if (core == null) {
                return;
            }

            // Получаем первый предмет из Goblin Link.
            Item item = items.first();

            if (item == null) {
                return;
            }

            // Проверяем, сколько такого предмета ещё может принять ядро.
            int accepted = core.getMaximumAccepted(item);

            if (accepted <= 0) {
                return;
            }

            // Передаём максимум 10 предметов за тик.
            int amount = Math.min(10, accepted);

            // Нельзя передать больше, чем есть в Goblin Link.
            amount = Math.min(amount, items.get(item));

            if (amount <= 0) {
                return;
            }

            // Передаём предметы в ядро.
            core.items.add(item, amount);
            items.remove(item, amount);
        }
    }
}

package example;

import arc.Core;
import mindustry.gen.Building;
import mindustry.type.ItemStack;
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

            // Ищем ближайшее ядро своей команды.
            Building core = core();

            // Если ядра нет — ничего не делаем.
            if (core == null || items.total <= 0) {
                return;
            }

            // Получаем список предметов, которые сейчас находятся
            // внутри Goblin Link.
            ItemStack[] stacks = items.toArray();

            // Переносим предметы в ядро.
            // За один тик переносится максимум 10 штук.
            for (ItemStack stack : stacks) {

                if (stack.amount <= 0) {
                    continue;
                }

                int accepted = core.getMaximumAccepted(stack.item);

                if (accepted <= 0) {
                    continue;
                }

                int amount = Math.min(stack.amount, 10);
                amount = Math.min(amount, accepted);

                if (amount <= 0) {
                    continue;
                }

                // Убираем предметы из Goblin Link.
                items.remove(stack.item, amount);

                // Добавляем их в ядро.
                core.items.add(stack.item, amount);

                // За один тик обрабатываем только один тип предмета.
                break;
            }
        }
    }
}

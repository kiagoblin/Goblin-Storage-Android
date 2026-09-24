package example;

import arc.Core;
import arc.scene.ui.layout.Table;
import mindustry.Vars;
import mindustry.gen.Building;
import mindustry.gen.Unit;
import mindustry.type.Item;
import mindustry.world.blocks.ItemSelection;
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

        // Разрешаем настройку блока.
        configurable = true;

        // Двойное нажатие очищает выбранный предмет.
        clearOnDoubleTap = true;

        // Очистка конфигурации.
        configClear((GoblinLinkBuild build) -> {
            build.selectedItem = null;
        });
    }

    public class GoblinLinkBuild extends StorageBuild {

        private Item selectedItem;

        @Override
        public void buildConfiguration(Table table) {

            ItemSelection.buildTable(
                table,
                Vars.content.items(),
                () -> selectedItem,
                item -> configure(item)
            );

            table.row();

            table.button(
                "ОТМЕНА",
                () -> configure(null)
            ).size(180f, 50f);
        }

        @Override
        public void configured(Unit builder, Object value) {

            if (value instanceof Item) {
                selectedItem = (Item)value;
            } else {
                selectedItem = null;
            }
        }

        private void pullFromCore(Item item) {

            if (item == null) {
                return;
            }

            Building core = core();

            if (core == null) {
                return;
            }

            int available = core.items.get(item);

            if (available <= 0) {
                return;
            }

            int free = itemCapacity - items.get(item);

            if (free <= 0) {
                return;
            }

            int amount = Math.min(10, available);
            amount = Math.min(amount, free);

            if (amount <= 0) {
                return;
            }

            core.items.remove(item, amount);
            items.add(item, amount);
        }

        /*
         * Goblin Link → Core
         *
         * Используем штатную систему передачи Mindustry.
         * Ядро само проверяет, может ли принять предмет.
         */
        private void pushToCore(Item item) {

            if (item == null) {
                return;
            }

            Building core = core();

            if (core == null) {
                return;
            }

            for (int i = 0; i < 10; i++) {

                if (items.get(item) <= 0) {
                    break;
                }

                // Ядро само проверяет свой реальный лимит.
                if (!core.acceptItem(this, item)) {
                    break;
                }

                // Передаём один предмет штатным способом.
                core.handleItem(this, item);

                // Удаляем его из Goblin Link.
                items.remove(item, 1);
            }
        }

        @Override
        public void updateTile() {

            super.updateTile();

            /*
             * ЕСЛИ ВЫБРАН ПРЕДМЕТ:
             *
             * Ядро → Goblin Link
             */
            if (selectedItem != null) {

                pullFromCore(selectedItem);

                return;
            }

            /*
             * ЕСЛИ ПРЕДМЕТ НЕ ВЫБРАН:
             *
             * Goblin Link → Ядро
             */

            if (items.total() <= 0) {
                return;
            }

            Item item = items.first();

            if (item == null) {
                return;
            }

            pushToCore(item);
        }
    }
}

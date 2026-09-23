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

        // Предмет, который нужно постоянно получать из ядра.
        private Item selectedItem;

        @Override
        public void buildConfiguration(Table table) {

            // Стандартный выбор предмета Mindustry.
            ItemSelection.buildTable(
                table,
                Vars.content.items(),
                () -> selectedItem,
                item -> configure(item)
            );

            // Кнопка отмены выбора.
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

        @Override
        public void updateTile() {

            super.updateTile();

            /*
             * ЕСЛИ ВЫБРАН ПРЕДМЕТ:
             *
             * Ядро → Goblin Link
             *
             * Передача выбранного предмета продолжается постоянно.
             */
            if (selectedItem != null) {

                pullFromCore(selectedItem);

                return;
            }

            /*
             * ЕСЛИ ПРЕДМЕТ НЕ ВЫБРАН:
             *
             * Старый режим:
             *
             * Goblin Link → Ядро
             */

            if (items.total() <= 0) {
                return;
            }

            Building core = core();

            if (core == null) {
                return;
            }

            Item item = items.first();

            if (item == null) {
                return;
            }

            int accepted = core.getMaximumAccepted(item);

            if (accepted <= 0) {
                return;
            }

            int amount = Math.min(10, accepted);
            amount = Math.min(amount, items.get(item));

            if (amount <= 0) {
                return;
            }

            core.items.add(item, amount);
            items.remove(item, amount);
        }
    }
}

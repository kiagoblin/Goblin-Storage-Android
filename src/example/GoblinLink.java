package example;

import arc.Core;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;
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

        // Двойное нажатие очищает выбранные предметы.
        clearOnDoubleTap = true;

        // Очистка конфигурации.
        configClear((GoblinLinkBuild build) -> {
            build.selectedItems.clear();
        });
    }

    public class GoblinLinkBuild extends StorageBuild {

        // Список выбранных предметов.
        private Seq<Item> selectedItems = new Seq<>();

        @Override
        public void buildConfiguration(Table table) {

            /*
             * Выбор предметов.
             *
             * Нажатие на предмет:
             * - если его нет в списке → добавляет;
             * - если уже есть → убирает.
             */
            ItemSelection.buildTable(
                table,
                Vars.content.items(),
                () -> null,
                item -> toggleItem(item)
            );

            table.row();

            table.button(
                "ОТМЕНА",
                () -> selectedItems.clear()
            ).size(180f, 50f);
        }

        private void toggleItem(Item item) {

            if (item == null) {
                return;
            }

            if (selectedItems.contains(item)) {
                selectedItems.remove(item);
            } else {
                selectedItems.add(item);
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
         * Передаём предметы через штатную систему Mindustry.
         * Ядро само контролирует свой лимит.
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

                if (!core.acceptItem(this, item)) {
                    break;
                }

                core.handleItem(this, item);
                items.remove(item, 1);
            }
        }

        @Override
        public void updateTile() {

            super.updateTile();

            /*
             * ЕСЛИ ЕСТЬ ВЫБРАННЫЕ ПРЕДМЕТЫ:
             *
             * Ядро → Goblin Link
             */
            if (selectedItems.size > 0) {

                /*
                 * Проходим по всем выбранным предметам.
                 */
                for (Item item : selectedItems) {

                    if (items.total() >= itemCapacity) {
                        break;
                    }

                    pullFromCore(item);
                }

                return;
            }

            /*
             * ЕСЛИ НИЧЕГО НЕ ВЫБРАНО:
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

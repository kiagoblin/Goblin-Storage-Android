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

        // Разрешаем updateTile().
        update = true;

        // Стандартная текстура хранилища.
        region = Core.atlas.find("vault");

        // Тип постройки.
        buildType = () -> new GoblinLinkBuild();

        // Разрешаем настройку.
        configurable = true;

        // Двойное нажатие очищает список выбранных предметов.
        clearOnDoubleTap = true;

        // Очистка списка выбранных предметов.
        configClear((GoblinLinkBuild build) -> {
            build.selectedItems.clear();
        });
    }

    public class GoblinLinkBuild extends StorageBuild {

        /*
         * Все выбранные предметы.
         */
        private Seq<Item> selectedItems = new Seq<>();

        @Override
        public void buildConfiguration(Table table) {

            /*
             * Окно выбора предметов.
             *
             * Можно выбирать несколько предметов.
             */
            ItemSelection.buildTable(
                table,
                Vars.content.items(),
                () -> null,
                this::toggleItem,
                false
            );

            table.row();

            /*
             * Полная отмена выбора.
             */
            table.button(
                "ОТМЕНА",
                () -> selectedItems.clear()
            ).size(180f, 50f);
        }

        /*
         * Добавить или убрать предмет из списка.
         */
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

        /*
         * Ядро → Goblin Link.
         *
         * Для каждого предмета действует отдельный лимит 1000.
         */
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

            /*
             * Свободное место ИМЕННО для этого предмета.
             */
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
         * Goblin Link → соседний блок.
         *
         * Разгрузчик НЕ нужен.
         */
        private boolean pushToNearby(Item item) {

            if (item == null) {
                return false;
            }

            if (items.get(item) <= 0) {
                return false;
            }

            Building core = core();

            /*
             * Перебираем соседние здания.
             */
            for (Building next : proximity) {

                if (next == null) {
                    continue;
                }

                /*
                 * Не отправляем предмет обратно в ядро.
                 */
                if (next == core) {
                    continue;
                }

                /*
                 * Только здания нашей команды.
                 */
                if (next.team != team) {
                    continue;
                }

                /*
                 * Проверяем, может ли сосед принять предмет.
                 */
                if (!next.acceptItem(this, item)) {
                    continue;
                }

                /*
                 * Передаём предмет напрямую.
                 */
                next.handleItem(this, item);

                /*
                 * Убираем предмет из Link.
                 */
                items.remove(item, 1);

                return true;
            }

            return false;
        }

        /*
         * Goblin Link → Ядро.
         *
         * Работает только когда ничего не выбрано.
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

                /*
                 * Ядро само проверяет свой лимит.
                 */
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
             * =========================================
             * РЕЖИМ ВЫБРАННЫХ ПРЕДМЕТОВ
             * =========================================
             *
             * Ядро → Goblin Link
             *
             * И одновременно:
             *
             * Goblin Link → соседние блоки
             */
            if (selectedItems.size > 0) {

                /*
                 * Получаем КАЖДЫЙ выбранный предмет.
                 *
                 * Здесь НЕТ общего лимита items.total().
                 *
                 * Каждый предмет имеет свой лимит 1000.
                 */
                for (Item item : selectedItems) {

                    pullFromCore(item);
                }

                /*
                 * Выдаём выбранные предметы наружу.
                 */
                for (Item item : selectedItems) {

                    for (int i = 0; i < 10; i++) {

                        if (items.get(item) <= 0) {
                            break;
                        }

                        if (!pushToNearby(item)) {
                            break;
                        }
                    }
                }

                return;
            }

            /*
             * =========================================
             * ОБЫЧНЫЙ РЕЖИМ
             * =========================================
             *
             * Если ничего не выбрано:
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

package example;

import arc.Core;
import arc.graphics.Color;
import arc.scene.ui.Image;
import arc.scene.ui.Label;
import arc.scene.ui.layout.Stack;
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
             * =========================================
             * ВЫБРАННЫЕ ПРЕДМЕТЫ
             * =========================================
             */

            table.add("ВЫБРАНО:").left().pad(5f);
            table.row();

            if (selectedItems.size == 0) {

                table.add("Ничего не выбрано")
                    .color(Color.lightGray)
                    .left()
                    .pad(5f);

                table.row();

            } else {

                /*
                 * Показываем все выбранные предметы.
                 */
                Table selectedTable = new Table();

                for (Item item : selectedItems) {

                    Stack stack = new Stack();

                    /*
                     * Иконка предмета.
                     */
                    stack.add(new Image(item.uiIcon));

                    /*
                     * Зелёная галочка поверх иконки.
                     */
                    Label check = new Label("✓");
                    check.setColor(Color.green);

                    stack.add(check);

                    selectedTable.add(stack)
                        .size(42f)
                        .pad(3f);
                }

                table.add(selectedTable)
                    .left()
                    .pad(5f);

                table.row();
            }

            /*
             * Разделитель.
             */
            table.add("────────────────")
                .color(Color.gray)
                .row();

            /*
             * =========================================
             * ВЫБОР ПРЕДМЕТОВ
             * =========================================
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
             * =========================================
             * ОТМЕНА
             * =========================================
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

                // Уже выбран → убираем.
                selectedItems.remove(item);

            } else {

                // Не выбран → добавляем.
                selectedItems.add(item);
            }
        }

        /*
         * =========================================
         * ЯДРО → GOBLIN LINK
         * =========================================
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
             * Свободное место именно для ЭТОГО предмета.
             *
             * Каждый тип может храниться до 1000.
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
         * =========================================
         * GOBLIN LINK → СОСЕДНИЙ БЛОК
         * =========================================
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
                 * Может ли сосед принять предмет?
                 */
                if (!next.acceptItem(this, item)) {
                    continue;
                }

                /*
                 * Передаём напрямую.
                 */
                next.handleItem(this, item);

                /*
                 * Убираем из Link.
                 */
                items.remove(item, 1);

                return true;
            }

            return false;
        }

        /*
         * =========================================
         * GOBLIN LINK → ЯДРО
         * =========================================
         *
         * Используется, когда ничего не выбрано.
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
                 * Ядро само контролирует свой лимит.
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
             * Goblin Link → заводы
             */
            if (selectedItems.size > 0) {

                /*
                 * Получаем каждый выбранный предмет.
                 *
                 * У каждого свой лимит 1000.
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
             * Ничего не выбрано:
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

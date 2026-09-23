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

        // Используем стандартную текстуру хранилища Mindustry.
        region = Core.atlas.find("vault");

        // Тип постройки.
        buildType = () -> new GoblinLinkBuild();

        // Разрешаем открывать меню настройки блока.
        configurable = true;
    }

    public class GoblinLinkBuild extends StorageBuild {

        // Выбранный предмет для получения из ядра.
        private Item selectedItem;

        // Небольшая задержка после получения предмета,
        // чтобы он не улетел обратно в ядро сразу же.
        private int pullCooldown = 0;

        @Override
        public void buildConfiguration(Table table) {

            // Стандартное меню выбора предмета Mindustry.
            ItemSelection.buildTable(
                table,
                Vars.content.items(),
                () -> selectedItem,
                item -> configure(item)
            );
        }

        @Override
        public void configured(Unit builder, Object value) {

            if (value instanceof Item) {

                selectedItem = (Item)value;

                pullFromCore(selectedItem);

                // Даём предмету немного времени остаться
                // внутри Goblin Link для проверки.
                pullCooldown = 120;
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

            // Проверяем, сколько такого предмета есть в ядре.
            int available = core.items.get(item);

            if (available <= 0) {
                return;
            }

            // Проверяем свободное место в Goblin Link.
            int free = itemCapacity - items.get(item);

            if (free <= 0) {
                return;
            }

            // Забираем максимум 10 предметов за одно нажатие.
            int amount = Math.min(10, available);

            amount = Math.min(amount, free);

            if (amount <= 0) {
                return;
            }

            // Забираем предметы из ядра.
            core.items.remove(item, amount);

            // Добавляем их в Goblin Link.
            items.add(item, amount);
        }

        @Override
        public void updateTile() {

            super.updateTile();

            // После ручного получения временно
            // запрещаем отправлять предмет обратно.
            if (pullCooldown > 0) {
                pullCooldown--;
                return;
            }

            // Если Goblin Link пустой — ничего не делаем.
            if (items.total() <= 0) {
                return;
            }

            // Получаем ядро нашей команды.
            Building core = core();

            if (core == null) {
                return;
            }

            // Получаем первый предмет из Goblin Link.
            Item item = items.first();

            if (item == null) {
                return;
            }

            // Проверяем, сколько такого предмета может принять ядро.
            int accepted = core.getMaximumAccepted(item);

            if (accepted <= 0) {
                return;
            }

            // Передаём максимум 10 предметов за тик.
            int amount = Math.min(10, accepted);

            // Не передаём больше, чем есть в Goblin Link.
            amount = Math.min(amount, items.get(item));

            if (amount <= 0) {
                return;
            }

            // Передаём предметы в ядро.
            core.items.add(item, amount);

            // Убираем их из Goblin Link.
            items.remove(item, amount);
        }
    }
}

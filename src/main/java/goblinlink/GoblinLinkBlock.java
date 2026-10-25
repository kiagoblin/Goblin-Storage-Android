package goblinlink;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.scene.ui.Image;
import arc.scene.ui.Label;
import arc.scene.ui.layout.Stack;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import arc.util.Strings;
import arc.util.Time;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.Vars;
import mindustry.content.Items;
import mindustry.gen.Building;
import mindustry.type.Category;
import mindustry.type.Item;
import mindustry.world.blocks.ItemSelection;
import mindustry.world.blocks.storage.StorageBlock;

import static mindustry.type.ItemStack.with;

public class GoblinLinkBlock extends StorageBlock {

    /** Базовая ёмкость Линка (на каждый вид предмета), без модулей хранения. */
    public static final int BASE_CAPACITY = 10;
    /** Базовая скорость передачи (предметов в секунду), без процессоров. */
    public static final float BASE_TRANSFER_RATE = 2f;

    public GoblinLinkBlock(String name) {
        super(name);

        // Без requirements блок скрыт: его нет ни в меню строительства, ни в базе данных.
        requirements(Category.effect, with(Items.copper, 1000, Items.lead, 650, Items.silicon, 500));

        size = 2;
        itemCapacity = BASE_CAPACITY;
        health = 500;
        update = true;
        configurable = true;
        clearOnDoubleTap = true;
        hasItems = true;
        solid = true;
        destructible = true;

        config(Item.class, (GoblinLinkBuild build, Item item) -> {
            build.toggleItem(item);
        });

        configClear((GoblinLinkBuild build) -> {
            build.selectedItems.clear();
        });
    }

    @Override
    public void load() {
        super.load();

        region = GoblinSprites.find(name, region);
        uiIcon = region;
        fullIcon = region;
    }

    public class GoblinLinkBuild extends StorageBuild {
        public Seq<Item> selectedItems = new Seq<>();

        /** Бонусы от модулей: модули за тик складывают их сюда, Линк забирает в начале своего тика. */
        public int pendingCapacity = 0;
        public float pendingSpeed = 0f;

        /** Действующие бонусы (за прошлый тик). */
        public int capacityBonus = 0;
        public float speedBonus = 0f;

        /** Накопленный запас передач: одна передача = один предмет. */
        private float budget = 0f;

        public int currentCapacity() {
            return BASE_CAPACITY + capacityBonus;
        }

        public float transferRate() {
            return BASE_TRANSFER_RATE + speedBonus;
        }

        @Override
        public int getMaximumAccepted(Item item) {
            return linkedCore != null ? super.getMaximumAccepted(item) : currentCapacity();
        }

        @Override
        public void draw() {
            super.draw();
            // Явно задаем физический размер отрисовки 16x16 units (2x2 блока)
            Draw.rect(region, x, y, size * Vars.tilesize, size * Vars.tilesize);
        }

        @Override
        public void buildConfiguration(Table table) {
            table.label(() -> "Ёмкость: " + currentCapacity() + " | Скорость: " + Strings.fixed(transferRate(), 1) + "/с")
                .left()
                .pad(5f);
            table.row();

            table.add("ВЫБРАНО:").left().pad(5f);
            table.row();

            if (selectedItems.size == 0) {
                table.add("Ничего не выбрано")
                    .color(Color.lightGray)
                    .left()
                    .pad(5f);
                table.row();
            } else {
                Table selectedTable = new Table();

                for (Item item : selectedItems) {
                    Stack stack = new Stack();
                    stack.add(new Image(item.uiIcon));

                    Label check = new Label("✓");
                    check.setColor(Color.green);

                    stack.add(check);

                    selectedTable.add(stack).size(42f).pad(3f);
                }

                table.add(selectedTable).left().pad(5f);
                table.row();
            }

            table.add("────────────────").color(Color.gray).row();

            ItemSelection.buildTable(
                table,
                Vars.content.items(),
                () -> null,
                this::configure
            );

            table.row();

            table.button("ОТМЕНА", () -> configure(null))
                 .size(180f, 50f)
                 .pad(5f);
        }

        public void toggleItem(Item item) {
            if (item == null) {
                selectedItems.clear();
                return;
            }

            if (selectedItems.contains(item)) {
                selectedItems.remove(item);
            } else {
                selectedItems.add(item);
            }
        }

        /** Переносит один предмет из ядра в Линк. */
        private boolean pullOneFromCore(Item item) {
            if (item == null) return false;

            Building core = core();
            if (core == null) return false;

            if (core.items.get(item) <= 0) return false;
            if (items.get(item) >= getMaximumAccepted(item)) return false;

            core.items.remove(item, 1);
            items.add(item, 1);
            return true;
        }

        /** Отдаёт один предмет соседнему блоку (кроме ядра). */
        private boolean pushOneToNearby(Item item) {
            if (item == null || items.get(item) <= 0) return false;

            Building core = core();

            for (Building next : proximity) {
                if (next == null || next == core || next.team != team) continue;

                if (!next.acceptItem(this, item)) continue;

                next.handleItem(this, item);
                items.remove(item, 1);
                return true;
            }

            return false;
        }

        /** Возвращает один предмет из Линка в ядро. */
        private boolean pushOneToCore(Item item) {
            if (item == null || items.get(item) <= 0) return false;

            Building core = core();
            if (core == null) return false;
            if (!core.acceptItem(this, item)) return false;

            core.handleItem(this, item);
            items.remove(item, 1);
            return true;
        }

        @Override
        public void updateTile() {
            super.updateTile();

            // Забираем бонусы, которые модули успели добавить за прошлый тик.
            capacityBonus = pendingCapacity;
            speedBonus = pendingSpeed;
            pendingCapacity = 0;
            pendingSpeed = 0f;

            // Запас передач копится со скоростью transferRate и ограничен примерно секундой работы.
            float rate = transferRate();
            budget = Math.min(budget + rate / 60f * Time.delta, Math.max(2f, rate));

            if (selectedItems.size > 0) {
                int guard = 0;

                while (budget >= 1f && guard++ < 256) {
                    boolean moved = false;

                    for (Item item : selectedItems) {
                        if (budget >= 1f && pullOneFromCore(item)) {
                            budget -= 1f;
                            moved = true;
                        }

                        if (budget >= 1f && pushOneToNearby(item)) {
                            budget -= 1f;
                            moved = true;
                        }
                    }

                    if (!moved) break;
                }
                return;
            }

            if (items.total() <= 0) return;

            int guard = 0;
            while (budget >= 1f && guard++ < 256) {
                Item item = items.first();
                if (item == null || !pushOneToCore(item)) break;
                budget -= 1f;
            }
        }

        @Override
        public void write(Writes write) {
            super.write(write);
            write.s(selectedItems.size);
            for (Item item : selectedItems) {
                write.s(item.id);
            }
        }

        @Override
        public void read(Reads read, byte revision) {
            super.read(read, revision);
            int count = read.s();
            selectedItems.clear();
            for (int i = 0; i < count; i++) {
                short id = read.s();
                Item item = Vars.content.item(id);
                if (item != null) {
                    selectedItems.add(item);
                }
            }
        }
    }
}

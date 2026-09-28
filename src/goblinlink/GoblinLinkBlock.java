package goblinlink;

import arc.Core;
import arc.graphics.Color;
import arc.scene.ui.Image;
import arc.scene.ui.Label;
import arc.scene.ui.layout.Stack;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.Vars;
import mindustry.gen.Building;
import mindustry.type.Item;
import mindustry.world.blocks.ItemSelection;
import mindustry.world.blocks.storage.StorageBlock;

public class GoblinLinkBlock extends StorageBlock {

    public GoblinLinkBlock(String name) {
        super(name);

        size = 2;
        itemCapacity = 1000;
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
        // Ищем текстуру по полному имени блока или по базовому
        if (Core.atlas.has(name)) {
            region = Core.atlas.find(name);
        } else if (Core.atlas.has("goblin-link")) {
            region = Core.atlas.find("goblin-link");
        } else if (Core.atlas.has("goblin-link-goblin-link")) {
            region = Core.atlas.find("goblin-link-goblin-link");
        }
    }

    public class GoblinLinkBuild extends StorageBuild {
        public Seq<Item> selectedItems = new Seq<>();

        @Override
        public void buildConfiguration(Table table) {
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

        private void pullFromCore(Item item) {
            if (item == null) return;

            Building core = core();
            if (core == null) return;

            int available = core.items.get(item);
            if (available <= 0) return;

            int free = itemCapacity - items.get(item);
            if (free <= 0) return;

            int amount = Math.min(10, available);
            amount = Math.min(amount, free);

            if (amount <= 0) return;

            core.items.remove(item, amount);
            items.add(item, amount);
        }

        private boolean pushToNearby(Item item) {
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

        private void pushToCore(Item item) {
            if (item == null) return;

            Building core = core();
            if (core == null) return;

            for (int i = 0; i < 10; i++) {
                if (items.get(item) <= 0) break;
                if (!core.acceptItem(this, item)) break;

                core.handleItem(this, item);
                items.remove(item, 1);
            }
        }

        @Override
        public void updateTile() {
            super.updateTile();

            if (selectedItems.size > 0) {
                for (Item item : selectedItems) {
                    pullFromCore(item);
                }

                for (Item item : selectedItems) {
                    for (int i = 0; i < 10; i++) {
                        if (items.get(item) <= 0) break;
                        if (!pushToNearby(item)) break;
                    }
                }
                return;
            }

            if (items.total() <= 0) return;

            Item item = items.first();
            if (item != null) {
                pushToCore(item);
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
            int size = read.s();
            selectedItems.clear();
            for (int i = 0; i < size; i++) {
                short id = read.s();
                Item item = Vars.content.item(id);
                if (item != null) {
                    selectedItems.add(item);
                }
            }
        }
    }
}

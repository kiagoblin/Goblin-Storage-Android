package goblinlink;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.Vars;
import mindustry.gen.Building;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.type.Category;
import mindustry.type.ItemStack;
import mindustry.world.Block;
import mindustry.world.meta.Stat;
import mindustry.world.meta.StatUnit;

/**
 * Узел связи: к нему подключаются модули (процессоры и хранилища) с большого расстояния,
 * а сам узел подключается к одному Гоблин Линку и передаёт ему суммарный бонус.
 * Узлы нельзя соединять друг с другом (иначе бонус зациклится).
 */
public class GoblinHubBlock extends Block {

    /** Радиус связывания (модули к узлу и узел к Линку), в клетках. */
    public float linkRange;

    public GoblinHubBlock(String name, int size, ItemStack[] reqs, float powerUse, float linkRange) {
        super(name);

        this.linkRange = linkRange;

        requirements(Category.effect, reqs);

        this.size = size;
        health = 150 * size * size;
        update = true;
        solid = true;
        destructible = true;
        configurable = true;
        hasPower = true;

        consumePower(powerUse);

        config(Integer.class, (GoblinHubBuild build, Integer pos) -> {
            build.link = pos;
        });
    }

    @Override
    public void load() {
        super.load();

        region = GoblinSprites.find(name, region);
        uiIcon = region;
        fullIcon = region;
    }

    @Override
    public void setStats() {
        super.setStats();

        stats.add(Stat.range, linkRange, StatUnit.blocks);
    }

    @Override
    public void drawPlace(int x, int y, int rotation, boolean valid) {
        super.drawPlace(x, y, rotation, valid);

        Drawf.dashCircle(x * Vars.tilesize + offset, y * Vars.tilesize + offset, linkRange * Vars.tilesize, Pal.accent);
    }

    public class GoblinHubBuild extends Building implements GoblinBonusReceiver {
        /** Позиция привязанного Линка, -1 если связи нет. */
        public int link = -1;

        /** Бонусы, накопленные за прошлый тик от модулей. */
        public int pendingCapacity = 0;
        public float pendingSpeed = 0f;

        @Override
        public void addBonus(int capacity, float speed) {
            pendingCapacity += capacity;
            pendingSpeed += speed;
        }

        private boolean inReach(Building other) {
            return within(other, linkRange * Vars.tilesize + other.block.size * Vars.tilesize / 2f);
        }

        private boolean isLink(Building other) {
            return other instanceof GoblinLinkBlock.GoblinLinkBuild && other.team == team && inReach(other);
        }

        public GoblinLinkBlock.GoblinLinkBuild linkedLink() {
            if (link == -1) return null;

            Building other = Vars.world.build(link);
            return other != null && isLink(other) ? (GoblinLinkBlock.GoblinLinkBuild) other : null;
        }

        @Override
        public void updateTile() {
            int capacity = pendingCapacity;
            float speed = pendingSpeed;
            pendingCapacity = 0;
            pendingSpeed = 0f;

            GoblinLinkBlock.GoblinLinkBuild target = linkedLink();
            if (target == null) return;

            // Без энергии узел ничего не передаёт Линку.
            if (efficiency > 0.5f) {
                target.addBonus(capacity, speed);
            }
        }

        @Override
        public boolean onConfigureBuildTapped(Building other) {
            // Нажали на Линк: привязать узел к нему или отвязать.
            if (other != null && isLink(other)) {
                if (link == other.pos()) {
                    configure(-1);
                } else {
                    configure(other.pos());
                }
                return false;
            }

            // Нажали на модуль: привязать его к этому узлу или отвязать (удобно привязывать пачкой).
            if (other instanceof GoblinModuleBlock.GoblinModuleBuild && other.team == team && inReach(other)) {
                if (((GoblinModuleBlock.GoblinModuleBuild) other).link == pos()) {
                    other.configure(-1);
                } else {
                    other.configure(pos());
                }
                return false;
            }

            return true;
        }

        @Override
        public void drawConfigure() {
            super.drawConfigure();

            Drawf.dashCircle(x, y, linkRange * Vars.tilesize, Pal.accent);

            GoblinLinkBlock.GoblinLinkBuild target = linkedLink();
            if (target != null) {
                Drawf.square(target.x, target.y, target.block.size * Vars.tilesize / 2f + 1f, Pal.place);
            }
        }

        @Override
        public void draw() {
            super.draw();

            GoblinLinkBlock.GoblinLinkBuild target = linkedLink();
            if (target == null) return;

            Draw.z(Layer.power);
            Draw.color(Color.white, 0.5f);
            Lines.stroke(1f);
            Lines.line(x, y, target.x, target.y);
            Draw.reset();
            Draw.z(Layer.block);
        }

        @Override
        public void write(Writes write) {
            super.write(write);
            write.i(link);
        }

        @Override
        public void read(Reads read, byte revision) {
            super.read(read, revision);
            link = read.i();
        }
    }
}

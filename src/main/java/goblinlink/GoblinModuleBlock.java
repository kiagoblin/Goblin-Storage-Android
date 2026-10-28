package goblinlink;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.Vars;
import mindustry.content.Liquids;
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
 * Модуль для Гоблин Линка: даёт бонус к ёмкости и/или скорости передачи.
 * Работает от энергии и привязывается к Линку вручную (нажать на модуль, затем на Линк).
 */
public class GoblinModuleBlock extends Block {

    /** Сколько предметов каждого вида добавляет к ёмкости Линка. */
    public int capacityBonus;
    /** Сколько предметов в секунду добавляет к скорости передачи Линка. */
    public float speedBonus;
    /** Радиус связывания с Линком, в клетках. */
    public float linkRange = 10f;

    public GoblinModuleBlock(String name, int size, ItemStack[] reqs, float powerUse, int capacityBonus, float speedBonus, float linkRange, float cryoUse) {
        super(name);

        this.capacityBonus = capacityBonus;
        this.speedBonus = speedBonus;
        this.linkRange = linkRange;

        requirements(Category.effect, reqs);

        this.size = size;
        health = 120 * size * size;
        update = true;
        solid = true;
        destructible = true;
        configurable = true;
        hasPower = true;

        consumePower(powerUse);

        // Модули поздней стадии дополнительно требуют криогенную жидкость.
        if (cryoUse > 0f) {
            hasLiquids = true;
            consumeLiquid(Liquids.cryofluid, cryoUse);
        }

        config(Integer.class, (GoblinModuleBuild build, Integer pos) -> {
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

        if (capacityBonus > 0) {
            stats.add(Stat.itemCapacity, capacityBonus, StatUnit.items);
        }
        if (speedBonus > 0f) {
            stats.add(Stat.itemsMoved, speedBonus, StatUnit.itemsSecond);
        }
    }

    @Override
    public void drawPlace(int x, int y, int rotation, boolean valid) {
        super.drawPlace(x, y, rotation, valid);

        Drawf.dashCircle(x * Vars.tilesize + offset, y * Vars.tilesize + offset, linkRange * Vars.tilesize, Pal.accent);
    }

    public class GoblinModuleBuild extends Building {
        /** Позиция привязанного Линка, -1 если связи нет. */
        public int link = -1;

        /** Возвращает Линк, к которому модуль привязан напрямую или через узел связи. */
        public GoblinLinkBlock.GoblinLinkBuild linkedLink() {
            if (link == -1) return null;

            Building other = Vars.world.build(link);
            if (other == null || other.team != team) return null;

            if (other instanceof GoblinLinkBlock.GoblinLinkBuild) {
                return inLinkRange(other) ? (GoblinLinkBlock.GoblinLinkBuild) other : null;
            }

            if (other instanceof GoblinNodeBlock.GoblinNodeBuild) {
                GoblinNodeBlock.GoblinNodeBuild node = (GoblinNodeBlock.GoblinNodeBuild) other;
                return node.reaches(this) ? node.linkedLink() : null;
            }

            return null;
        }

        /** То, к чему модуль привязан (Линк или узел), либо null. */
        public Building linkTarget() {
            if (link == -1) return null;

            Building other = Vars.world.build(link);
            return other != null && other.team == team ? other : null;
        }

        private boolean inLinkRange(Building other) {
            return within(other, linkRange * Vars.tilesize + other.block.size * Vars.tilesize / 2f);
        }

        @Override
        public void updateTile() {
            GoblinLinkBlock.GoblinLinkBuild target = linkedLink();
            if (target == null) return;

            // Без энергии efficiency равна 0 и бонус не даётся.
            if (efficiency > 0.5f) {
                target.pendingCapacity += capacityBonus;
            }
            target.pendingSpeed += speedBonus * efficiency;
        }

        @Override
        public boolean onConfigureBuildTapped(Building other) {
            if (other == null || other.team != team) return true;

            boolean validLink = other instanceof GoblinLinkBlock.GoblinLinkBuild && inLinkRange(other);
            boolean validNode = other instanceof GoblinNodeBlock.GoblinNodeBuild
                && ((GoblinNodeBlock.GoblinNodeBuild) other).reaches(this);

            if (validLink || validNode) {
                configure(link == other.pos() ? -1 : other.pos());
                return false;
            }

            return true;
        }

        @Override
        public void drawConfigure() {
            super.drawConfigure();

            Drawf.dashCircle(x, y, linkRange * Vars.tilesize, Pal.accent);

            Building target = linkTarget();
            if (target != null) {
                Drawf.square(target.x, target.y, target.block.size * Vars.tilesize / 2f + 1f, Pal.place);
            }
        }

        @Override
        public void draw() {
            super.draw();

            Building target = linkTarget();
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

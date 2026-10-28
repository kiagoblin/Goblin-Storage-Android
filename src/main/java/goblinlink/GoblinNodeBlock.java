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

/**
 * Узел связи: к нему привязываются модули (процессоры и хранилища), а сам он привязывается к Линку.
 * У узла радиус действия намного больше, чем у модулей, поэтому модули можно ставить далеко от Линка.
 */
public class GoblinNodeBlock extends Block {

    /** Радиус действия узла, в клетках. */
    public float range;
    /** Узел поздней стадии работает только при подаче криогенной жидкости. */
    public boolean needsCoolant;

    public GoblinNodeBlock(String name, int size, ItemStack[] reqs, float range, float cryoUse) {
        super(name);

        this.range = range;
        this.needsCoolant = cryoUse > 0f;

        requirements(Category.effect, reqs);

        this.size = size;
        health = 120 * size * size;
        update = true;
        solid = true;
        destructible = true;
        configurable = true;

        if (cryoUse > 0f) {
            hasLiquids = true;
            consumeLiquid(Liquids.cryofluid, cryoUse);
        }

        config(Integer.class, (GoblinNodeBuild build, Integer pos) -> {
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
    public void drawPlace(int x, int y, int rotation, boolean valid) {
        super.drawPlace(x, y, rotation, valid);

        Drawf.dashCircle(x * Vars.tilesize + offset, y * Vars.tilesize + offset, range * Vars.tilesize, Pal.accent);
    }

    public class GoblinNodeBuild extends Building {
        /** Позиция привязанного Линка, -1 если связи нет. */
        public int link = -1;

        /** Достаёт ли радиус узла до указанного блока. */
        public boolean reaches(Building other) {
            return within(other, range * Vars.tilesize + other.block.size * Vars.tilesize / 2f);
        }

        /** Привязанный Линк, если он существует, наш и в радиусе узла. */
        public GoblinLinkBlock.GoblinLinkBuild linkedLink() {
            if (link == -1) return null;
            if (needsCoolant && efficiency <= 0.5f) return null;

            Building other = Vars.world.build(link);
            if (other == null || other.team != team) return null;
            if (!(other instanceof GoblinLinkBlock.GoblinLinkBuild)) return null;
            if (!reaches(other)) return null;

            return (GoblinLinkBlock.GoblinLinkBuild) other;
        }

        @Override
        public boolean onConfigureBuildTapped(Building other) {
            if (other == null || other.team != team) return true;

            // Нажали на Линк: привязываем или отвязываем.
            if (other instanceof GoblinLinkBlock.GoblinLinkBuild && reaches(other)) {
                configure(link == other.pos() ? -1 : other.pos());
                return false;
            }

            // Нажали на модуль: привязываем его к этому узлу или отвязываем.
            if (other instanceof GoblinModuleBlock.GoblinModuleBuild && reaches(other)) {
                other.configure(((GoblinModuleBlock.GoblinModuleBuild) other).link == pos() ? -1 : pos());
                return false;
            }

            return true;
        }

        @Override
        public void drawConfigure() {
            super.drawConfigure();

            Drawf.dashCircle(x, y, range * Vars.tilesize, Pal.accent);

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

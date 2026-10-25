package goblinlink;

import arc.Core;
import arc.graphics.g2d.TextureRegion;

/** Поиск спрайта блока: у модов название региона в атласе зависит от имени файла. */
public final class GoblinSprites {

    private static final String MOD_PREFIX = "goblin-link-";

    private GoblinSprites() {
    }

    /** Возвращает первый найденный регион из типичных вариантов имени, иначе fallback. */
    public static TextureRegion find(String blockName, TextureRegion fallback) {
        String base = blockName.startsWith(MOD_PREFIX) ? blockName.substring(MOD_PREFIX.length()) : blockName;

        String[] candidates = {
            blockName,
            "block-" + blockName,
            MOD_PREFIX + blockName,
            MOD_PREFIX + "block-" + blockName,
            base,
            "block-" + base,
            MOD_PREFIX + base
        };

        for (String candidate : candidates) {
            if (Core.atlas.has(candidate)) {
                return Core.atlas.find(candidate);
            }
        }

        return fallback;
    }
}

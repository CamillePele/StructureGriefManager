package cam.pele.sgm.config.model;

import net.minecraft.core.BlockPos;

public class Bounds {
    public int[] min = new int[] { 0, 0, 0 };
    public int[] max = new int[] { 0, 0, 0 };

    public boolean contains(BlockPos pos) {
        if (min == null || max == null || min.length < 3 || max.length < 3)
            return false;

        return pos.getX() >= min[0] && pos.getX() <= max[0] &&
                pos.getY() >= min[1] && pos.getY() <= max[1] &&
                pos.getZ() >= min[2] && pos.getZ() <= max[2];
    }
}

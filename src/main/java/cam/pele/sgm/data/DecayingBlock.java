package cam.pele.sgm.data;

import cam.pele.sgm.config.model.DropStrategy;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;

public class DecayingBlock {
    public BlockPos pos;
    public long targetTime;
    public DropStrategy strategy;

    public DecayingBlock(BlockPos pos, long targetTime, DropStrategy strategy) {
        this.pos = pos;
        this.targetTime = targetTime;
        this.strategy = strategy;
    }

    public static DecayingBlock readNbt(CompoundTag tag) {
        BlockPos pos = NbtUtils.readBlockPos(tag.getCompound("pos"));
        long targetTime = tag.getLong("targetTime");
        DropStrategy strategy = DropStrategy.valueOf(tag.getString("strategy"));
        return new DecayingBlock(pos, targetTime, strategy);
    }

    public CompoundTag writeNbt() {
        CompoundTag tag = new CompoundTag();
        tag.put("pos", NbtUtils.writeBlockPos(pos));
        tag.putLong("targetTime", targetTime);
        tag.putString("strategy", strategy.name());
        return tag;
    }
}

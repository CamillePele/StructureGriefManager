package cam.pele.sgm.data;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.level.block.state.BlockState;

public class RestoringBlock {
    public BlockPos pos;
    public BlockState state;
    public long targetTime;
    public int retryCount;

    public RestoringBlock(BlockPos pos, BlockState state, long targetTime, int retryCount) {
        this.pos = pos;
        this.state = state;
        this.targetTime = targetTime;
        this.retryCount = retryCount;
    }

    public RestoringBlock(BlockPos pos, BlockState state, long targetTime) {
        this(pos, state, targetTime, 0);
    }

    public static RestoringBlock readNbt(CompoundTag tag) {
        BlockPos pos = NbtUtils.readBlockPos(tag.getCompound("pos"));
        BlockState state = NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), tag.getCompound("state"));
        long targetTime = tag.getLong("targetTime");
        int retryCount = tag.getInt("retryCount");
        return new RestoringBlock(pos, state, targetTime, retryCount);
    }

    public CompoundTag writeNbt() {
        CompoundTag tag = new CompoundTag();
        tag.put("pos", NbtUtils.writeBlockPos(pos));
        tag.put("state", NbtUtils.writeBlockState(state));
        tag.putLong("targetTime", targetTime);
        tag.putInt("retryCount", retryCount);
        return tag;
    }
}

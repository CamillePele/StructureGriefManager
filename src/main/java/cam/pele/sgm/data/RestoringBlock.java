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
    public CompoundTag nbt;

    public RestoringBlock(BlockPos pos, BlockState state, long targetTime, int retryCount, CompoundTag nbt) {
        this.pos = pos;
        this.state = state;
        this.targetTime = targetTime;
        this.retryCount = retryCount;
        this.nbt = nbt;
    }

    public RestoringBlock(BlockPos pos, BlockState state, long targetTime, CompoundTag nbt) {
        this(pos, state, targetTime, 0, nbt);
    }

    // Legacy constructor compatibility if needed internally, though ideally we pass
    // NBT everywhere now.
    public RestoringBlock(BlockPos pos, BlockState state, long targetTime) {
        this(pos, state, targetTime, 0, null);
    }

    public static RestoringBlock readNbt(CompoundTag tag) {
        BlockPos pos = NbtUtils.readBlockPos(tag.getCompound("pos"));
        BlockState state = NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), tag.getCompound("state"));
        long targetTime = tag.getLong("targetTime");
        int retryCount = tag.getInt("retryCount");
        CompoundTag nbt = null;
        if (tag.contains("blockEntityData")) {
            nbt = tag.getCompound("blockEntityData");
        }
        return new RestoringBlock(pos, state, targetTime, retryCount, nbt);
    }

    public CompoundTag writeNbt() {
        CompoundTag tag = new CompoundTag();
        tag.put("pos", NbtUtils.writeBlockPos(pos));
        tag.put("state", NbtUtils.writeBlockState(state));
        tag.putLong("targetTime", targetTime);
        tag.putInt("retryCount", retryCount);
        if (this.nbt != null) {
            tag.put("blockEntityData", this.nbt);
        }
        return tag;
    }
}

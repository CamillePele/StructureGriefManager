package cam.pele.sgm.data;

import net.minecraft.nbt.CompoundTag;
import java.util.List;

public interface ISgmChunkData {
    List<RestoringBlock> getRestoringBlocks();

    List<DecayingBlock> getDecayingBlocks();

    void addRestoringBlock(RestoringBlock block);

    void addDecayingBlock(DecayingBlock block);

    boolean removeDecayingBlock(net.minecraft.core.BlockPos pos);

    void copyFrom(ISgmChunkData other);

    void saveNbt(CompoundTag tag);

    void loadNbt(CompoundTag tag);
}

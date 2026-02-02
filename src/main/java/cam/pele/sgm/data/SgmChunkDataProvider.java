package cam.pele.sgm.data;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SgmChunkDataProvider implements ICapabilitySerializable<CompoundTag> {

    private final SgmChunkData backend = new SgmChunkData();
    private final LazyOptional<ISgmChunkData> optional = LazyOptional.of(() -> backend);

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == SgmCapEvents.CHUNK_DATA) {
            return optional.cast();
        }
        return LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        backend.saveNbt(tag);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        backend.loadNbt(nbt);
    }
}

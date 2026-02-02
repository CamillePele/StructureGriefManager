package cam.pele.sgm.data;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import java.util.ArrayList;
import java.util.List;

public class SgmChunkData implements ISgmChunkData {

    private final List<RestoringBlock> restoringBlocks = new ArrayList<>();
    private final List<DecayingBlock> decayingBlocks = new ArrayList<>();

    @Override
    public List<RestoringBlock> getRestoringBlocks() {
        return restoringBlocks;
    }

    @Override
    public List<DecayingBlock> getDecayingBlocks() {
        return decayingBlocks;
    }

    @Override
    public void addRestoringBlock(RestoringBlock block) {
        restoringBlocks.add(block);
    }

    @Override
    public void addDecayingBlock(DecayingBlock block) {
        decayingBlocks.add(block);
    }

    @Override
    public boolean removeDecayingBlock(net.minecraft.core.BlockPos pos) {
        java.util.Iterator<DecayingBlock> iterator = decayingBlocks.iterator();
        while (iterator.hasNext()) {
            DecayingBlock block = iterator.next();
            if (block.pos.equals(pos)) {
                iterator.remove();
                return true;
            }
        }
        return false;
    }

    @Override
    public void copyFrom(ISgmChunkData other) {
        this.restoringBlocks.clear();
        this.restoringBlocks.addAll(other.getRestoringBlocks());
        this.decayingBlocks.clear();
        this.decayingBlocks.addAll(other.getDecayingBlocks());
    }

    @Override
    public void saveNbt(CompoundTag tag) {
        ListTag restoringList = new ListTag();
        for (RestoringBlock block : restoringBlocks) {
            restoringList.add(block.writeNbt());
        }
        tag.put("RestoringBlocks", restoringList);

        ListTag decayingList = new ListTag();
        for (DecayingBlock block : decayingBlocks) {
            decayingList.add(block.writeNbt());
        }
        tag.put("DecayingBlocks", decayingList);
    }

    @Override
    public void loadNbt(CompoundTag tag) {
        restoringBlocks.clear();
        decayingBlocks.clear();

        if (tag.contains("RestoringBlocks")) {
            ListTag list = tag.getList("RestoringBlocks", Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                restoringBlocks.add(RestoringBlock.readNbt(list.getCompound(i)));
            }
        }

        if (tag.contains("DecayingBlocks")) {
            ListTag list = tag.getList("DecayingBlocks", Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                decayingBlocks.add(DecayingBlock.readNbt(list.getCompound(i)));
            }
        }
    }
}

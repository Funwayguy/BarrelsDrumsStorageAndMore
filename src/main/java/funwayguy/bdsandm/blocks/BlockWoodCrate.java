package funwayguy.bdsandm.blocks;

import funwayguy.bdsandm.core.BDSM;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;

public class BlockWoodCrate extends BlockCrateBase
{
    public BlockWoodCrate()
    {
        super(Material.WOOD, 64, 1024);
        this.setHardness(2.0F).setResistance(5.0F);
        Blocks.FIRE.setFireInfo(this, 5, 20);
        this.setTranslationKey(BDSM.MOD_ID + ".wood_crate");
    }
}

package funwayguy.bdsandm.inventory.capability;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;

import javax.annotation.Nullable;

public interface IBarrel extends IStackContainer, IFluidHandlerItem
{
    @Nullable
    FluidStack getRefFluid();
    void setFluidContainer(ItemStack stack);
}

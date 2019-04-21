package funwayguy.bdsandm.client.renderer;

import funwayguy.bdsandm.blocks.tiles.TileEntityCrate;
import funwayguy.bdsandm.core.BDSM;
import funwayguy.bdsandm.inventory.capability.BdsmCapabilies;
import funwayguy.bdsandm.inventory.capability.ICrate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.config.GuiUtils;

public class TileEntityRenderCrate extends TileEntitySpecialRenderer<TileEntityCrate>
{
    private static final ResourceLocation ICON_TEX = new ResourceLocation(BDSM.MOD_ID, "textures/gui/color_sliders.png");
    
    @Override
    public void render(TileEntityCrate te, double x, double y, double z, float partialTicks, int destroyStage, float alpha)
    {
        super.render(te, x, y, z, partialTicks, destroyStage, alpha);
        
        Minecraft mc = Minecraft.getMinecraft();
        ICrate crate = te.getCapability(BdsmCapabilies.CRATE_CAP, null);
        FontRenderer font = mc.fontRenderer;
        
        if(crate == null) return;
        boolean shift = crate.isOreDict() || crate.isLocked() || crate.voidOverflow();
        
        if(this.rendererDispatcher.cameraHitResult != null && te.getPos().equals(this.rendererDispatcher.cameraHitResult.getBlockPos()))
        {
            ItemStack item = mc.player.getHeldItemMainhand();
            
            if(!item.isEmpty() && item.getItem() == BDSM.itemUpgrade && item.getItemDamage() < 5)
            {
                // === RENDER TEXT ===
                
                GlStateManager.pushMatrix();
                
                GlStateManager.translate(x + 0.5D, y + 0.5D, z + 0.5D);
                rotateSide(EnumFacing.byIndex(te.getBlockMetadata() & 7));
                GlStateManager.translate(0D, 0.3D, 0.5D);
                GlStateManager.scale(0.01F, 0.01F, 1F);
                GlStateManager.rotate(180F, 1F, 0F, 0F);
                
                GlStateManager.disableLighting();
                
                String s = "" + crate.getStackCap();
                font.drawString(s, -font.getStringWidth(s) / 2, 0, 0xFFFFFFFF);
                GlStateManager.translate(0F, 0F, 0.01F);
                font.drawString(s, -font.getStringWidth(s) / 2 + 1, 1, colorToShadow(0xFFFFFFFF));
                GlStateManager.translate(0F, 0F, -0.01F);
                
                GlStateManager.translate(0F, font.FONT_HEIGHT, 0F);
                s = "/ " + crate.getUpgradeCap();
                font.drawString(s, -font.getStringWidth(s) / 2, 0, 0xFFFFFFFF);
                GlStateManager.translate(0F, 0F, 0.01F);
                font.drawString(s, -font.getStringWidth(s) / 2 + 1, 1, colorToShadow(0xFFFFFFFF));
                GlStateManager.translate(0F, 0F, -0.01F);
               
                GlStateManager.enableLighting();
                
                GlStateManager.popMatrix();
                return;
            }
        }
        
        if(!crate.getRefItem().isEmpty())
        {
            GlStateManager.pushMatrix();
            
            GlStateManager.translate(x + 0.5D, y + 0.5D, z + 0.5D);
            rotateSide(EnumFacing.byIndex(te.getBlockMetadata() & 7));
            GlStateManager.translate(shift ? 0.075D : 0D, 0.35D, 0.5D);
            GlStateManager.scale(0.01F, 0.01F, 1F);
            GlStateManager.rotate(180F, 1F, 0F, 0F);
            
            GlStateManager.disableLighting();
            
            String s = "" + crate.getRefItem().getMaxStackSize();
            s += "x" + (crate.getCount() / crate.getRefItem().getMaxStackSize());
            font.drawString(s, -font.getStringWidth(s) / 2, 0, 0xFFFFFFFF);
            GlStateManager.translate(0F, 0F, 0.01F);
            font.drawString(s, -font.getStringWidth(s) / 2 + 1, 1, colorToShadow(0xFFFFFFFF));
            GlStateManager.translate(0F, 0F, -0.01F);
            
            GlStateManager.translate(0F, font.FONT_HEIGHT, 0F);
            s = "+" + (crate.getCount() % crate.getRefItem().getMaxStackSize());
            font.drawString(s, -font.getStringWidth(s) / 2, 0, 0xFFFFFFFF);
            GlStateManager.translate(0F, 0F, 0.01F);
            font.drawString(s, -font.getStringWidth(s) / 2 + 1, 1, colorToShadow(0xFFFFFFFF));
            GlStateManager.translate(0F, 0F, -0.01F);
            
            GlStateManager.enableLighting();
            
            GlStateManager.popMatrix();
            
            GlStateManager.pushMatrix();
            GlStateManager.translate(x + 0.5D, y + 0.5D, z + 0.5D);
            rotateSide(EnumFacing.byIndex(te.getBlockMetadata() & 7));
            GlStateManager.translate(shift ? 0.075D : 0D, -0.075D, 0.5D);
            GlStateManager.scale(0.475F, 0.475F, 0.01F);
            
            mc.getRenderItem().renderItem(crate.getRefItem(), TransformType.GUI);
            
            GlStateManager.popMatrix();
        }
        
        GlStateManager.pushMatrix();
        
        GlStateManager.translate(x + 0.5D, y + 0.5D, z + 0.5D);
        rotateSide(EnumFacing.byIndex(te.getBlockMetadata() & 7));
        GlStateManager.translate(0D, 0D, 0.5D);
        GlStateManager.scale(0.01F, 0.01F, 1F);
        GlStateManager.rotate(180F, 1F, 0F, 0F);
        
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        
        GlStateManager.disableLighting();
        GlStateManager.enableTexture2D();
        mc.renderEngine.bindTexture(ICON_TEX);
        GlStateManager.color(1F, 1F, 1F, 1F);
        
        if(crate.isLocked()) GuiUtils.drawTexturedModalRect(-36, -36, 0, 0, 16, 16, 0);
        if(crate.isOreDict()) GuiUtils.drawTexturedModalRect(-36, -20, 32, 0, 16, 16, 0);
        if(crate.voidOverflow()) GuiUtils.drawTexturedModalRect(-36,-4, 16, 0, 16, 16, 0);
        
        GlStateManager.popMatrix();
    }
    
    private void rotateSide(EnumFacing facing)
    {
        switch(facing)
        {
            case UP:
                GlStateManager.rotate(180F, 0F, 1F, 0F);
                GlStateManager.rotate(90F, 1F, 0F, 0F);
                break;
            case DOWN:
                GlStateManager.rotate(180F, 0F, 1F, 0F);
                GlStateManager.rotate(270F, 1F, 0F, 0F);
                break;
            case NORTH:
                GlStateManager.rotate(0F, 0F, 1F, 0F);
                break;
            case SOUTH:
                GlStateManager.rotate(180F, 0F, 1F, 0F);
                break;
            case WEST:
                GlStateManager.rotate(90F, 0F, 1F, 0F);
                break;
            case EAST:
                GlStateManager.rotate(270F, 0F, 1F, 0F);
                break;
        }
    }
    
    private int colorToShadow(int color)
    {
        if ((color & -67108864) == 0)
        {
            color |= -16777216;
        }

        return (color & 16579836) >> 2 | color & -16777216;
    }
}
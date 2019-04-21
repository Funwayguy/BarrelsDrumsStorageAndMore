package funwayguy.bdsandm.client.renderer;

import funwayguy.bdsandm.blocks.tiles.TileEntityBarrel;
import funwayguy.bdsandm.core.BDSM;
import funwayguy.bdsandm.inventory.capability.BdsmCapabilies;
import funwayguy.bdsandm.inventory.capability.CapabilityBarrel;
import funwayguy.bdsandm.inventory.capability.IBarrel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.config.GuiUtils;

public class TileEntityRenderBarrel extends TileEntitySpecialRenderer<TileEntityBarrel>
{
    private static final ResourceLocation ICON_TEX = new ResourceLocation(BDSM.MOD_ID, "textures/gui/color_sliders.png");
    
    @Override
    public void render(TileEntityBarrel te, double x, double y, double z, float partialTicks, int destroyStage, float alpha)
    {
        super.render(te, x, y, z, partialTicks, destroyStage, alpha);
        
        IBarrel bCap = te.getCapability(BdsmCapabilies.BARREL_CAP, null);
        
        if(!(bCap instanceof CapabilityBarrel)) return;
        
        CapabilityBarrel barrel = (CapabilityBarrel)bCap;
        Minecraft mc = Minecraft.getMinecraft();
        FontRenderer font = mc.fontRenderer;
        
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
                
                String s = "" + barrel.getStackCap();
                font.drawString(s, -font.getStringWidth(s) / 2, 0, 0xFFFFFFFF);
                GlStateManager.translate(0F, 0F, 0.01F);
                font.drawString(s, -font.getStringWidth(s) / 2 + 1, 1, colorToShadow(0xFFFFFFFF));
                GlStateManager.translate(0F, 0F, -0.01F);
                
                GlStateManager.translate(0F, font.FONT_HEIGHT, 0F);
                s = "/ " + barrel.getUpgradeCap();
                font.drawString(s, -font.getStringWidth(s) / 2, 0, 0xFFFFFFFF);
                GlStateManager.translate(0F, 0F, 0.01F);
                font.drawString(s, -font.getStringWidth(s) / 2 + 1, 1, colorToShadow(0xFFFFFFFF));
                GlStateManager.translate(0F, 0F, -0.01F);
                
                GlStateManager.enableLighting();
                
                GlStateManager.popMatrix();
                return;
            }
        }
        
        if(!barrel.getRefItem().isEmpty())
        {
            // === RENDER TEXT ===
            
            GlStateManager.pushMatrix();
            
            GlStateManager.translate(x + 0.5D, y + 0.5D, z + 0.5D);
            rotateSide(EnumFacing.byIndex(te.getBlockMetadata() & 7));
            GlStateManager.translate(0D, 0.3D, 0.5D);
            GlStateManager.scale(0.01F, 0.01F, 1F);
            GlStateManager.rotate(180F, 1F, 0F, 0F);
            
            GlStateManager.disableLighting();
            
            String s = "" + barrel.getRefItem().getMaxStackSize();
            s += "x" + (barrel.getCount() / barrel.getRefItem().getMaxStackSize());
            font.drawString(s, -font.getStringWidth(s) / 2, 0, 0xFFFFFFFF);
            GlStateManager.translate(0F, 0F, 0.01F);
            font.drawString(s, -font.getStringWidth(s) / 2 + 1, 1, colorToShadow(0xFFFFFFFF));
            GlStateManager.translate(0F, 0F, -0.01F);
            
            GlStateManager.translate(0F, font.FONT_HEIGHT, 0F);
            s = "+" + (barrel.getCount() % barrel.getRefItem().getMaxStackSize());
            font.drawString(s, -font.getStringWidth(s) / 2, 0, 0xFFFFFFFF);
            GlStateManager.translate(0F, 0F, 0.01F);
            font.drawString(s, -font.getStringWidth(s) / 2 + 1, 1, colorToShadow(0xFFFFFFFF));
            
            GlStateManager.enableLighting();
            
            GlStateManager.popMatrix();
            
            // === RENDER ITEM ===
            
            GlStateManager.pushMatrix();
            GlStateManager.translate(x + 0.5D, y + 0.5D, z + 0.5D);
            rotateSide(EnumFacing.byIndex(te.getBlockMetadata() & 7));
            GlStateManager.translate(0D, - 0.1D, 0.5D);
            GlStateManager.scale(0.4F, 0.4F, 0.01F);
            
            mc.getRenderItem().renderItem(barrel.getRefItem(), TransformType.GUI);
            
            GlStateManager.popMatrix();
        } else if(barrel.getRefFluid() != null)
        {
            // === RENDER TEXT ===
            
            GlStateManager.pushMatrix();
            
            GlStateManager.translate(x + 0.5D, y + 0.5D, z + 0.5D);
            rotateSide(EnumFacing.byIndex(te.getBlockMetadata() & 7));
            GlStateManager.translate(0D, 0.3D, 0.5D);
            GlStateManager.scale(0.01F, 0.01F, 1F);
            GlStateManager.rotate(180F, 1F, 0F, 0F);
            
            GlStateManager.disableLighting();
            
            String s = "1B";
            s += "x" + (barrel.getCount() / 1000);
            font.drawString(s, -font.getStringWidth(s) / 2, 0, 0xFFFFFFFF);
            GlStateManager.translate(0F, 0F, 0.01F);
            font.drawString(s, -font.getStringWidth(s) / 2 + 1, 1, colorToShadow(0xFFFFFFFF));
            GlStateManager.translate(0F, 0F, -0.01F);
            
            GlStateManager.translate(0F, font.FONT_HEIGHT, 0F);
            s = "+" + (barrel.getCount() % 1000) + "mB";
            font.drawString(s, -font.getStringWidth(s) / 2, 0, 0xFFFFFFFF);
            GlStateManager.translate(0F, 0F, 0.01F);
            font.drawString(s, -font.getStringWidth(s) / 2 + 1, 1, colorToShadow(0xFFFFFFFF));
            
            GlStateManager.enableLighting();
            
            GlStateManager.popMatrix();
            
            // === RENDER FLUID ===
            
            if(barrel.getRefFluid().getFluid().getStill(barrel.getRefFluid()) != null)
            {
                GlStateManager.pushMatrix();
                GlStateManager.translate(x + 0.5D, y + 0.5D, z + 0.5D);
                rotateSide(EnumFacing.byIndex(te.getBlockMetadata() & 7));
                GlStateManager.scale(0.025F, 0.025F, 1F);
                GlStateManager.rotate(180F, 1F, 0F, 0F);
                GlStateManager.translate(-8D, -5D, -0.5D);
                
                GlStateManager.disableLighting();
                GlStateManager.enableBlend();
                GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
                GlStateManager.color(1F, 1F, 1F, 1F);
                
				try
				{
			        mc.renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
					TextureAtlasSprite fluidTx = mc.getTextureMapBlocks().getAtlasSprite(barrel.getRefFluid().getFluid().getStill(barrel.getRefFluid()).toString());
					
					int color = barrel.getRefFluid().getFluid().getColor(barrel.getRefFluid());
					int b = color & 255;
					int g = (color >> 8) & 255;
					int r = (color >> 16) & 255;
					int a = (color >> 24) & 255;
					GlStateManager.color(r/255F, g/255F, b/255F, a/255F);
					
                    Tessellator tessellator = Tessellator.getInstance();
                    BufferBuilder vertexbuffer = tessellator.getBuffer();
                    vertexbuffer.begin(7, DefaultVertexFormats.POSITION_TEX);
                    vertexbuffer.pos(0, 16, 0).tex((double)fluidTx.getMinU(), (double)fluidTx.getMaxV()).endVertex();
                    vertexbuffer.pos(16, 16, 0).tex((double)fluidTx.getMaxU(), (double)fluidTx.getMaxV()).endVertex();
                    vertexbuffer.pos(16, 0, 0).tex((double)fluidTx.getMaxU(), (double)fluidTx.getMinV()).endVertex();
                    vertexbuffer.pos(0, 0, 0).tex((double)fluidTx.getMinU(), (double)fluidTx.getMinV()).endVertex();
                    tessellator.draw();
				} catch(Exception e)
                {
                    BDSM.logger.error("Error rendering barrel fluid", e);
                }
                
                GlStateManager.enableLighting();
                GlStateManager.popMatrix();
            }
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
        
        if(barrel.isLocked()) GuiUtils.drawTexturedModalRect(-36, -24, 0, 0, 16, 16, 0);
        if(barrel.isOreDict()) GuiUtils.drawTexturedModalRect(-36, -8, 32, 0, 16, 16, 0);
        if(barrel.voidOverflow()) GuiUtils.drawTexturedModalRect(-36,8, 16, 0, 16, 16, 0);
        
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

package funwayguy.bdsandm.client.renderer;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager.DestFactor;
import com.mojang.blaze3d.platform.GlStateManager.SourceFactor;
import com.mojang.blaze3d.systems.RenderSystem;
import funwayguy.bdsandm.blocks.tiles.CrateTileEntity;
import funwayguy.bdsandm.core.BDSM;
import funwayguy.bdsandm.core.BDSMTags;
import funwayguy.bdsandm.inventory.capability.BdsmCapabilies;
import funwayguy.bdsandm.inventory.capability.ICrate;
import funwayguy.bdsandm.items.UpgradeItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.item.ItemStack;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.fml.client.gui.GuiUtils;

public class TileEntityRenderCrate extends TileEntityRenderer<CrateTileEntity>
{
    private static final ResourceLocation ICON_TEX = new ResourceLocation(BDSM.MOD_ID, "textures/gui/color_sliders.png");

    public TileEntityRenderCrate(TileEntityRendererDispatcher rendererDispatcherIn)
    {
        super(rendererDispatcherIn);
    }

    @Override
    public void render(CrateTileEntity te, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn)
    {
        Minecraft mc = Minecraft.getInstance();
        ICrate crate = te.getCapability(BdsmCapabilies.CRATE_CAP, null).orElse(null);
        FontRenderer font = mc.fontRenderer;
        Direction facing = te.getBlockState().get(BlockStateProperties.FACING);
        
        if(crate == null) return;
        boolean shift = crate.isOreDict() || crate.isLocked() || crate.voidOverflow();

        if(this.renderDispatcher.cameraHitResult != null && te.getPos().equals(this.renderDispatcher.cameraHitResult.getHitVec()))
        {
            ItemStack item = mc.player.getHeldItemMainhand();

            if(!item.isEmpty() && item.getItem().isIn(BDSMTags.UPGRADES) && item.getItem() instanceof UpgradeItem)
            {
                // === RENDER TEXT ===

                matrixStackIn.push();
                RenderSystem.pushMatrix();
                
                rotateSide(matrixStackIn, facing);
                matrixStackIn.translate(0D, 0.3D, 0.5D);
                matrixStackIn.scale(0.01F, 0.01F, 1F);
                matrixStackIn.rotate(Vector3f.XP.rotationDegrees(180F));
                
                RenderSystem.disableLighting();
                
                String s = "" + crate.getStackCap();
                font.drawString(matrixStackIn, s, -font.getStringWidth(s) / 2, 0, 0xFFFFFFFF);
                matrixStackIn.translate(0F, 0F, 0.01F);
                font.drawString(matrixStackIn, s, -font.getStringWidth(s) / 2 + 1, 1, colorToShadow(0xFFFFFFFF));
                matrixStackIn.translate(0F, 0F, -0.01F);
                
                matrixStackIn.translate(0F, font.FONT_HEIGHT, 0F);
                s = "/ " + crate.getUpgradeCap();
                font.drawString(matrixStackIn, s, -font.getStringWidth(s) / 2, 0, 0xFFFFFFFF);
                matrixStackIn.translate(0F, 0F, 0.01F);
                font.drawString(matrixStackIn, s, -font.getStringWidth(s) / 2 + 1, 1, colorToShadow(0xFFFFFFFF));
                matrixStackIn.translate(0F, 0F, -0.01F);
               
                RenderSystem.enableLighting();
                
                RenderSystem.popMatrix();
                matrixStackIn.pop();
                return;
            }
        }
        
        if(!crate.getRefItem().isEmpty())
        {
            matrixStackIn.push();
            RenderSystem.pushMatrix();
            
            rotateSide(matrixStackIn, facing);
            matrixStackIn.translate(shift ? 0.075D : 0D, 0.35D, 0.5D);
            matrixStackIn.scale(0.01F, 0.01F, 1F);
            matrixStackIn.rotate(Vector3f.XP.rotationDegrees(180F));
            
            RenderSystem.disableLighting();
            
            String s = "" + crate.getRefItem().getMaxStackSize();
            s += "x" + (crate.getCount() / crate.getRefItem().getMaxStackSize());
            font.drawString(matrixStackIn, s, -font.getStringWidth(s) / 2, 0, 0xFFFFFFFF);
            matrixStackIn.translate(0F, 0F, 0.01F);
            font.drawString(matrixStackIn, s, -font.getStringWidth(s) / 2 + 1, 1, colorToShadow(0xFFFFFFFF));
            matrixStackIn.translate(0F, 0F, -0.01F);
            
            matrixStackIn.translate(0F, font.FONT_HEIGHT, 0F);
            s = "+" + (crate.getCount() % crate.getRefItem().getMaxStackSize());
            font.drawString(matrixStackIn, s, -font.getStringWidth(s) / 2, 0, 0xFFFFFFFF);
            matrixStackIn.translate(0F, 0F, 0.01F);
            font.drawString(matrixStackIn, s, -font.getStringWidth(s) / 2 + 1, 1, colorToShadow(0xFFFFFFFF));
            matrixStackIn.translate(0F, 0F, -0.01F);
            
            RenderSystem.enableLighting();
            
            RenderSystem.popMatrix();
            
            RenderSystem.pushMatrix();
             rotateSide(matrixStackIn, facing);
            matrixStackIn.translate(shift ? 0.075D : 0D, -0.075D, 0.5D);
            matrixStackIn.scale(0.475F, 0.475F, 0.01F);
            
            Minecraft.getInstance().getItemRenderer().renderItem(crate.getRefItem(), TransformType.GUI, combinedLightIn, combinedOverlayIn, matrixStackIn, bufferIn);

            RenderSystem.popMatrix();
            matrixStackIn.pop();
        }

        matrixStackIn.push();
        RenderSystem.pushMatrix();
        
        rotateSide(matrixStackIn, facing);
        matrixStackIn.translate(0D, 0D, 0.5D);
        matrixStackIn.scale(0.01F, 0.01F, 1F);
        matrixStackIn.rotate(Vector3f.XP.rotationDegrees(180F));
        
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA, SourceFactor.ONE, DestFactor.ZERO);
        
        RenderSystem.disableLighting();
        RenderSystem.enableTexture();
        mc.getTextureManager().bindTexture(ICON_TEX);
        RenderSystem.color4f(1F, 1F, 1F, 1F);
        
        if(crate.isLocked()) GuiUtils.drawTexturedModalRect(matrixStackIn, -36, -36, 0, 0, 16, 16, 0);
        if(crate.isOreDict()) GuiUtils.drawTexturedModalRect(matrixStackIn, -36, -20, 32, 0, 16, 16, 0);
        if(crate.voidOverflow()) GuiUtils.drawTexturedModalRect(matrixStackIn, -36,-4, 16, 0, 16, 16, 0);

        RenderSystem.popMatrix();
        matrixStackIn.pop();
    }

    private void rotateSide(MatrixStack matrixStackIn, Direction facing)
    {
        switch(facing)
        {
            case UP:
                matrixStackIn.rotate(Vector3f.YP.rotationDegrees(180F));
                matrixStackIn.rotate(Vector3f.XP.rotationDegrees(90F));
                break;
            case DOWN:
                matrixStackIn.rotate(Vector3f.YP.rotationDegrees(180F));
                matrixStackIn.rotate(Vector3f.XP.rotationDegrees(270F));
                break;
            case NORTH:
                matrixStackIn.rotate(Vector3f.YP.rotationDegrees(0F));
                break;
            case SOUTH:
                matrixStackIn.rotate(Vector3f.YP.rotationDegrees(180F));
                break;
            case WEST:
                matrixStackIn.rotate(Vector3f.YP.rotationDegrees(90F));
                break;
            case EAST:
                matrixStackIn.rotate(Vector3f.YP.rotationDegrees(270F));
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
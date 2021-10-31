package funwayguy.bdsandm.client.renderer;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager.DestFactor;
import com.mojang.blaze3d.platform.GlStateManager.SourceFactor;
import com.mojang.blaze3d.systems.RenderSystem;
import funwayguy.bdsandm.blocks.tiles.BarrelTileEntity;
import funwayguy.bdsandm.core.BDSM;
import funwayguy.bdsandm.core.BDSMTags;
import funwayguy.bdsandm.inventory.capability.BdsmCapabilies;
import funwayguy.bdsandm.inventory.capability.CapabilityBarrel;
import funwayguy.bdsandm.inventory.capability.IBarrel;
import funwayguy.bdsandm.items.UpgradeItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.Texture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.item.ItemStack;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fml.client.gui.GuiUtils;

public class TileEntityRenderBarrel extends TileEntityRenderer<BarrelTileEntity>
{
    private static final ResourceLocation ICON_TEX = new ResourceLocation(BDSM.MOD_ID, "textures/gui/color_sliders.png");

    public TileEntityRenderBarrel(TileEntityRendererDispatcher rendererDispatcherIn)
    {
        super(rendererDispatcherIn);
    }

    @Override
    public void render(BarrelTileEntity te, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn)
    {
        IBarrel bCap = te.getCapability(BdsmCapabilies.BARREL_CAP, null).orElse(null);

        if(!(bCap instanceof CapabilityBarrel)) return;

        CapabilityBarrel barrel = (CapabilityBarrel)bCap;
        Minecraft mc = Minecraft.getInstance();
        FontRenderer font = mc.fontRenderer;
        Direction facing = te.getBlockState().get(BlockStateProperties.FACING);

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

                String s = "" + barrel.getStackCap();
                font.drawString(matrixStackIn, s, -font.getStringWidth(s) / 2, 0, 0xFFFFFFFF);
                matrixStackIn.translate(0F, 0F, 0.01F);
                font.drawString(matrixStackIn, s, -font.getStringWidth(s) / 2 + 1, 1, colorToShadow(0xFFFFFFFF));
                matrixStackIn.translate(0F, 0F, -0.01F);

                matrixStackIn.translate(0F, font.FONT_HEIGHT, 0F);
                s = "/ " + barrel.getUpgradeCap();
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

        if(!barrel.getRefItem().isEmpty())
        {
            // === RENDER TEXT ===

            matrixStackIn.push();
            RenderSystem.pushMatrix();

            rotateSide(matrixStackIn, facing);
            matrixStackIn.translate(0D, 0.3D, 0.5D);
            matrixStackIn.scale(0.01F, 0.01F, 1F);
            matrixStackIn.rotate(Vector3f.XP.rotationDegrees(180F));

            RenderSystem.disableLighting();

            String s = "" + barrel.getRefItem().getMaxStackSize();
            s += "x" + (barrel.getCount() / barrel.getRefItem().getMaxStackSize());
            font.drawString(matrixStackIn, s, -font.getStringWidth(s) / 2, 0, 0xFFFFFFFF);
            matrixStackIn.translate(0F, 0F, 0.01F);
            font.drawString(matrixStackIn, s, -font.getStringWidth(s) / 2 + 1, 1, colorToShadow(0xFFFFFFFF));
            matrixStackIn.translate(0F, 0F, -0.01F);

            matrixStackIn.translate(0F, font.FONT_HEIGHT, 0F);
            s = "+" + (barrel.getCount() % barrel.getRefItem().getMaxStackSize());
            font.drawString(matrixStackIn, s, -font.getStringWidth(s) / 2, 0, 0xFFFFFFFF);
            matrixStackIn.translate(0F, 0F, 0.01F);
            font.drawString(matrixStackIn, s, -font.getStringWidth(s) / 2 + 1, 1, colorToShadow(0xFFFFFFFF));

            RenderSystem.enableLighting();

            RenderSystem.popMatrix();

            // === RENDER ITEM ===

            RenderSystem.pushMatrix();
            rotateSide(matrixStackIn, facing);
            matrixStackIn.translate(0D, - 0.1D, 0.5D);
            matrixStackIn.scale(0.4F, 0.4F, 0.01F);

            Minecraft.getInstance().getItemRenderer().renderItem(barrel.getRefItem(), TransformType.GUI, combinedLightIn, combinedOverlayIn, matrixStackIn, bufferIn);

            RenderSystem.popMatrix();
            matrixStackIn.pop();
        } else if(barrel.getRefFluid() != null)
        {
            // === RENDER TEXT ===

            matrixStackIn.push();
            RenderSystem.pushMatrix();

            rotateSide(matrixStackIn, facing);
            matrixStackIn.translate(0D, 0.3D, 0.5D);
            matrixStackIn.scale(0.01F, 0.01F, 1F);
            matrixStackIn.rotate(Vector3f.XP.rotationDegrees(180F));

            RenderSystem.disableLighting();

            String s = "1B";
            s += "x" + (barrel.getCount() / 1000);
            font.drawString(matrixStackIn, s, -font.getStringWidth(s) / 2, 0, 0xFFFFFFFF);
            matrixStackIn.translate(0F, 0F, 0.01F);
            font.drawString(matrixStackIn, s, -font.getStringWidth(s) / 2 + 1, 1, colorToShadow(0xFFFFFFFF));
            matrixStackIn.translate(0F, 0F, -0.01F);

            matrixStackIn.translate(0F, font.FONT_HEIGHT, 0F);
            s = "+" + (barrel.getCount() % 1000) + "mB";
            font.drawString(matrixStackIn, s, -font.getStringWidth(s) / 2, 0, 0xFFFFFFFF);
            matrixStackIn.translate(0F, 0F, 0.01F);
            font.drawString(matrixStackIn, s, -font.getStringWidth(s) / 2 + 1, 1, colorToShadow(0xFFFFFFFF));

            RenderSystem.enableLighting();

            RenderSystem.popMatrix();
            matrixStackIn.pop();

            // === RENDER FLUID ===

            FluidAttributes refAttributes = barrel.getRefFluid().getFluid().getAttributes();
            if(refAttributes.getStillTexture(barrel.getRefFluid()) != null)
            {
                matrixStackIn.push();
                RenderSystem.pushMatrix();
                rotateSide(matrixStackIn, facing);
                matrixStackIn.scale(0.025F, 0.025F, 1F);
                matrixStackIn.rotate(Vector3f.XP.rotationDegrees(180F));
                matrixStackIn.translate(-8D, -5D, -0.5D);

                RenderSystem.disableLighting();
                RenderSystem.enableBlend();
                RenderSystem.blendFuncSeparate(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA, SourceFactor.ONE, DestFactor.ZERO);
                RenderSystem.color4f(1F, 1F, 1F, 1F);

                try
                {
                    mc.getTextureManager().bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);
                    Texture texture = Minecraft.getInstance().getTextureManager().getTexture(PlayerContainer.LOCATION_BLOCKS_TEXTURE);
                    TextureAtlasSprite fluidTx = ((AtlasTexture) texture).getSprite(refAttributes.getStillTexture(barrel.getRefFluid()));

                    int color = refAttributes.getColor(barrel.getRefFluid());
                    int b = color & 255;
                    int g = (color >> 8) & 255;
                    int r = (color >> 16) & 255;
                    int a = (color >> 24) & 255;
                    RenderSystem.color4f(r/255F, g/255F, b/255F, a/255F);

                    Tessellator tessellator = Tessellator.getInstance();
                    BufferBuilder vertexbuffer = tessellator.getBuffer();
                    vertexbuffer.begin(7, DefaultVertexFormats.POSITION_TEX);
                    vertexbuffer.pos(0, 16, 0).tex(fluidTx.getMinU(), fluidTx.getMaxV()).endVertex();
                    vertexbuffer.pos(16, 16, 0).tex(fluidTx.getMaxU(), fluidTx.getMaxV()).endVertex();
                    vertexbuffer.pos(16, 0, 0).tex(fluidTx.getMaxU(), fluidTx.getMinV()).endVertex();
                    vertexbuffer.pos(0, 0, 0).tex(fluidTx.getMinU(), fluidTx.getMinV()).endVertex();
                    tessellator.draw();
                } catch(Exception e)
                {
                    BDSM.LOGGER.error("Error rendering barrel fluid", e);
                }

                RenderSystem.enableLighting();
                RenderSystem.popMatrix();
                matrixStackIn.pop();
            }
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

        if(barrel.isLocked()) GuiUtils.drawTexturedModalRect(matrixStackIn, -36, -24, 0, 0, 16, 16, 0);
        if(barrel.isOreDict()) GuiUtils.drawTexturedModalRect(matrixStackIn, -36, -8, 32, 0, 16, 16, 0);
        if(barrel.voidOverflow()) GuiUtils.drawTexturedModalRect(matrixStackIn, -36,8, 16, 0, 16, 16, 0);

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

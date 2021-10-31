package funwayguy.bdsandm.client;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import funwayguy.bdsandm.core.BDSM;
import funwayguy.bdsandm.inventory.ShippingContainer;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class ShippingScreen extends ContainerScreen<ShippingContainer>
{
    private static final ResourceLocation CHEST_GUI_TEXTURE = new ResourceLocation("textures/gui/container/generic_54.png");

    public ShippingScreen(ShippingContainer container, PlayerInventory playerInvo, ITextComponent title)
    {
        super(container, playerInvo, title);

        this.passEvents = false;
        this.ySize = 114 + 3 * 18;
    }

    /**
     * Draws the screen and all the components in it.
     */
    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks)
    {
        this.renderBackground(matrixStack);

        super.render(matrixStack, mouseX, mouseY, partialTicks);
        this.renderHoveredTooltip(matrixStack, mouseX, mouseY);
    }

    /**
     * Draw the foreground layer for the GuiContainer (everything in front of the items)
     */

    @Override
    protected void drawGuiContainerForegroundLayer(MatrixStack matrixStack, int mouseX, int mouseY)
    {
        this.font.drawText(matrixStack, getTitle(), 8, 6, 4210752);
        this.font.drawString(matrixStack, this.playerInventory.getDisplayName().getString(), 8, this.ySize - 96 + 2, 4210752);
    }

    /**
     * Draws the background layer of this container (behind the items).
     */
    @Override
    protected void drawGuiContainerBackgroundLayer(MatrixStack matrixStack, float partialTicks, int x, int y)
    {
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.minecraft.getTextureManager().bindTexture(this.CHEST_GUI_TEXTURE);
        int i = (this.width - this.xSize) / 2;
        int j = (this.height - this.ySize) / 2;
        this.blit(matrixStack, i, j, 0, 0, this.xSize, 3 * 18 + 17);
        this.blit(matrixStack, i, j + 3 * 18 + 17, 0, 126, this.xSize, 96);
    }
}

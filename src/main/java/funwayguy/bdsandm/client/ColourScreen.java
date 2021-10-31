package funwayguy.bdsandm.client;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager.DestFactor;
import com.mojang.blaze3d.platform.GlStateManager.SourceFactor;
import com.mojang.blaze3d.systems.RenderSystem;
import funwayguy.bdsandm.client.color.IBdsmColorBlock;
import funwayguy.bdsandm.core.BDSM;
import funwayguy.bdsandm.network.ClientPacketBdsm;
import funwayguy.bdsandm.network.ServerPacketBdsm;
import funwayguy.bdsandm.network.PacketHandler;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

import java.util.Arrays;

public class ColourScreen extends Screen
{
    private static final ResourceLocation SLIDER_TEXTUTRE = new ResourceLocation(BDSM.MOD_ID, "textures/gui/color_sliders.png");
    //private static final int MAX_HISTORY = 5;
    
    private static final int[][] HISTORY = new int[10][4];
    
    // These are mostly used for when the data is sent off to the server for application
    private final World world;
    private final BlockPos pos;
    private final int[] colors;
    
    private int curIndex = 0;
    private int dragChan = -1;
    private int lastHistory = -1;
    
    public ColourScreen(IBdsmColorBlock block, World world, BlockPos pos)
    {
        super(StringTextComponent.EMPTY);
        this.world = world;
        this.pos = pos;
    
        BlockState state = world.getBlockState(pos);
        this.colors = Arrays.copyOf(block.getColors(world, state, pos), block.getColorCount(world, state, pos));
        findInHistory();
    }

	public static void openScreen(BlockState state, World worldIn, BlockPos pos) {
        Minecraft.getInstance().displayGuiScreen(new ColourScreen((IBdsmColorBlock)state.getBlock(), worldIn, pos));
	}

	@Override
    protected void init() 
    {
        int cx = this.width/2;
        int cy = this.height/2;

        this.addButton(new Button(cx - 50, (cy + 96) - 20, 100, 20, new TranslationTextComponent("gui.done"), (button) -> {
            int[] tmp = new int[HISTORY[0].length];

            for(int i = 0; i < tmp.length; i++)
            {
                if(i < colors.length)
                {
                    tmp[i] = colors[i];
                } else if(lastHistory >= 0)
                {
                    tmp[i] = HISTORY[lastHistory][i];
                } else
                {
                    tmp[i] = 0xFF000000;
                }
            }

            // Shift history
            if(lastHistory != 0)
            {
                for(int i = lastHistory < 0 ? HISTORY.length - 1 : lastHistory; i > 0; i--)
                {
                    HISTORY[i] = Arrays.copyOf(HISTORY[i - 1], HISTORY[0].length);
                }
            }

            // Save new history
            HISTORY[0] = tmp;

            // Send changes to server
            CompoundNBT tags = new CompoundNBT();
            tags.putInt("msgType", 1);
            tags.putString("dim", world.getDimensionKey().getLocation().toString());
            tags.putLong("pos", pos.toLong());
            tags.putIntArray("color", colors);

            PacketHandler.NETWORK.sendToServer(new ServerPacketBdsm(tags));

            minecraft.displayGuiScreen(null);
        }));

        for(int i = 0; i < this.colors.length; i++)
        {
            Button colorBtn = new Button(cx - 128 + i * 24, cy - 96, 20, 20, new StringTextComponent("" + i), (button) -> {
                curIndex = Integer.parseInt(button.getMessage().getString());
                for(Widget btn : this.buttons)
                {
                    btn.active = btn != button;
                }
            });
            colorBtn.active = i != curIndex;
            this.addButton(colorBtn);
        }
    }

    @Override
    public void render(MatrixStack matrixStack, int mx, int my, float partialTicks) 
    {
        RenderSystem.pushMatrix();

        int xLeft = this.width/2 - 128;
        int yTop = this.height/2 - 96;

        int col = colors[curIndex];

        // argb
        int rMask = 255 << 16;
        int gMask = 255 << 8;
        int bMask = 255;

        int noR = col & ~rMask;
        int noG = col & ~gMask;
        int noB = col & ~bMask;

        this.renderBackground(matrixStack);
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);

        this.minecraft.getTextureManager().bindTexture(SLIDER_TEXTUTRE);
        RenderSystem.color3f(1F, ((noR >> 8) & 255) / 255F, (noR & 255) / 255F);
        this.blit(matrixStack, xLeft + 16, yTop + 40, 128, 16, 128, 16);
        RenderSystem.color3f(1F, 1F, 1F);
        this.blit(matrixStack, xLeft + 8 + ((col >> 16) & 255) / 2, yTop + 40, 128, 64, 16, 16);
        this.drawString(matrixStack, minecraft.fontRenderer, "R", xLeft, yTop + 44, 0xFFFF0000);
        this.drawString(matrixStack, minecraft.fontRenderer, "" + ((col >> 16) & 255), xLeft + 128 + 24, yTop + 44, 0xFFFF0000);

        this.minecraft.getTextureManager().bindTexture(SLIDER_TEXTUTRE);
        RenderSystem.color3f(((noG >> 16) & 255) / 255F, 1F, (noG & 255) / 255F);
        this.blit(matrixStack, xLeft + 16, yTop + 60, 128, 32, 128, 16);
        RenderSystem.color3f(1F, 1F, 1F);
        this.blit(matrixStack, xLeft + 8 + ((col >> 8) & 255) / 2, yTop + 60, 128, 64, 16, 16);
        this.drawString(matrixStack, minecraft.fontRenderer, "G", xLeft, yTop + 64, 0xFF00FF00);
        this.drawString(matrixStack, minecraft.fontRenderer, "" + ((col >> 8) & 255), xLeft + 128 + 24, yTop + 64, 0xFF00FF00);

        this.minecraft.getTextureManager().bindTexture(SLIDER_TEXTUTRE);
        RenderSystem.color3f(((noB >> 16) & 255) / 255F, ((noB >> 8) & 255) / 255F, 1F);
        this.blit(matrixStack, xLeft + 16, yTop + 80, 128, 48, 128, 16);
        RenderSystem.color3f(1F, 1F, 1F);
        this.blit(matrixStack, xLeft + 8 + (col & bMask) / 2, yTop + 80, 128, 64, 16, 16);
        this.drawString(matrixStack, minecraft.fontRenderer, "B", xLeft, yTop + 84, 0xFF0000FF);
        this.drawString(matrixStack, minecraft.fontRenderer, "" + (col & 255), xLeft + 128 + 24, yTop + 84, 0xFF0000FF);

        fillGradient(matrixStack, xLeft + 128 + 64, yTop + 40, xLeft + 128 + 64 + 60, yTop + 40 + 60, col | (255 << 24), col | (255 << 24));

        for(int i = 0; i < HISTORY.length; i++)
        {
            for(int j = 0; j < HISTORY[i].length; j++)
            {
                int hCol = 0xFF000000 | HISTORY[i][j];
                fillGradient(matrixStack, xLeft + (26 * i), yTop + 116 + (j * 10), xLeft + 16 + (26 * i), yTop + 124 + (j * 10), hCol, hCol);
            }
        }

        RenderSystem.popMatrix();

        super.render(matrixStack, mx, my, partialTicks);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY)
    {
        if(dragChan >= 0)
        {
            if (button != 0) {
                dragChan = -1;
                return false;
            } else {
                int xLeft = this.width/2 - 128;
                double value = MathHelper.clamp((mouseX - (xLeft + 16)) * 2, 0, 255);
                int mask = 255 << (8 * dragChan);

                colors[curIndex] &= ~mask;
                colors[curIndex] |= (((int)value) << (8 * dragChan));
                findInHistory();

                return true;
            }
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int click)
    {
        boolean flag = super.mouseClicked(mx, my, click);

        if(click != 0) return flag;

        int cx = this.width/2 - 128;
        int cy = this.height/2 - 96;

        if((int)mx >= cx + 16 && (int)mx < cx + 128 + 16 && (int)my < cy + 80 + 16)
        {
            if((int)my >= cy + 40 && (int)my < cy + 40 + 16)
            {
                dragChan = 2;
            } else if((int)my >= cy + 60 && (int)my < cy + 60 + 16)
            {
                dragChan = 1;
            } else if((int)my >= cy + 80 && (int)my < cy + 80 + 16)
            {
                dragChan = 0;
            } else
            {
                dragChan = -1;
            }
        } else if((int)mx >= cx && (int)mx < cx + (HISTORY.length * 26) - 10 && (int)my >= cy + 116 && (int)my < cy + 154)
        {
            int idx = ((int)mx - cx) / 26;
            idx %= HISTORY.length; // Sanity check
            lastHistory = idx;

            for(int c = 0; c < colors.length; c++)
            {
                colors[c] = HISTORY[idx][c] | 0xFF000000;
            }
        }
        return flag;
    }

    @Override
    public boolean isPauseScreen()
    {
        return false;
    }

    private void findInHistory()
    {
        topLoop:
        for(int i = 0; i < HISTORY.length; i++)
        {
            for(int j = 0; j < colors.length; j++)
            {
                if(HISTORY[i][j] != colors[j]) continue topLoop;
            }
            
            lastHistory = i;
            return;
        }
        
        lastHistory = -1;
    }
}

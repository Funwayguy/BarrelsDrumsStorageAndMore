package funwayguy.bdsandm.client;

import funwayguy.bdsandm.client.color.IBdsmColorBlock;
import funwayguy.bdsandm.core.BDSM;
import funwayguy.bdsandm.network.PacketBdsm;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.Arrays;

public class GuiColour extends GuiScreen
{
    private static final ResourceLocation SLIDER_TEXTUTRE = new ResourceLocation(BDSM.MOD_ID, "textures/gui/color_sliders.png");
    //private static final int MAX_HISTORY = 5;
    
    private static final int[][] HISTORY = new int[10][4];
    
    // These are mostly used for when the data is sent off to the server for application
    //private final IBdsmColorBlock block;
    private final World world;
    private final BlockPos pos;
    private final int[] colors;
    
    private int curIndex = 0;
    private int dragChan = -1;
    private int lastHistory = -1;
    
    public GuiColour(IBdsmColorBlock block, World world, BlockPos pos)
    {
        //this.block = block;
        this.world = world;
        this.pos = pos;
    
        IBlockState state = world.getBlockState(pos);
        this.colors = Arrays.copyOf(block.getColors(world, state, pos), block.getColorCount(world, state, pos));
        findInHistory();
    }
    
    @Override
    public void initGui()
    {
        int cx = this.width/2;
        int cy = this.height/2;
        
        this.addButton(new GuiButton(0, cx - 50, (cy + 96) - 20, 100, 20, I18n.format("gui.done")));
        
        for(int i = 0; i < this.colors.length; i++)
        {
            GuiButton btn = new GuiButton(1 + i, cx - 128 + i * 24, cy - 96, 20, 20, "" + i);
            btn.enabled = i != curIndex;
            this.addButton(btn);
        }
    }
    
    @Override
    public void drawScreen(int mx, int my, float partialTick)
    {
        GlStateManager.pushMatrix();
        
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
        
        this.drawDefaultBackground();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
        
        this.mc.getTextureManager().bindTexture(SLIDER_TEXTUTRE);
        GlStateManager.color(1F, ((noR >> 8) & 255) / 255F, (noR & 255) / 255F);
        this.drawTexturedModalRect(xLeft + 16, yTop + 40, 128, 16, 128, 16);
        GlStateManager.color(1F, 1F, 1F);
        this.drawTexturedModalRect(xLeft + 8 + ((col >> 16) & 255) / 2, yTop + 40, 128, 64, 16, 16);
        this.drawString(mc.fontRenderer, "R", xLeft, yTop + 44, 0xFFFF0000);
        this.drawString(mc.fontRenderer, "" + ((col >> 16) & 255), xLeft + 128 + 24, yTop + 44, 0xFFFF0000);
        
        this.mc.getTextureManager().bindTexture(SLIDER_TEXTUTRE);
        GlStateManager.color(((noG >> 16) & 255) / 255F, 1F, (noG & 255) / 255F);
        this.drawTexturedModalRect(xLeft + 16, yTop + 60, 128, 32, 128, 16);
        GlStateManager.color(1F, 1F, 1F);
        this.drawTexturedModalRect(xLeft + 8 + ((col >> 8) & 255) / 2, yTop + 60, 128, 64, 16, 16);
        this.drawString(mc.fontRenderer, "G", xLeft, yTop + 64, 0xFF00FF00);
        this.drawString(mc.fontRenderer, "" + ((col >> 8) & 255), xLeft + 128 + 24, yTop + 64, 0xFF00FF00);
        
        this.mc.getTextureManager().bindTexture(SLIDER_TEXTUTRE);
        GlStateManager.color(((noB >> 16) & 255) / 255F, ((noB >> 8) & 255) / 255F, 1F);
        this.drawTexturedModalRect(xLeft + 16, yTop + 80, 128, 48, 128, 16);
        GlStateManager.color(1F, 1F, 1F);
        this.drawTexturedModalRect(xLeft + 8 + (col & bMask) / 2, yTop + 80, 128, 64, 16, 16);
        this.drawString(mc.fontRenderer, "B", xLeft, yTop + 84, 0xFF0000FF);
        this.drawString(mc.fontRenderer, "" + (col & 255), xLeft + 128 + 24, yTop + 84, 0xFF0000FF);
        
        this.drawGradientRect(xLeft + 128 + 64, yTop + 40, xLeft + 128 + 64 + 60, yTop + 40 + 60, col | (255 << 24), col | (255 << 24));
        
        for(int i = 0; i < HISTORY.length; i++)
        {
            for(int j = 0; j < HISTORY[i].length; j++)
            {
                int hCol = 0xFF000000 | HISTORY[i][j];
                this.drawGradientRect(xLeft + (26 * i), yTop + 116 + (j * 10), xLeft + 16 + (26 * i), yTop + 124 + (j * 10), hCol, hCol);
            }
        }
        
        GlStateManager.popMatrix();
        
        super.drawScreen(mx, my, partialTick);
        
        // Drag Sliders
        if(dragChan >= 0)
        {
            if(!Mouse.isButtonDown(0))
            {
                dragChan = -1;
                return;
            }
            
            int value = MathHelper.clamp((mx - (xLeft + 16)) * 2, 0, 255);
            int mask = 255 << (8 * dragChan);
            
            colors[curIndex] &= ~mask;
            colors[curIndex] |= (value << (8 * dragChan));
            findInHistory();
        }
    }
    
    @Override
    protected void mouseClicked(int mx, int my, int click) throws IOException
    {
        super.mouseClicked(mx, my, click);
        
        if(click != 0) return;
        
        int cx = this.width/2 - 128;
        int cy = this.height/2 - 96;
        
        if(mx >= cx + 16 && mx < cx + 128 + 16 && my < cy + 80 + 16)
        {
            if(my >= cy + 40 && my < cy + 40 + 16)
            {
                dragChan = 2;
            } else if(my >= cy + 60 && my < cy + 60 + 16)
            {
                dragChan = 1;
            } else if(my >= cy + 80 && my < cy + 80 + 16)
            {
                dragChan = 0;
            } else
            {
                dragChan = -1;
            }
        } else if(mx >= cx && mx < cx + (HISTORY.length * 26) - 10 && my >= cy + 116 && my < cy + 154)
        {
            int idx = (mx - cx) / 26;
            idx %= HISTORY.length; // Sanity check
            lastHistory = idx;
            
            for(int c = 0; c < colors.length; c++)
            {
                colors[c] = HISTORY[idx][c] | 0xFF000000;
            }
        }
    }
    
    @Override
    public void actionPerformed(GuiButton button)
    {
        if(button.id == 0) // Apply
        {
            // Shift history
            if(lastHistory != 0)
            {
                for(int i = lastHistory < 0 ? HISTORY.length - 1 : lastHistory; i > 0; i--)
                {
                    HISTORY[i] = Arrays.copyOf(HISTORY[i - 1], HISTORY[0].length);
                }
            }
            
            // Save new history
            for(int i = 0; i < HISTORY[0].length; i++)
            {
                if(i >= colors.length)
                {
                    HISTORY[0][i] = 0xFF000000;
                } else
                {
                    HISTORY[0][i] = colors[i];
                }
            }
            
            // Send changes to server
            NBTTagCompound tags = new NBTTagCompound();
            tags.setInteger("msgType", 1);
            tags.setInteger("dim", world.provider.getDimension());
            tags.setLong("pos", pos.toLong());
            tags.setIntArray("color", colors);
            
            BDSM.INSTANCE.network.sendToServer(new PacketBdsm(tags));
            
            mc.displayGuiScreen(null);
        } else if(button.id > 0)
        {
            curIndex = button.id - 1;
            
            for(GuiButton btn : this.buttonList)
            {
                btn.enabled = curIndex != btn.id - 1;
            }
        }
    }
    
    @Override
    public boolean doesGuiPauseGame()
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

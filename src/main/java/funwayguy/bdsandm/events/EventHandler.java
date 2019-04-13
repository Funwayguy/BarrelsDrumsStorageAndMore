package funwayguy.bdsandm.events;

import funwayguy.bdsandm.blocks.BlockBarrelBase;
import funwayguy.bdsandm.blocks.BlockCrateBase;
import net.minecraft.block.state.IBlockState;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.LeftClickBlock;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@EventBusSubscriber
public class EventHandler
{
    /*private static int shaderIdx = EntityRenderer.SHADER_COUNT;
    
    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public static void onInput(InputEvent.KeyInputEvent event)
    {
        Minecraft mc = Minecraft.getMinecraft();
        
        if(mc.player.isSneaking() && Keyboard.getEventKey() == Keyboard.KEY_HOME && Keyboard.getEventKeyState())
        {
            EntityRenderer eRender = mc.entityRenderer;
            
            shaderIdx = (shaderIdx + 1) % (EntityRenderer.SHADER_COUNT + 1);
            
            if(shaderIdx == EntityRenderer.SHADER_COUNT)
            {
                eRender.stopUseShader();
                eRender.switchUseShader();
                
                mc.player.sendStatusMessage(new TextComponentString("Shader: None"), true);
            } else
            {
                ResourceLocation[] shaderList = ObfuscationReflectionHelper.getPrivateValue(EntityRenderer.class, null, "field_147712_ad", "SHADERS_TEXTURES");
                eRender.loadShader(shaderList[shaderIdx]);
                
                ShaderGroup group = eRender.getShaderGroup();
                mc.player.sendStatusMessage(new TextComponentString("Shader: " + group.getShaderGroupName()), true);
            }
        }
    }*/
    
    @SubscribeEvent
    public static void onBlockHit(LeftClickBlock event)
    {
        IBlockState state = event.getWorld().getBlockState(event.getPos());
        
        if(!event.getEntityPlayer().isSneaking() && event.getEntityPlayer().capabilities.isCreativeMode && (state.getBlock() instanceof BlockCrateBase || state.getBlock() instanceof BlockBarrelBase))
        {
            event.setCanceled(true);
            
            if(event.getWorld().isRemote) return;
            
            BlockCrateBase crate = (BlockCrateBase)state.getBlock();
            crate.onBlockClicked(event.getWorld(), event.getPos(), event.getEntityPlayer());
        }
    }
}

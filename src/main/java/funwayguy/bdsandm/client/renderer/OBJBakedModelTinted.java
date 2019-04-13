package funwayguy.bdsandm.client.renderer;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.model.PerspectiveMapWrapper;
import net.minecraftforge.client.model.obj.OBJModel.OBJBakedModel;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import javax.vecmath.Matrix4f;
import java.util.ArrayList;
import java.util.List;

public class OBJBakedModelTinted implements IBakedModel
{
    private final OBJBakedModel original;
    private List<BakedQuad> replacedQuads;
    
    public OBJBakedModelTinted(OBJBakedModel obj)
    {
        this.original = obj;
    }
    
    @Override
    public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand)
    {
        if(replacedQuads != null)
        {
            return replacedQuads;
        }
        
        List<BakedQuad> quads = original.getQuads(state, side, rand);
        replacedQuads = new ArrayList<>(quads.size());
        
        for(BakedQuad bq : quads)
        {
            replacedQuads.add(new BakedQuad(bq.getVertexData(), 0, bq.getFace(), bq.getSprite(), bq.shouldApplyDiffuseLighting(), bq.getFormat()));
        }
        
        return replacedQuads;
    }
    
    @Override
    public boolean isAmbientOcclusion()
    {
        return original.isAmbientOcclusion();
    }
    
    @Override
    public boolean isGui3d()
    {
        return original.isGui3d();
    }
    
    @Override
    public boolean isBuiltInRenderer()
    {
        return original.isBuiltInRenderer();
    }
    
    @Override
    public TextureAtlasSprite getParticleTexture()
    {
        return original.getParticleTexture();
    }
    
    @Override
    public ItemOverrideList getOverrides()
    {
        return original.getOverrides();
    }
    
    @Override
    public Pair<? extends IBakedModel, Matrix4f> handlePerspective(TransformType cameraTransformType)
    {
        return PerspectiveMapWrapper.handlePerspective(this, original.getState(), cameraTransformType);
    }
    
    @Override
    public String toString()
    {
        return original.toString();
    }
}

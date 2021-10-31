package funwayguy.bdsandm.client.renderer;

public class OBJBakedModelTinted //implements IBakedModel
{
//    private final OBJBakedModel original;
//    private List<BakedQuad> replacedQuads;
//
//    public OBJBakedModelTinted(OBJBakedModel obj)
//    {
//        this.original = obj;
//    }
//
//    @Override
//    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, long rand)
//    {
//        if(replacedQuads != null)
//        {
//            return replacedQuads;
//        }
//
//        List<BakedQuad> quads = original.getQuads(state, side, rand);
//        replacedQuads = new ArrayList<>(quads.size());
//
//        for(BakedQuad bq : quads)
//        {
//            replacedQuads.add(new BakedQuad(bq.getVertexData(), 0, bq.getFace(), bq.getSprite(), bq.shouldApplyDiffuseLighting(), bq.getFormat()));
//        }
//
//        return replacedQuads;
//    }
//
//    @Override
//    public boolean isAmbientOcclusion()
//    {
//        return original.isAmbientOcclusion();
//    }
//
//    @Override
//    public boolean isGui3d()
//    {
//        return original.isGui3d();
//    }
//
//    @Override
//    public boolean isBuiltInRenderer()
//    {
//        return original.isBuiltInRenderer();
//    }
//
//    @Override
//    public TextureAtlasSprite getParticleTexture()
//    {
//        return original.getParticleTexture();
//    }
//
//    @Override
//    public ItemOverrideList getOverrides()
//    {
//        return original.getOverrides();
//    }
//
//    @Override
//    public Pair<? extends IBakedModel, Matrix4f> handlePerspective(TransformType cameraTransformType)
//    {
//        return PerspectiveMapWrapper.handlePerspective(this, original.getState(), cameraTransformType);
//    }
//
//    @Override
//    public String toString()
//    {
//        return original.toString();
//    }
}

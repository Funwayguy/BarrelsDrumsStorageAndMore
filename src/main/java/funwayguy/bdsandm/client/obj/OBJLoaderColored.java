package funwayguy.bdsandm.client.obj;

public class OBJLoaderColored //implements ICustomModelLoader
{
//    public static final OBJLoaderColored INSTANCE = new OBJLoaderColored();
//
//    private IResourceManager manager;
//    private final Set<String> enabledDomains = new HashSet<>();
//    private final Map<ResourceLocation, OBJModelColored> cache = new HashMap<>();
//    private final Map<ResourceLocation, Exception> errors = new HashMap<>();
//
//    public void addDomain(String domain)
//    {
//        enabledDomains.add(domain.toLowerCase());
//        FMLLog.log.info("OBJLoaderColored: Domain {} has been added.", domain.toLowerCase());
//    }
//
//    @Override
//    public void onResourceManagerReload(IResourceManager resourceManager)
//    {
//        this.manager = resourceManager;
//        cache.clear();
//        errors.clear();
//    }
//
//    @Override
//    public boolean accepts(ResourceLocation modelLocation)
//    {
//        return enabledDomains.contains(modelLocation.getNamespace()) && modelLocation.getPath().endsWith(".obj");
//    }
//
//    @Override
//    public IModel loadModel(ResourceLocation modelLocation) throws Exception
//    {
//        ResourceLocation file = new ResourceLocation(modelLocation.getNamespace(), modelLocation.getPath());
//        if (!cache.containsKey(file))
//        {
//            IResource resource = null;
//            try
//            {
//                try
//                {
//                    resource = manager.getResource(file);
//                }
//                catch (FileNotFoundException e)
//                {
//                    if (modelLocation.getPath().startsWith("models/block/"))
//                        resource = manager.getResource(new ResourceLocation(file.getNamespace(), "models/item/" + file.getPath().substring("models/block/".length())));
//                    else if (modelLocation.getPath().startsWith("models/item/"))
//                        resource = manager.getResource(new ResourceLocation(file.getNamespace(), "models/block/" + file.getPath().substring("models/item/".length())));
//                    else throw e;
//                }
//                OBJModelColored.Parser parser = new OBJModelColored.Parser(resource, manager);
//                OBJModelColored model = null;
//                try
//                {
//                    model = parser.parse();
//                }
//                catch (Exception e)
//                {
//                    errors.put(modelLocation, e);
//                }
//                finally
//                {
//                    cache.put(modelLocation, model);
//                }
//            }
//            finally
//            {
//                IOUtils.closeQuietly(resource);
//            }
//        }
//        OBJModelColored model = cache.get(file);
//        if (model == null) throw new ModelLoaderRegistry.LoaderException("Error loading model previously: " + file, errors.get(modelLocation));
//        return model;
//    }
}

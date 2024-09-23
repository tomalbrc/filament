# For Developers

Filament provides an API and registry for custom item, block and decoration behaviours.

Behaviours can inherit `ItemBehaviour`, `BlockBehaviour` and `DecorationBehaviour`.

Custom behaviours can be registered using the `BehaviourRegistry`.

When referencing a custom behaviour in a config file you will have to specify the whole id of the behaviour, by default filament parses behaviours without namespace with the filament namespace.

You can checkout [TSA: Decorations](https://github.com/tomalbrc/tsa-decorations/blob/main/src/main/java/de/tomalbrc/decorations/carpentry/CarpentryBehaviour.java) for an example Decoration Behaviour implementation or one of the various built-in behaviours. 

---

Filament also provides methods to load config files and blockbench models from mods using mod-ids.

This is needed in case you want your datapack to work client-side without the player having reload resources, since resources are loaded with datapacks when loading a world, and clients load the assets in earlier than that.

Blockbench models require an additional identifier for the model which are used to reference it in the decoration config files later on

```java
public class MyMod implements ModInitializer {
    @Override
    public void onInitialize() {
        FilamentLoader.loadModels("my-modid", ResourceLocation.of(...));
        FilamentLoader.loadItems("my-modid");
        FilamentLoader.loadBlocks("my-modid");
        FilamentLoader.loadDecorations("my-modid");
        PolymerResourcePackUtils.addModAssets("my-modid");
        PolymerResourcePackUtils.markAsRequired();
    }
}
```

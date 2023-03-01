package io.izzel.arclight.common.mixin.core.world.storage;

import io.izzel.arclight.common.bridge.world.storage.DerivedWorldInfoBridge;
import io.izzel.arclight.i18n.ArclightConfig;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.DimensionType;
import net.minecraft.world.storage.DerivedWorldInfo;
import net.minecraft.world.storage.IServerWorldInfo;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(DerivedWorldInfo.class)
public class DerivedWorldInfoMixin implements DerivedWorldInfoBridge {

    @Shadow @Final private IServerWorldInfo delegate;

    private ResourceLocation dimKey = null;
    private RegistryKey<DimensionType> typeKey;
    private String finalWorldName = null;

    /**
     * @author IzzelAliz
     * @reason
     */
    @Overwrite
    public String getWorldName() {
        if (ArclightConfig.spec().getCompat().isNormalizedWorldNames()){
            if (finalWorldName == null) {
                finalWorldName = dimKey == null ? "overworld" : dimKey.getPath();
                switch (finalWorldName){
                    case "overworld":
                        finalWorldName = delegate.getWorldName();
                        break;
                    case "the_nether":
                        finalWorldName = "DIM-1";
                        break;
                    case "the_end":
                        finalWorldName = "DIM1";
                        break;
                }
            }
            return finalWorldName;
        }

        if (typeKey == null || typeKey == DimensionType.OVERWORLD) {
            return this.delegate.getWorldName();
        } else {
            if (ArclightConfig.spec().getCompat().isSymlinkWorld()) {
                String worldName = this.delegate.getWorldName() + "_";
                String suffix;
                if (typeKey == DimensionType.THE_NETHER) {
                    suffix = "nether";
                } else if (typeKey == DimensionType.THE_END) {
                    suffix = "the_end";
                } else {
                    suffix = (typeKey.getLocation().getNamespace() + "_" + typeKey.getLocation().getPath()).replace('/', '_');
                }
                return worldName + suffix;
            } else {
                String worldName = this.delegate.getWorldName() + "/";
                String suffix;
                if (typeKey == DimensionType.THE_END) {
                    suffix = "DIM1";
                } else if (typeKey == DimensionType.THE_NETHER) {
                    suffix = "DIM-1";
                } else {
                    suffix = typeKey.getLocation().getNamespace() + "/" + typeKey.getLocation().getPath();
                }
                return worldName + suffix;
            }
        }
    }

    @Override
    public void bridge$setDimKey(ResourceLocation dimKey) {
        this.dimKey = dimKey;
    }

    @Override
    public IServerWorldInfo bridge$getDelegate() {
        return delegate;
    }

    @Override
    public void bridge$setDimType(RegistryKey<DimensionType> typeKey) {
        this.typeKey = typeKey;
        this.finalWorldName = null;
    }
}

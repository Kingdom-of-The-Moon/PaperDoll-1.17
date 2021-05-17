package net.dreemurr.paperdoll.mixin;

import net.dreemurr.paperdoll.config.Config;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin<T extends LivingEntity, M extends EntityModel<T>> extends EntityRenderer<T> implements FeatureRendererContext<T, M> {

    protected LivingEntityRendererMixin(EntityRenderDispatcher dispatcher) {
        super(dispatcher);
    }

    @Inject(method = "hasLabel", at = @At("HEAD"), cancellable = true)
    public void hasLabel(T livingEntity, CallbackInfoReturnable<Boolean> cir) {
        if ((boolean) Config.entries.get("nametag").value && MinecraftClient.isHudEnabled() && livingEntity == MinecraftClient.getInstance().player)
            cir.setReturnValue(true);
    }
}

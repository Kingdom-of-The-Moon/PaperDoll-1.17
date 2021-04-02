package net.dreemurr.paperdoll.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import net.dreemurr.paperdoll.PaperDoll;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Quaternion;
import net.minecraft.util.math.Vec3f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static net.minecraft.client.gui.screen.ingame.HandledScreen.BACKGROUND_TEXTURE;

@Mixin(InGameHud.class)
public class InGameHudMixin extends DrawableHelper {

    //activity time
    private static long lastActivityTime;

    @Shadow @Final private MinecraftClient client;

    @Inject(method="render", at=@At("RETURN"))
    public void render(MatrixStack matrices, float f, CallbackInfo ci) {

        //dont draw if the F3 screen is open or if hud is hidden
        if (this.client.options.debugEnabled || this.client.options.hudHidden) {
            return;
        }

        //not the player
        ClientPlayerEntity player = this.client.player;
        if(player == null) {
            return;
        }

        //not render if first person only or player is sleeping
        if ((PaperDoll.fponly && !MinecraftClient.getInstance().options.getPerspective().isFirstPerson()) || player.isSleeping())
            return;

        //check if should stay always on
        if (!PaperDoll.alwayson) {
            //if action - reset activity time and enable can draw
            if (player.isSprinting() || player.isSneaking() || player.isUsingRiptide() || player.isInSwimmingPose() || player.isFallFlying() || player.isBlocking() || player.isClimbing() || player.getAbilities().flying)
                lastActivityTime = System.currentTimeMillis();
            //if activity time is greater than duration - return
            else if(System.currentTimeMillis() - lastActivityTime > PaperDoll.delay) return;
        }

        //draw
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderTexture(0, BACKGROUND_TEXTURE);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        drawEntity((int)((15 + PaperDoll.x) * PaperDoll.scale), (int)((60 + PaperDoll.y) * PaperDoll.scale), (int)(30 * PaperDoll.scale), PaperDoll.rotation, player);
    }

    private static void drawEntity(int x, int y, int size, float mouseX, LivingEntity entity) {
        MatrixStack matrixStack = RenderSystem.getModelViewStack();
        matrixStack.push();
        matrixStack.translate(x, y, 1050.0D);
        matrixStack.scale(1.0F, 1.0F, -1.0F);
        RenderSystem.applyModelViewMatrix();
        MatrixStack matrixStack2 = new MatrixStack();
        matrixStack2.translate(0.0D, 0.0D, 1000.0D);
        matrixStack2.scale((float)size, (float)size, (float)size);
        Quaternion quaternion = Vec3f.POSITIVE_Z.getDegreesQuaternion(180.0F);
        Quaternion quaternion2 = Vec3f.POSITIVE_X.getDegreesQuaternion(0.01F);
        quaternion.hamiltonProduct(quaternion2);
        matrixStack2.multiply(quaternion);
        float h = entity.bodyYaw;
        float l = entity.headYaw;
        float k = entity.pitch;

        //player offset and pitch
        double o = 0.0D;
        if (entity.isUsingRiptide() || entity.isInSwimmingPose() || entity.isFallFlying()) {
            o = 1.0D;
            entity.pitch = 0;
        }
        else if (entity.hasVehicle())
            o = 0.05;

        double offset = o;

        //convert to positive numbers
        if (entity.bodyYaw < 0) {
            entity.bodyYaw %= -360;
            entity.bodyYaw += 360;
        }
        if (entity.headYaw < 0) {
            entity.headYaw %= -360;
            entity.headYaw += 360;
        }
        //keep it in 360
        entity.bodyYaw %= 360;
        entity.headYaw %= 360;

        //body and foward difference
        float d = entity.bodyYaw - 180.0F;

        //apply to both head and body
        entity.bodyYaw -= d + mouseX;
        entity.headYaw -= d + mouseX;

        DiffuseLighting.method_34742();
        EntityRenderDispatcher entityRenderDispatcher = MinecraftClient.getInstance().getEntityRenderDispatcher();
        quaternion2.conjugate();
        entityRenderDispatcher.setRotation(quaternion2);
        entityRenderDispatcher.setRenderShadows(false);
        VertexConsumerProvider.Immediate immediate = MinecraftClient.getInstance().getBufferBuilders().getEntityVertexConsumers();

        RenderSystem.runAsFancy(() -> entityRenderDispatcher.render(entity, 0.0D, offset, 0.0D, 0.0F, 1.0F, matrixStack2, immediate, 15728880));
        immediate.draw();
        entityRenderDispatcher.setRenderShadows(true);
        entity.bodyYaw = h;
        entity.headYaw = l;
        entity.pitch = k;
        matrixStack.pop();
        RenderSystem.applyModelViewMatrix();
        DiffuseLighting.enableGuiDepthLighting();
    }
}

package net.dreemurr.paperdoll.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import net.dreemurr.paperdoll.config.Config;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Quaternion;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class InGameHudMixin extends DrawableHelper {

    //activity time
    private static long lastActivityTime;
    private static int guiScale;
    private static int screenHeight;

    @Shadow @Final private MinecraftClient client;

    @Inject(method="render", at=@At("RETURN"))
    public void render(MatrixStack matrices, float f, CallbackInfo ci) {

        //if hud is hidden
        if (this.client.options.hudHidden)
            return;

        //F3 screen check
        if (this.client.options.debugEnabled && !(boolean) Config.entries.get("debugRender").value)
            return;

        ClientPlayerEntity player = this.client.player;
        //if no player found or is sleeping or is not in first person when fp only
        if (player == null || ((boolean) Config.entries.get("fponly").value && !MinecraftClient.getInstance().options.getPerspective().isFirstPerson()) || player.isSleeping())
            return;

        //check if should stay always on
        if (!(boolean) Config.entries.get("alwayson").value) {
            //if action - reset activity time and enable can draw
            if (player.isSprinting() || player.isInSneakingPose() || player.isUsingRiptide() || player.isInSwimmingPose() || player.isFallFlying() || player.isBlocking() || player.isClimbing() || !player.canFly()) {
                lastActivityTime = System.currentTimeMillis();
            }
            //if activity time is greater than duration - return
            else if(System.currentTimeMillis() - lastActivityTime > (long) Config.entries.get("delay").value) return;
        }

        //store stuff
        guiScale = (int) this.client.getWindow().getScaleFactor();
        screenHeight = this.client.getWindow().getHeight();

        //draw
        drawEntity(15 + (int) Config.entries.get("x").value, 60 + (int) Config.entries.get("y").value, (int) (30 * (float) Config.entries.get("scale").value), (int) Config.entries.get("rotation").value, player);
    }

    private static void drawEntity(int x, int y, int size, float mouseX, LivingEntity entity) {
        RenderSystem.pushMatrix();
        RenderSystem.translatef((float) x, (float) y, 1050.0F);
        RenderSystem.scalef(1.0F, 1.0F, -1.0F);
        MatrixStack matrixStack = new MatrixStack();
        matrixStack.translate(0.0D, 0.0D, 1000.0D);
        matrixStack.scale((float) size, (float) size, (float) size);
        Quaternion quaternion = Vector3f.POSITIVE_Z.getDegreesQuaternion(180.0F);
        Quaternion quaternion2 = Vector3f.POSITIVE_X.getDegreesQuaternion(0.01F);
        quaternion.hamiltonProduct(quaternion2);
        matrixStack.multiply(quaternion);
        float h = entity.bodyYaw;
        float l = entity.headYaw;
        float k = entity.pitch; //get pitch

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

        //offset and pitch
        double xOff = 0.0D;
        double yOff = 0.0D;

        if (entity.isUsingRiptide() || entity.isInSwimmingPose() || entity.isFallFlying()) {
            yOff = 1.0D;
            entity.pitch = 0; //set pitch
        }
        else if (entity.hasVehicle()) {
            yOff = 0.05D;
        }

        if (entity.isFallFlying() && (boolean) Config.entries.get("elytraOffset").value) {
            int rotation = (int) (mouseX % 360);
            if (rotation < 0) rotation += 360;

            if (rotation >= 0 && rotation <= 90)
                xOff = rotation / 90.0;
            else if (rotation > 90 && rotation <= 180)
                xOff = 1 - ((rotation - 90) / 90.0);
            else if (rotation > 180 && rotation <= 270)
                xOff = -((rotation - 180) / 90.0);
            else
                xOff = -1 + ((rotation - 270) / 90.0);
        }

        matrixStack.translate(xOff, yOff, 0.0D);

        //body to front difference
        float d = entity.bodyYaw - 180.0F;

        //apply to both head and body
        entity.bodyYaw -= d + mouseX;
        entity.headYaw -= d + mouseX;

        EntityRenderDispatcher entityRenderDispatcher = MinecraftClient.getInstance().getEntityRenderDispatcher();
        quaternion2.conjugate();
        entityRenderDispatcher.setRotation(quaternion2);
        entityRenderDispatcher.setRenderShadows(false);
        VertexConsumerProvider.Immediate immediate = MinecraftClient.getInstance().getBufferBuilders().getEntityVertexConsumers();

        //render
        int box = (int) ((int) Config.entries.get("bounds").value * guiScale * (float) Config.entries.get("scale").value);
        RenderSystem.enableScissor(x * guiScale - box / 2, screenHeight - y * guiScale - box / 2 + 30 * guiScale, box, box);
        RenderSystem.runAsFancy(() -> entityRenderDispatcher.render(entity, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F, matrixStack, immediate, 15728880));
        RenderSystem.disableScissor();

        immediate.draw();
        entityRenderDispatcher.setRenderShadows(true);
        entity.bodyYaw = h;
        entity.headYaw = l;
        entity.pitch = k; //set pitch
        RenderSystem.popMatrix();
    }
}

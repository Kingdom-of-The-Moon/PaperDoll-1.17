package net.dreemurr.paperdoll.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import net.dreemurr.paperdoll.PaperDoll;
import net.dreemurr.paperdoll.config.Config;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.DiffuseLighting;
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

@Mixin(InGameHud.class)
public class InGameHudMixin extends DrawableHelper {

    //activity time
    private static long lastActivityTime;
    private static int guiScale;
    private static int screenHeight;

    @Shadow @Final private MinecraftClient client;

    @Inject(method="render", at=@At("RETURN"))
    public void render(MatrixStack matrices, float f, CallbackInfo ci) {

        //if hud is hidden or mod is disabled
        if (this.client.options.hudHidden || !(boolean) Config.entries.get("enablemod").value)
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
            if (player.isSprinting() || player.isInSneakingPose() || player.isUsingRiptide() || player.isInSwimmingPose() || player.isFallFlying() || player.isBlocking() || player.isClimbing() || player.getAbilities().flying) {
                lastActivityTime = System.currentTimeMillis();
            }
            //if activity time is greater than duration - return
            else if(System.currentTimeMillis() - lastActivityTime > (long) Config.entries.get("delay").value) return;
        }

        //store stuff
        guiScale = (int) this.client.getWindow().getScaleFactor();
        screenHeight = this.client.getWindow().getHeight();

        //draw
        try {
            drawEntity(15 + (int) Config.entries.get("x").value, 60 + (int) Config.entries.get("y").value, (int) (30 * (float) Config.entries.get("scale").value), (int) Config.entries.get("rotation").value, PaperDoll.CLONER.shallowClone(player));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void drawEntity(int x, int y, int size, float rotation, LivingEntity entity) {
        MatrixStack matrixStack = RenderSystem.getModelViewStack();
        matrixStack.push();
        matrixStack.translate(x, y, 1050.0D);
        matrixStack.scale(1.0F, 1.0F, -1.0F);
        RenderSystem.applyModelViewMatrix();
        MatrixStack matrixStack2 = new MatrixStack();
        matrixStack2.translate(0.0D, 0.0D, 1000.0D);
        matrixStack2.scale((float) size, (float) size, (float) size);
        Quaternion quaternion = Vec3f.POSITIVE_Z.getDegreesQuaternion(180.0F);
        Quaternion quaternion2 = Vec3f.POSITIVE_X.getDegreesQuaternion(0.01F);
        quaternion.hamiltonProduct(quaternion2);
        matrixStack2.multiply(quaternion);
        float h = entity.bodyYaw;
        float l = entity.headYaw;
        float k = entity.getPitch();

        //mod rotation
        rotation %= 360;
        if (rotation < 0) rotation += 360;

        //offset and pitch
        double xOff = 0.0D;
        double yOff = 0.0D;

        if (entity.isUsingRiptide() || entity.isInSwimmingPose() || entity.isFallFlying()) {
            yOff = 1.0D;
            entity.setPitch(0);
        }
        else if (entity.hasVehicle()) {
            yOff = 0.05D;
        }

        if (entity.isFallFlying() && (boolean) Config.entries.get("elytraOffset").value) {
            if (rotation >= 0 && rotation <= 90)
                xOff = rotation / 90.0;
            else if (rotation > 90 && rotation <= 180)
                xOff = 1 - ((rotation - 90) / 90.0);
            else if (rotation > 180 && rotation <= 270)
                xOff = -((rotation - 180) / 90.0);
            else
                xOff = -1 + ((rotation - 270) / 90.0);
        }

        matrixStack2.translate(xOff, yOff, 0.0D);

        //body to front difference
        float d = entity.bodyYaw - 180.0F;

        //rotate
        entity.bodyYaw -= d + rotation;
        entity.headYaw -= d + rotation;

        DiffuseLighting.method_34742();
        EntityRenderDispatcher entityRenderDispatcher = MinecraftClient.getInstance().getEntityRenderDispatcher();
        quaternion2.conjugate();
        entityRenderDispatcher.setRotation(quaternion2);
        entityRenderDispatcher.setRenderShadows(false);
        VertexConsumerProvider.Immediate immediate = MinecraftClient.getInstance().getBufferBuilders().getEntityVertexConsumers();

        //nameplate
        PaperDoll.nameplate = (boolean) Config.entries.get("nametag").value;

        //render
        int box = (int) ((int) Config.entries.get("bounds").value * guiScale * (float) Config.entries.get("scale").value);
        RenderSystem.enableScissor(x * guiScale - box / 2, screenHeight - y * guiScale - box / 2 + 30 * guiScale, box, box);
        RenderSystem.runAsFancy(() -> entityRenderDispatcher.render(entity, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F, matrixStack2, immediate, 15728880));
        RenderSystem.disableScissor();

        PaperDoll.nameplate = true;
        immediate.draw();
        entityRenderDispatcher.setRenderShadows(true);
        entity.bodyYaw = h;
        entity.headYaw = l;
        entity.setPitch(k); //set pitch
        matrixStack.pop();
        RenderSystem.applyModelViewMatrix();
        DiffuseLighting.enableGuiDepthLighting();
    }
}

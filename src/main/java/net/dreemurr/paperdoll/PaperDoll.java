package net.dreemurr.paperdoll;

import com.mojang.blaze3d.systems.RenderSystem;
import com.rits.cloning.Cloner;
import net.dreemurr.paperdoll.config.Config;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Quaternion;
import net.minecraft.util.math.Vec3f;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PaperDoll implements ClientModInitializer {

    //global stuff
    public static final Logger LOGGER = LogManager.getLogger();
    public static final Cloner CLONER = new Cloner();
    public static boolean nameplate = true;

    //paperdoll
    private static long lastActivityTime;
    private static int guiScale;
    private static int screenHeight;

    @Override
    public void onInitializeClient() {
        Config.initialize();
        HudRenderCallback.EVENT.register((matrixStack, tickDelta) -> renderPaperDoll());
    }

    public void renderPaperDoll() {
        MinecraftClient client = MinecraftClient.getInstance();

        //if hud is hidden or mod is disabled
        if (client.options.hudHidden || !(boolean) Config.entries.get("enablemod").value)
            return;

        //F3 screen check
        if (client.options.debugEnabled && !(boolean) Config.entries.get("debugRender").value)
            return;

        ClientPlayerEntity player = client.player;
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
        guiScale = (int) client.getWindow().getScaleFactor();
        screenHeight = client.getWindow().getHeight();

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
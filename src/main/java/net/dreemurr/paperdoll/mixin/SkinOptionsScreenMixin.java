package net.dreemurr.paperdoll.mixin;

import net.dreemurr.paperdoll.ConfigScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.GameOptionsScreen;
import net.minecraft.client.gui.screen.option.SkinOptionsScreen;
import net.minecraft.client.gui.widget.AbstractButtonWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.option.GameOptions;
import net.minecraft.text.TranslatableText;

@Mixin(SkinOptionsScreen.class)
public abstract class SkinOptionsScreenMixin extends GameOptionsScreen {

    protected SkinOptionsScreenMixin(Screen parent, GameOptions gameOptions) {
        super(parent, gameOptions, new TranslatableText("options.skinCustomisation.title"));
    }

    @Inject(method = "init()V", at = @At("TAIL"))
    public void init(CallbackInfo info) {
        this.<AbstractButtonWidget>addButton(new ButtonWidget(
                this.width - 105, this.height - 25,
                100, 20,
                new TranslatableText("paperdoll.menu.button"),
                button -> this.client.openScreen(new ConfigScreen(this))));
    }
}
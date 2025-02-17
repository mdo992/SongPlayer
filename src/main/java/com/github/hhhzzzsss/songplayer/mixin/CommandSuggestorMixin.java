package com.github.hhhzzzsss.songplayer.mixin;

import com.github.hhhzzzsss.songplayer.CommandProcessor;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.client.gui.screen.CommandSuggestor;
import net.minecraft.client.gui.screen.CommandSuggestor.SuggestionWindow;
import net.minecraft.client.gui.widget.TextFieldWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.CompletableFuture;

@Mixin(CommandSuggestor.class)
public class CommandSuggestorMixin {
    @Shadow
    CompletableFuture<Suggestions> pendingSuggestions;

    @Shadow
    private static int getStartOfCurrentWord(String input) {
        return 0;
    }

    @Shadow
    public void showSuggestions(boolean narrateFirstSuggestion) {}

    @Shadow
    final TextFieldWidget textField;

    public CommandSuggestorMixin() {
        textField = null;
    }

    @Inject(at = @At("TAIL"), method = "refresh()V")
    public void onRefresh(CallbackInfo ci) {
        String textStr = this.textField.getText();
        int cursorPos = this.textField.getCursor();
        String preStr = textStr.substring(0, cursorPos);
        if (!preStr.startsWith("$")) {
            return;
        }

        int wordStart = getStartOfCurrentWord(preStr);
        CompletableFuture<Suggestions> suggestions = CommandProcessor.handleSuggestions(preStr, new SuggestionsBuilder(preStr, wordStart));
        if (suggestions != null) {
            this.pendingSuggestions = suggestions;
            this.showSuggestions(true);
        }
    }
}

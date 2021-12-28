package com.t2pellet.gamocosm;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.TranslatableText;

public class GamocosmScreen extends Screen {

    private final Screen parent;

    public GamocosmScreen(Screen parent) {
        super(new TranslatableText("gamocosm.title"));
        this.parent = parent;
    }
}

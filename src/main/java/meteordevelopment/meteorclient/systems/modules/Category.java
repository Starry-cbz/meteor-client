/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.systems.modules;

import net.minecraft.client.resource.language.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

public class Category {
    public final String name;
    public final ItemStack icon;
    private final int nameHash;

    public Category(String name, ItemStack icon) {
        this.name = name;
        this.nameHash = name.hashCode();
        this.icon = icon == null ? Items.AIR.getDefaultStack() : icon;
    }
    public Category(String name) {
        this(name, null);
    }

    @Override
    public String toString() {
        return getName();
    }

    /** Returns the translated category name, falling back to the English name. */
    public String getName() {
        String key = "category.meteor-client." + name;
        return I18n.hasTranslation(key) ? I18n.translate(key) : name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Category category = (Category) o;
        return nameHash == category.nameHash;
    }

    @Override
    public int hashCode() {
        return nameHash;
    }
}

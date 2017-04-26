package com.feed_the_beast.ftbl.api.guide;

import com.feed_the_beast.ftbl.lib.EnumNameMap;
import net.minecraft.util.IStringSerializable;

/**
 * Created by PC on 17.07.2016.
 */
public enum GuideType implements IStringSerializable
{
    MOD("mod"),
    MODPACK("modpack"),
    OTHER("other");

    private static final EnumNameMap<GuideType> NAME_MAP = new EnumNameMap<>(values(), false);

    private final String name;

    GuideType(String s)
    {
        name = s;
    }

    @Override
    public String getName()
    {
        return name;
    }

    public static GuideType getFromString(String s)
    {
        GuideType type = NAME_MAP.get(s);
        return (type == null) ? OTHER : type;
    }
}
package com.feed_the_beast.ftbl.api.security;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

/**
 * Created by LatvianModder on 12.07.2016.
 */
public interface ISecure
{
    @Nullable
    UUID getOwner();

    @Nonnull
    EnumPrivacyLevel getPrivacyLevel();

    boolean saveOwner();

    boolean savePrivacyLevel();
}
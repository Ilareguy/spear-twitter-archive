package com.ilareguy.spear.util;

import androidx.annotation.NonNull;

public interface DiffCallback<D>{
    boolean isSame(@NonNull D newObject);
    boolean isContentsSame(@NonNull D newObject);
}

package com.ilareguy.spear.util;

import android.os.Bundle;

import androidx.annotation.NonNull;

public interface RestorableState{
    @NonNull Bundle saveState();
    void restoreState(final @NonNull Bundle savedState);
}

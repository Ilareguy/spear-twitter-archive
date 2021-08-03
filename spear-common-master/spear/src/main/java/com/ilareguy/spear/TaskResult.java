package com.ilareguy.spear;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class TaskResult<T>{
    private final @Nullable T object;
    private final @Nullable SpearError error;

    public TaskResult(@NonNull T object){
        this.object = object;
        this.error = null;
    }

    public TaskResult(@NonNull SpearError error){
        this.error = error;
        this.object = null;
    }

    public TaskResult(@NonNull T object, @NonNull SpearError error){
        this.object = object;
        this.error = error;
    }

    public final @Nullable T getObject(){ return object; }
    public final @Nullable SpearError getError(){ return error; }
    public final boolean isSuccessful(){ return (error == null); }
}

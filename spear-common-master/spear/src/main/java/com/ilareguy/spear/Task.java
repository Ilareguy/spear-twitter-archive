package com.ilareguy.spear;

import androidx.annotation.Nullable;

/**
 *
 * @param <Args> The arguments type this task is expecting.
 * @param <Progress> The progress type this task is returning.
 * @param <Result> The type returned by this task upon completion. Your task must return an object
 *                 of type SpearTaskResult.
 */
public abstract class Task<Args, Progress, Result>
        extends TaskTyped<Args, Progress, Result, TaskResult<Result>>{

    public Task(final @Nullable PageAbstract callingPage){
        super(callingPage);
    }

}

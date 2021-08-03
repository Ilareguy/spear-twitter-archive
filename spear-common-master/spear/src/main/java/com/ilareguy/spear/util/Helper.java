package com.ilareguy.spear.util;

import android.content.res.Resources;
import android.os.AsyncTask;
import android.util.TypedValue;

import java.util.concurrent.ThreadPoolExecutor;

import androidx.annotation.UiThread;
import androidx.annotation.WorkerThread;

public abstract class Helper{

    public static float dpToPx(float dp, final Resources r){
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics());
    }

    /**
     * Executes a task on the given executor.
     */
    public static <ResultType> void doInBackground(final ThreadPoolExecutor executor,
                                                   final doInBackgroundFunc<ResultType> run,
                                                   final doInBackgroundPostWork<ResultType> postRun){
        new AsyncTask<Void, Void, ResultType>(){
            @Override public ResultType doInBackground(Void... v){ return run.run(); }
            @Override public void onPostExecute(ResultType result){ postRun.postRun(result); }
        }.executeOnExecutor(executor);
    }

    @WorkerThread public interface doInBackgroundFunc<ResultType>{ ResultType run();}
    @UiThread public interface doInBackgroundPostWork<ResultType>{ void postRun(ResultType result);}

}

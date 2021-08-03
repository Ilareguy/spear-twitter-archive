package com.ilareguy.spear;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;

import com.ilareguy.spear.util.OnErrorListener;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;
import androidx.annotation.WorkerThread;

public class BackgroundOperationExecutor{

    public interface OnEventListener{
        void onBackgroundOperationStart(final @NonNull BackgroundOperationExecutor executor,
                                        final @NonNull BackgroundOperationExecutor.Operation operation);
        void onBackgroundOperationFinish(final @NonNull BackgroundOperationExecutor executor,
                                         final @NonNull BackgroundOperationExecutor.Operation operation);
        void onAllBackgroundOperationsFinish(final @NonNull BackgroundOperationExecutor executor);
    }

    private final ThreadPoolExecutor asyncLifecycleExecutor;
    private final List<OnEventListener> _OnEventListenerList = new ArrayList<>();

    public BackgroundOperationExecutor(){
        this.asyncLifecycleExecutor = new ThreadPoolExecutor(
                1, 1,
                1, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>()
        );
    }


    private interface OnEventListenerRunnable{
        void run(final @NonNull OnEventListener listener);
    }

    public void addOnEventListener(final @NonNull OnEventListener listener){
        _OnEventListenerList.add(listener);
    }

    public void removeOnEventListener(final @NonNull OnEventListener victim){
        _OnEventListenerList.remove(victim);
    }

    private void forEachOnEventListener(final @NonNull OnEventListenerRunnable runnable){
        for(OnEventListener listener : _OnEventListenerList)
            runnable.run(listener);
    }

    public void asyncExecute(final Operation operation){
        operation.setBackgroundOperationExecutor(this);
        operation.executeOnExecutor(asyncLifecycleExecutor);
    }

    private void onOperationFinish(final Operation operation){
        final boolean all_operations_done = (asyncLifecycleExecutor.getQueue().size() == 0);
        forEachOnEventListener((final @NonNull OnEventListener l) -> {
            l.onBackgroundOperationFinish(this, operation);
            if(all_operations_done)
                l.onAllBackgroundOperationsFinish(this);
        });
    }

    private void onOperationStart(final Operation operation){
        forEachOnEventListener((final @NonNull OnEventListener l)
                -> l.onBackgroundOperationStart(this, operation));
    }

    /**
     * Much like an AsyncTask, but an operation can have several steps, each doing their
     * own work, in a sequential order. Each SpearBackgroundOperation can have as many
     * Steps registered to it as required.
     */
    public static class Operation{

        @UiThread
        public interface OnPostExecuteListener{
            void onPostExecute(@NonNull List<SpearError> errors);
        }

        @UiThread
        public interface OnEventListener{
            void onProgressChange(float newProgress);
            void onStatusChange(String newStatus);
            void onStepFinish(final @NonNull Step previousStep, final @Nullable Step nextStep);
        }

        /**
         * Class serving as an intermediate between a Step and the owning SpearBackgroundOperation.
         * The StepHandler is responsible for reporting progress back to the owning
         * SpearBackgroundOperation object.
         */
        @WorkerThread
        public static final class StepHandler{
            private final Operation owningOperationObject;
            //private Step currentStep;
            private final Handler mainHandler = new Handler(Looper.getMainLooper());
            private boolean fatalErrorArose = false;

            private StepHandler(final Operation owningOperationObject){
                this.owningOperationObject = owningOperationObject;
            }

            /**
             * Reports the current progress for a Step.
             * @param progress The new progress, between 0 and 1.
             */
            public void reportProgress(float progress){
                synchronized(owningOperationObject){
                    if(owningOperationObject.onEventListener != null){
                        mainHandler.post(()
                                -> owningOperationObject.onEventListener.onProgressChange(progress));
                    }
                }
            }

            /**
             * Reports the current status for a Step.
             * @param status The status to be reported. This may be visible by the end-user.
             */
            public void reportStatus(String status){
                synchronized(owningOperationObject){
                    if(owningOperationObject.onEventListener != null){
                        mainHandler.post(()
                                -> owningOperationObject.onEventListener.onStatusChange(status));
                    }
                }
            }

            /**
             * Reports an error for the current Step.
             * @param error The error to be reported.
             * @param fatal True if the error is fatal, in which case no further Step will be executed;
             *              false otherwise.
             */
            public void reportError(SpearError error, boolean fatal){
                synchronized(owningOperationObject){
                    if(owningOperationObject.onErrorListener != null){
                        mainHandler.post(()
                                -> owningOperationObject.onErrorListener.onError(error));
                    }
                }
            }
        }

        private void onPostExecute(final List<SpearError> errors){
            executing = false;
            if(onPostExecuteListener != null)
                onPostExecuteListener.onPostExecute(errors);
            executor.onOperationFinish(this);
        }

        private void onPreExecute(){
            executing = true;
            executor.onOperationStart(this);
        }

        /**
         * A Step represents a single step in a SpearBackgroundOperation.
         */
        @WorkerThread
        public interface Step{
            @Nullable SpearError run(final StepHandler handler);
        }

        private List<Step> registeredSteps = new ArrayList<>();
        //private Step currentStep = null;

        private boolean executing = false, executionStarted = false;
        private BackgroundOperationExecutor executor = null;

        private @NonNull OnPostExecuteListener onPostExecuteListener = null;
        private @Nullable OnErrorListener onErrorListener = null;
        private @Nullable OnEventListener onEventListener = null;

        public Operation(){}

        public void setOnPostExecuteListener(final @NonNull OnPostExecuteListener onPostExecuteListener)
        { this.onPostExecuteListener = onPostExecuteListener; }

        @UiThread
        public void setOnErrorListener(final @Nullable OnErrorListener onErrorListener)
        { if(isExecutionStarted()){ return; } this.onErrorListener = onErrorListener; }

        @UiThread
        public void setOnEventListener(@Nullable OnEventListener onEventListener)
        { if(isExecutionStarted()){ return; } this.onEventListener = onEventListener; }

        public final synchronized boolean isExecutionStarted(){ return executionStarted; }
        public final synchronized boolean isExecuting(){ return executing; }

        /**
         * Registers a new Step to be executed as part of the SpearBackgroundOperation. Steps will execute
         * in the same order in which they were registered.
         *
         * @param newStep The new Step to register.
         * @return Returns true if the registration was successful; false otherwise, such as if the
         * operation has already executed.
         */
        @UiThread
        public synchronized boolean registerStep(Step newStep){
            if(executionStarted) return false;
            return registeredSteps.add(newStep);
        }

        @UiThread
        private synchronized void executeOnExecutor(final @NonNull ThreadPoolExecutor executor){
            if(executionStarted) return;

            executionStarted = true;
            new ExecutionTask(this).executeOnExecutor(executor);
        }

        @UiThread
        private void setBackgroundOperationExecutor(final @NonNull BackgroundOperationExecutor executor){
            this.executor = executor;
        }

        @WorkerThread
        public @NonNull List<SpearError> execute(){
            final List<SpearError> errors = new ArrayList<>();
            final StepHandler step_handler = new StepHandler(this);
            SpearError current_error;

            for(Step step : registeredSteps){
                current_error = executeStep(step, step_handler);
                if(current_error != null){
                    errors.add(current_error);
                    if(step_handler.fatalErrorArose)
                        break;
                }
            }

            return errors;
        }

        @WorkerThread
        private @Nullable SpearError executeStep(final Step step, final StepHandler stepHandler){
            //stepHandler.currentStep = step;
            return step.run(stepHandler);
        }

        private static final class ExecutionTask extends AsyncTask<Void, Void, List<SpearError>>{
            final Operation operationObject;

            ExecutionTask(final Operation operationObject){
                this.operationObject = operationObject;
            }

            @Override
            public void onPreExecute(){
                operationObject.onPreExecute();
            }

            @Override
            public List<SpearError> doInBackground(Void... v){ return operationObject.execute(); }

            @Override
            public void onPostExecute(List<SpearError> errors){
                operationObject.onPostExecute(errors);
            }
        }

    }
}

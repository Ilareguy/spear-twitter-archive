package com.ilareguy.spear.twitter.async_task;

import android.content.Context;

import com.ilareguy.spear.PageAbstract;
import com.ilareguy.spear.SpearError;
import com.ilareguy.spear.Task;
import com.ilareguy.spear.TaskResult;
import com.ilareguy.spear.twitter.TwitterApplication;
import com.ilareguy.spear.twitter.TwitterError;
import com.ilareguy.spear.twitter.data.AppDatabase;
import com.ilareguy.spear.twitter.data.LogonUser;
import com.ilareguy.spear.util.Timestamp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import static com.ilareguy.spear.twitter.TwitterErrorCode.USER_NOT_LOGGED_IN;

public class SetCurrentLogonUser extends Task<SetCurrentLogonUser.Args, Void, SetCurrentLogonUser.Result>{

    public SetCurrentLogonUser(final @Nullable PageAbstract callingPage){
        super(callingPage);
    }

    @Override
    protected TaskResult<Result> doInBackground(Args... args_c) {
        Args args = args_c[0];
        AppDatabase db = TwitterApplication.getTwitterInstance().getCacheDatabase();

        // If there was a previous user, set it as "not current" in the cache database
        if (args.previousAccount != null) {
            LogonUser previous_logon_user = db.logonUserDao().get(args.previousAccount.getUid());
            previous_logon_user.setCurrent(false);
            db.logonUserDao().update(previous_logon_user);
        }

        if (args.newAccount == null) {
            // There was an error somehow loading a new TwitterAccount object
            return new TaskResult<>(new Result(args.onCompletionListener, null),
                    TwitterError.build(USER_NOT_LOGGED_IN));
        }

        // Build its preferences file
        args.newAccount.loadPreferences(TwitterApplication.getTwitterInstance());

        // Set the new current user
        args.newAccount.setCurrent(true);
        args.newAccount.setLastLogonTimestamp(Timestamp.now());
        db.logonUserDao().save(args.newAccount);
        TwitterApplication.getTwitterInstance().setCurrentUser(args.newAccount);

        int c = db.logonUserDao().count();
        TwitterApplication._d(String.valueOf(c));

        // Success
        return new TaskResult<>(new Result(args.onCompletionListener, args.newAccount));
    }

    @Override
    protected void onPostExecute(final @NonNull TaskResult<Result> task_result){
        final Result r = task_result.getObject();

        if (task_result.isSuccessful() && r.completion_listener != null) {
            r.completion_listener.onLogonUserSwitched(r.new_account);
        } else if (!task_result.isSuccessful() && r.completion_listener != null) {
            r.completion_listener.onError(task_result.getError());
        }
    }

    public interface OnCompletionListener {
        void onLogonUserSwitched(final LogonUser new_account);
        void onError(final SpearError error);
    }

    public static final class Result {
        private final OnCompletionListener completion_listener;
        private final LogonUser new_account;

        private Result(OnCompletionListener l, LogonUser a) {
            this.completion_listener = l;
            this.new_account = a;
        }
    }

    public static final class Args {
        final Context context;
        final OnCompletionListener onCompletionListener;
        LogonUser previousAccount;
        LogonUser newAccount;

        public Args(LogonUser new_, final Context context,
                    @Nullable OnCompletionListener completion_listener) {
            this.context = context;
            this.previousAccount = TwitterApplication.getTwitterInstance().getCurrentLogonUser();
            this.newAccount = new_;
            this.onCompletionListener = completion_listener;
        }
    }

}

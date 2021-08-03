package com.ilareguy.spear.util;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashSet;
import java.util.Set;

public class PreferencesManager {

    private static final int ENTRY_TYPE_INT = 0;
    private static final int ENTRY_TYPE_STRING = 1;
    private static final int ENTRY_TYPE_LONG = 2;
    private static final int ENTRY_TYPE_FLOAT = 3;
    private static final int ENTRY_TYPE_BOOL = 4;
    private static final int ENTRY_TYPE_STRING_SET = 5;

    public abstract static class Entry<T>{

        private final String mName;
        protected T mValue;
        private boolean mValueHasChanged = false;

        public Entry(final String name){
            mName = name;
        }

        public Entry(final String name, T value){
            mName = name;
            mValue = value;
        }

        public abstract int getType();
        public void commit(SharedPreferences.Editor pref_editor){
            mValueHasChanged = false;
        }
        public abstract void load(SharedPreferences pref_object);

        public final T getValue(){ return mValue; }
        public void setValue(T value){ mValue = value; mValueHasChanged = true; }
        public final String getName(){ return mName; }
        public final boolean hasChanged(){ return mValueHasChanged; }

    }

    public static class EntryInt extends Entry<Integer>{

        private static final int DEFAULT_VALUE = 0;

        public EntryInt(final String name){
            super(name);
            setValue(DEFAULT_VALUE);
        }

        public EntryInt(final String name, int value){
            super(name, value);
        }

        public void increment(){ setValue(getValue() + 1); }
        public void decrement(){ setValue(getValue() - 1); }

        @Override
        public int getType(){ return ENTRY_TYPE_INT; }

        @Override
        public void commit(SharedPreferences.Editor pref_editor){
            super.commit(pref_editor);
            pref_editor.putInt(getName(), getValue());
        }

        @Override
        public void load(SharedPreferences pref_object){
            setValue(pref_object.getInt(getName(), DEFAULT_VALUE));
        }

    }

    public static class EntryString extends Entry<String>{

        private static final String DEFAULT_VALUE = "";

        public EntryString(final String name){
            super(name);
            setValue(DEFAULT_VALUE);
        }

        public EntryString(final String name, String value){
            super(name, value);
        }

        @Override
        public int getType(){ return ENTRY_TYPE_STRING; }

        @Override
        public void commit(SharedPreferences.Editor pref_editor){
            super.commit(pref_editor);
            pref_editor.putString(getName(), getValue());
        }

        @Override
        public void load(SharedPreferences pref_object){
            setValue(pref_object.getString(getName(), DEFAULT_VALUE));
        }

    }

    public static class EntryLong extends Entry<Long>{

        private static final long DEFAULT_VALUE = 0;

        public EntryLong(final String name){
            super(name);
            setValue(DEFAULT_VALUE);
        }

        public EntryLong(final String name, long value){
            super(name, value);
        }

        @Override
        public int getType(){ return ENTRY_TYPE_LONG; }

        @Override
        public void commit(SharedPreferences.Editor pref_editor){
            super.commit(pref_editor);
            pref_editor.putLong(getName(), getValue());
        }

        @Override
        public void load(SharedPreferences pref_object){
            setValue(pref_object.getLong(getName(), DEFAULT_VALUE));
        }

    }

    public static class EntryFloat extends Entry<Float>{

        private static final float DEFAULT_VALUE = 0;

        public EntryFloat(final String name){
            super(name);
            setValue(DEFAULT_VALUE);
        }

        public EntryFloat(final String name, float value){
            super(name, value);
        }

        @Override
        public int getType(){ return ENTRY_TYPE_FLOAT; }

        @Override
        public void commit(SharedPreferences.Editor pref_editor){
            super.commit(pref_editor);
            pref_editor.putFloat(getName(), getValue());
        }

        @Override
        public void load(SharedPreferences pref_object){
            setValue(pref_object.getFloat(getName(), DEFAULT_VALUE));
        }

    }

    public static class EntryBool extends Entry<Boolean>{

        private static final boolean DEFAULT_VALUE = false;

        public EntryBool(final String name){
            super(name);
            setValue(DEFAULT_VALUE);
        }

        public EntryBool(final String name, boolean value){
            super(name, value);
        }

        @Override
        public int getType(){ return ENTRY_TYPE_BOOL; }

        @Override
        public void commit(SharedPreferences.Editor pref_editor){
            super.commit(pref_editor);
            pref_editor.putBoolean(getName(), getValue());
        }

        @Override
        public void load(SharedPreferences pref_object){
            setValue(pref_object.getBoolean(getName(), DEFAULT_VALUE));
        }

    }

    public static class EntryStringSet extends Entry<Set<String>>{

        public EntryStringSet(final String name){
            super(name);
            mValue = new HashSet<String>();
        }

        public EntryStringSet(final String name, Set<String> value){
            super(name, value);
        }

        public boolean append(String value){ return mValue.add(value); }
        public boolean remove(String value){
            return mValue.remove(value);
        }

        @Override
        public int getType(){ return ENTRY_TYPE_STRING_SET; }

        @Override
        public void commit(SharedPreferences.Editor pref_editor){
            super.commit(pref_editor);
            pref_editor.putStringSet(getName(), getValue());
        }

        @Override
        public void load(SharedPreferences pref_object){
            setValue(pref_object.getStringSet(getName(), new HashSet<String>()));
        }

    }

    private final String mFilename;
    private boolean mIsLoaded = false;
    private Set<Entry> mRegisteredSettings;
    private final Application mApplication;

    public PreferencesManager(final Application application, final String filename){
        mFilename = filename;
        mApplication = application;
        mRegisteredSettings = new HashSet<Entry>();
    }

    /**
     * By default, the PreferencesManager will not load anything from the file.
     * You need to call load() once before you can use the PreferencesManager.
     * This does nothing if the preferences were already loaded.
     */
    public void load(){
        if(isLoaded()) return;

        // Create the SharedPreferences object
        SharedPreferences android_preferences = mApplication.getSharedPreferences(getFilename(), Context.MODE_PRIVATE);

        // load every registered entry
        for(Entry entry : mRegisteredSettings){
            entry.load(android_preferences);
        }

        mIsLoaded = true;
    }

    public void commit(){
        if(!isLoaded()) return;

        // Create the SharedPreferences and Editor objects
        SharedPreferences android_preferences = mApplication.getSharedPreferences(getFilename(), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = android_preferences.edit();

        // Save every registered entry
        for(Entry entry : mRegisteredSettings){
            if(entry.hasChanged())
                entry.commit(editor);
        }

        // Apply
        editor.apply();
    }



    /**
     * Registers a new Entry in the settings.
     * Derived classes must register all their settings before any loading
     * can be done.
     *
     * It is recommended that derived classes register their settings in the
     * constructor.
     *
     * @param entry
     */
    protected void registerEntry(Entry entry){
        mRegisteredSettings.add(entry);
    }



    public final boolean isLoaded(){ return mIsLoaded; }
    public final String getFilename() { return mFilename; }
    public final Application getApplication(){ return mApplication; }

}

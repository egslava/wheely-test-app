package org.cnii.testapp;

import org.androidannotations.annotations.sharedpreferences.SharedPref;

/**
 * Created by egslava on 04/08/14.
 */
@SharedPref
public interface Prefs {
    String username();
    String password();
}

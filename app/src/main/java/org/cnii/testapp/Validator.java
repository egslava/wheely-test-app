package org.cnii.testapp;

import android.widget.EditText;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.res.StringRes;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;

/**
 * Created by egslava on 04/08/14.
 */

@EBean
public class Validator {

    @StringRes
    String fieldCanNotBeBlank;

    public static final int CAN_NOT_BE_BLANK = 1;

    String validate(EditText editText, int flags){
        ArrayList<String> errors = new ArrayList<String>();
        String value = editText.getText().toString();

        if ( (flags & CAN_NOT_BE_BLANK) != 0 && StringUtils.isBlank(value)){
            errors.add(fieldCanNotBeBlank);
        }

        if ( errors.size() > 0 ){
            editText.setError(StringUtils.join(errors));
            return null;
        }

        return editText.getText().toString();
    }

}



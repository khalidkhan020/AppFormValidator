package com.appzone.formvalidator;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.design.widget.TextInputLayout;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import mhealth.com.validationlibrary.R;


/**
 * Created by KhalidKhan on 20/1/15.
 */
public class MEditText extends EditText {
    final static public int TEXT_NOT_EMPTY = 1;
    final static public int TEXT_NOT_ZERO = 2;
    final static public int TEXT_TEMPRATURE_RANGE_FAHRENHEIT = 4;
    final static public int TEXT_TEMPRATURE_RANGE_CELCIOUS = 8;
    final static public int TEXT_EMAIL = 16;
    final static public int TEXT_MOBILE = 256;
    final static public int TEXT_NAME = 512;
    final static public int TEXT_VALID_USER = 32;
    final static public int TEXT_MIN_MIN_LENGTH = 64;
    final static public int TEXT_NULLABLE = 128;
    final static public float MAX_FAHRENHEIT = 107f;
    final static public int MAX_CELCIOUS = 42;
    final static public int MIN_FAHRENHEIT = 93;
    final static public int MIN_CELCIOUS = 34;

    final static public int ERROR_TYPE_TOAST = 1;
    final static public int ERROR_TYPE_RETURN = 0;
    final static public int ERROR_TYPE_SET = 2;
    final static public int ERROR_TYPE_SET_TO_LAYOUT = 3;
    boolean seteror;
    int errortype = 0;
    private int validations = 0;
    private String flagName = "";
    private int textminLength = 5;
    private String errorMessage = "";

    public MEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    public MEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public MEditText(Context context) {
        super(context);
    }

    public static boolean validateEditText(View v) {
        boolean validation = true;
        if (v instanceof ViewGroup) {
            int count = ((ViewGroup) v).getChildCount();
            for (int i = 0; i < count; i++) {
                if (!validateEditText(((ViewGroup) v).getChildAt(i))) return false;
            }
        } else if (v instanceof MEditText) {
            validation = ((MEditText) v).validateText(true);
        }

        return validation;
    }

    public int isErrortypeToast() {
        return errortype;
    }

    public void setErrortypeToast(int errortype) {
        this.errortype = errortype;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public void addValidation(int validation) {
        validations = validations | validation;
    }

    public void removeValidation(int validation) {
        validations = validations & ~validation;
    }

    public void setError() {
        setError(errorMessage);
    }

    @Override
    protected void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
        setError(null);
        super.onTextChanged(text, start, lengthBefore, lengthAfter);
    }

    public void init(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.MEditText);
        validations = a.getInt(R.styleable.MEditText_validation, 0);
        flagName = a.getString(R.styleable.MEditText_flag_name);
        if (flagName == null && getHint() != null)
            flagName = getHint().toString();
        errortype = a.getInt(R.styleable.MEditText_errorType, 0);
        seteror = a.getInt(R.styleable.MEditText_errorType, 0) == 0;
        textminLength = a.getInt(R.styleable.MEditText_minLength, 0);
        a.recycle();
    }

    public boolean validateText() {

        return validateText(flagName, seteror);
    }

    public boolean validateText(boolean seteror) {

        return validateText(flagName, seteror);
    }

    public boolean validateText(String flag, boolean seteror) {
        boolean result = true;
        this.seteror = seteror;
        String text = getText().toString().trim();
        try {
            if ((validations & TEXT_NOT_EMPTY) == TEXT_NOT_EMPTY && text.equals("")) {
                errorMessage = getContext().getString(R.string.empty_field, flag);
                result = false;
            } else if ((validations & TEXT_NULLABLE) == TEXT_NULLABLE && text.equals("")) {
                //errorMessage = getContext().getString(R.string.zero_field, internetCheckFlag);
                result = true;
            } else if ((validations & TEXT_NOT_ZERO) == TEXT_NOT_ZERO && Double.parseDouble(text) <= 0) {
                errorMessage = getContext().getString(R.string.zero_field, flag);
                result = false;
            } else if ((validations & TEXT_EMAIL) == TEXT_EMAIL && !WebUtils.isEmailValid(text)) {
                errorMessage = getContext().getString(R.string.email_invalid, flag);
                result = false;
            } else if ((validations & TEXT_MOBILE) == TEXT_MOBILE && !WebUtils.isValidMobile(text)) {
                errorMessage = getContext().getString(R.string.mobile_invalid, flag);
                result = false;
            } else if ((validations & TEXT_NAME) == TEXT_NAME && !WebUtils.isUserNameValid(text)) {
                errorMessage = getContext().getString(R.string.name_invalid, flag);
                result = false;
            } else if ((validations & TEXT_VALID_USER) == TEXT_VALID_USER && !WebUtils.isUserValid(text)) {
                errorMessage = getContext().getString(R.string.invalid_username, flag);
                result = false;
            } else if ((validations & TEXT_MIN_MIN_LENGTH) == TEXT_MIN_MIN_LENGTH && text.length() < textminLength) {
                errorMessage = String.format(getResources().getString(R.string.invalid_length), flag, textminLength);
                result = false;
            }
        } catch (Exception e) {
            result = false;
        }
        if (!result && seteror && errortype == ERROR_TYPE_SET) setError();
        else if (!result && seteror && errortype == ERROR_TYPE_SET_TO_LAYOUT && getParent() instanceof TextInputLayout)
            ((TextInputLayout) getParent()).setError(errorMessage);
        else if (!result && seteror && errortype == ERROR_TYPE_TOAST)
            Toast.makeText(getContext(), errorMessage, Toast.LENGTH_LONG).show();
        else if (result && seteror && errortype == ERROR_TYPE_SET_TO_LAYOUT && getParent() instanceof TextInputLayout)
            ((TextInputLayout) getParent()).setError("");
        return result;
    }

}

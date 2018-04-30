package com.example.android.metro;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Sandeep Khan
 */

public class Utilities {
    public static final String PREF_NAME = "com.example.android.metro";

    /**
     * Checks if internet connectivity is available or not.
     * @param context Context
     * @return true if internet connectivity is available and false if no intrenet connectivity is available
     */
    public static boolean isNetworkAvailable(Context context){
        ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean v =  activeNetwork!=null && activeNetwork.isConnectedOrConnecting();
        if(v==false)
            Toast.makeText(context,"No internet connection",Toast.LENGTH_SHORT).show();
        return v;
    }

    /**
     * Checks if email entered is a valid email id or not.
     * @param emailStr Email id
     * @return true if email id is valid and false if email id is not valid.
     */
    public static boolean isEmailIDValid(String emailStr) {
        Pattern VALID_EMAIL_ADDRESS_REGEX =
                Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);

        Matcher matcher = VALID_EMAIL_ADDRESS_REGEX.matcher(emailStr);
        return matcher.find() && emailStr.length()!=0;
    }
    /**
     * Checks if name entered is of valid format or not.
     * A valid name is one which has atleast one alphabet.
     * @param nameStr Name
     * @return true if name is in valid format and false if name is not in valid format.
     */
    public static boolean isNameValid(String nameStr) {
        Pattern VALID_NAME_REGEX =
                Pattern.compile("^([A-z]+[ ]{1})*[A-z]+$", Pattern.CASE_INSENSITIVE);

        Matcher matcher = VALID_NAME_REGEX.matcher(nameStr);
        return matcher.find() && nameStr.length()!=0;
    }

    /**
     * Checks if password is valid or not. A valid password is one whose
     * length is greater than 5.
     * @param password Password
     * @return  true if password is in valid format and false if password is not in valid format.
     */
    public static boolean isPasswordValid(String password){
        return password.length()>5;
    }

}

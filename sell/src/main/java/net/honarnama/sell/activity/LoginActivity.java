package net.honarnama.sell.activity;

import com.parse.GetCallback;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;

import net.honarnama.core.activity.HonarnamaBaseActivity;
import net.honarnama.sell.R;
import net.honarnama.core.utils.GenericGravityTextWatcher;
import net.honarnama.core.utils.HonarnamaUser;
import net.honarnama.core.utils.NetworkManager;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class LoginActivity extends HonarnamaBaseActivity implements View.OnClickListener {
    private TextView mRegisterAsSellerTextView;
    private Button mLoginButton;
    private EditText mUsernameEditText;
    private EditText mPasswordEditText;
    private View mErrorMessageContainer;
    private TextView mErrorMessageTextView;
    private View mErrorMessageButton;
    private ProgressDialog mLoadingDialog;
    private TextView mForgotPasswordTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (HonarnamaUser.isShopOwner() && HonarnamaUser.isVerified()) {
            //Go to controlPanel Activity
            gotoControlPanel();

        } else {
            setContentView(R.layout.activity_login);
            mRegisterAsSellerTextView = (TextView) findViewById(R.id.register_as_seller_text_view);
            mRegisterAsSellerTextView.setOnClickListener(this);

            mLoginButton = (Button) findViewById(R.id.login_button);
            mLoginButton.setOnClickListener(this);

            mUsernameEditText = (EditText) findViewById(R.id.login_username_edit_text);
            mPasswordEditText = (EditText) findViewById(R.id.login_password_edit_text);
            mErrorMessageContainer = findViewById(R.id.login_error_container);
            mErrorMessageTextView = (TextView) findViewById(R.id.login_error_msg);
            mErrorMessageButton = findViewById(R.id.login_error_btn);
            mErrorMessageButton.setOnClickListener(this);

            mForgotPasswordTextView = (TextView) findViewById(R.id.forgot_password_text_view);
            mForgotPasswordTextView.setOnClickListener(this);

            mUsernameEditText.addTextChangedListener(new GenericGravityTextWatcher(mUsernameEditText));
            mPasswordEditText.addTextChangedListener(new GenericGravityTextWatcher(mPasswordEditText));

            ParseUser user = HonarnamaUser.getCurrentUser();
            if (user != null) {
                logI("Parse user is not empty", "user= " + user.getEmail());
                showLoadingDialog();
                user.fetchInBackground(new GetCallback<ParseObject>() {
                    @Override
                    public void done(ParseObject parseObject, ParseException e) {
                        gotoControlPanelOrRaiseError();
                        hideLoadingDialog();
                    }
                });
            } else {
                processIntent(getIntent());
            }
        }

        logI(null, "created!");
    }

    private void showLoadingDialog() {
        if (mLoadingDialog == null || !mLoadingDialog.isShowing()) {
            mLoadingDialog = ProgressDialog.show(this, "", getString(R.string.login_dialog_text), false);
            mLoadingDialog.setCancelable(false);
        }
    }

    private void hideLoadingDialog() {
        if (mLoadingDialog != null && mLoadingDialog.isShowing()) {
            mLoadingDialog.hide();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        processIntent(intent);
    }

    private void processIntent(Intent intent) {
        Uri data = intent.getData();

        logI(null, "processIntent :: data= " + data);

        if (data != null) {
            final String telegramToken = data.getQueryParameter("telegramToken");
            final String register = data.getQueryParameter("register");

            logI(null, "telegramToken= " + telegramToken + ", register= " + register);

            if (telegramToken != null && telegramToken.length() > 0) {
                showLoadingDialog();
                HonarnamaUser.telegramLogInInBackground(telegramToken, new LogInCallback() {
                    @Override
                    public void done(ParseUser parseUser, ParseException e) {
                        hideLoadingDialog();
                        if (e == null) {
                            gotoControlPanelOrRaiseError();
                        } else {
                            logE("Error while logging in using token", "telegramToken= " + telegramToken, e);
                            Toast.makeText(LoginActivity.this, R.string.error_login_failed, Toast.LENGTH_LONG).show();
                        }
                    }
                });
            } else if ("true".equals(register)) {
                Intent registerIntent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(registerIntent);
            }
        }
    }

    private void gotoControlPanel() {
        Intent intent = new Intent(this, ControlPanelActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (HonarnamaUser.isShopOwner() && HonarnamaUser.isVerified()) {
            gotoControlPanel();
        }
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.register_as_seller_text_view:
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
                break;
            case R.id.login_button:
                mErrorMessageContainer.setVisibility(View.GONE);
                signUserIn();
                break;
            case R.id.forgot_password_text_view:
                // TODO: what about email?
                Intent telegramIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://telegram.me/HonarNamaBot?start=**/login"));
                if (telegramIntent.resolveActivity(getPackageManager()) != null) {
                    startActivity(telegramIntent);
                }
                break;
            case R.id.login_error_btn:
                Intent telegramIntent2 = new Intent(Intent.ACTION_VIEW, Uri.parse("https://telegram.me/HonarNamaBot?start=" + HonarnamaUser.getCurrentUser().getString("telegramCode")));
                if (telegramIntent2.resolveActivity(getPackageManager()) != null) {
                    startActivity(telegramIntent2);
                }
            default:
                break;
        }
    }

    private void signUserIn() {
        String username = mUsernameEditText.getText().toString();
        String password = mPasswordEditText.getText().toString();

        if (!(NetworkManager.getInstance().isNetworkEnabled(this, true))) {
            return;
        }

        if (username.trim().length() == 0) {
            mUsernameEditText.requestFocus();
            mUsernameEditText.setError(getString(R.string.error_register_username_is_empty));
            return;
        }

        if (password.trim().length() == 0) {
            mPasswordEditText.requestFocus();
            mPasswordEditText.setError(getString(R.string.error_register_password_is_empty));
            return;
        }

        final ProgressDialog progressDialog = new ProgressDialog(LoginActivity.this);
        progressDialog.setMessage(getString(R.string.sending_data));
        progressDialog.setCancelable(false);
        progressDialog.show();

        ParseUser.logInInBackground(username, password, new LogInCallback() {
            public void done(ParseUser user, ParseException e) {
                progressDialog.dismiss();
                if (user != null) {
                    gotoControlPanelOrRaiseError();
                } else {
                    // Signup failed. Look at the ParseException to see what happened.
                    logE("Sign-up Failed. Code: ", e.getMessage(), e);
                    mErrorMessageContainer.setVisibility(View.VISIBLE);
                    mErrorMessageTextView.setText(getString(R.string.error_login_invalid_user_or_password));
                    mErrorMessageButton.setVisibility(View.GONE);
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mLoadingDialog != null) {
            mLoadingDialog.dismiss();
        }
    }

    private void gotoControlPanelOrRaiseError() {
        if (!HonarnamaUser.isVerified()) {
            logE("Login Failed. Account is not activated");
            mErrorMessageContainer.setVisibility(View.VISIBLE);
            mErrorMessageTextView.setText(R.string.not_verified);
            switch (HonarnamaUser.getActivationMethod()) {
                case MOBILE_NUMBER:
                    // TODO: onlt if telegram is installed
                    mErrorMessageButton.setVisibility(View.VISIBLE);
                    break;

                default:
                    mErrorMessageButton.setVisibility(View.GONE);
                    break;
            }
        } else if (!HonarnamaUser.isShopOwner()) {
            logE("Login Failed. User is not a shop owner");
            mErrorMessageContainer.setVisibility(View.VISIBLE);
            mErrorMessageTextView.setText(getString(R.string.error_login_you_are_not_shop_owner));
            mErrorMessageButton.setVisibility(View.GONE);
        } else {
            gotoControlPanel();
        }
    }
}

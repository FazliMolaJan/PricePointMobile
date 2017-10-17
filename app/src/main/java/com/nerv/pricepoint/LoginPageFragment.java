package com.nerv.pricepoint;

import android.app.Fragment;
import android.graphics.drawable.TransitionDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

/**
 * Created by NERV on 10.10.2017.
 */

public class LoginPageFragment extends CustomFragment implements View.OnClickListener {
    private View view;
    private EditText loginET;
    private EditText passwordET;
    private DatabaseManager databaseManager;
    private PageController pageController;
    private boolean transitionFlag = true;

    @Override
    public void init(MainActivity main) {
        pageController = main.getPageController();
        databaseManager = main.getDatabaseManager();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.login_page_layout, null);

        bgAnimation();

        view.findViewById(R.id.logInBtn).setOnClickListener(this);
        loginET = (EditText) view.findViewById(R.id.loginET);
        passwordET = (EditText) view.findViewById(R.id.passwordET);

        return view;
    }

    private void bgAnimation() {
        if (view == null) {
            return;
        }


        final TransitionDrawable trans = (TransitionDrawable) view.getBackground();

        Handler hand = new Handler();
        hand.postDelayed(new Runnable()
        {
            private boolean flag = true;

            @Override
            public void run()
            {
                change();
            }
            private void change()
            {
                if (transitionFlag)
                {
                    trans.startTransition(2000);
                    transitionFlag = false;
                } else
                {
                    trans.reverseTransition(2000);
                    transitionFlag = true;
                }
                bgAnimation();
            }
        }, 2000);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        view = null;
        loginET = null;
        passwordET = null;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.logInBtn) {
            String login = loginET.getText().toString();
            String password = passwordET.getText().toString();

            if (login.isEmpty()) {
                //login is empty msg
                //return;
            }

            if (password.isEmpty()) {
                //password is empty msg
                //return;
            }

            databaseManager.checkLoginPassword("box@delcom.ru", "123456", new DatabaseManager.LogInCallback() {
                @Override
                public void logInCallback(DatabaseManager.LogInResult result) {
                    switch (result) {
                        case OK:
                            databaseManager.retrieveUserTasks(new DatabaseManager.Callback() {
                                @Override
                                public void callback() {
                                    pageController.setPage(PageController.Page.ORDERS);
                                }
                            });
                            break;
                        case USER_NOT_FOUND:
                            break;
                        case WRONG_PASSWORD:
                            break;
                    }
                }
            });
        }
    }
}

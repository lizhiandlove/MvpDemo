package com.example.zwj.mvpdemo.mvp.login;

import com.example.zwj.mvpdemo.base.BasePresenter;

import javax.inject.Inject;

/**
 * <b>创建时间</b> 17/5/26 <br>
 *
 * @author zhouwenjun
 */

public class LoginPresenter extends BasePresenter<LoginContact.View> {

    @Inject
    public LoginPresenter(LoginContact.View rootView) {
        super(rootView);
    }

    public void getTime() {
        mRootView.setTime(System.currentTimeMillis() + "");
    }
}

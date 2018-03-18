package com.softonic.instamaterial.ui.activity.login;

import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.softonic.instamaterial.domain.common.UseCaseCallback;
import com.softonic.instamaterial.ui.orchestrator.SignIn;
import com.softonic.instamaterial.ui.presenter.Presenter;

/**
 * Created by javie on 22/10/2017.
 */

public class LoginPresenter extends Presenter<LoginPresenter.View> {

  private final SignIn singIn;

  public LoginPresenter(SignIn singIn) {
    this.singIn = singIn;
  }

  public void handleSignInResult(GoogleSignInResult signInResult) {
    singIn.handleSignInResult(signInResult);
  }

  public void requestLogin(int requestCode) {
    view.showLoading(true);
    singIn.execute(requestCode, new SignInCallback());
  }

  public interface View extends Presenter.View {

    void showLoading(boolean show);

    void closeLoginRequest();

    void showErrorWhileLoggingIn(String error);
  }

  private class SignInCallback implements UseCaseCallback<Boolean> {
    @Override public void onSuccess(Boolean result) {
      view.showLoading(false);
      if (result) {
        view.closeLoginRequest();
      } else {
        view.showErrorWhileLoggingIn(null);
      }
    }

    @Override public void onError(Exception exception) {
      view.showLoading(false);
      view.showErrorWhileLoggingIn(exception.getMessage());
    }
  }
}

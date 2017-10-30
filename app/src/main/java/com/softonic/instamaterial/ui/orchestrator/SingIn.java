package com.softonic.instamaterial.ui.orchestrator;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthCredential;
import com.google.firebase.auth.GoogleAuthProvider;
import com.softonic.instamaterial.R;
import com.softonic.instamaterial.domain.common.ObservableTask;
import com.softonic.instamaterial.domain.common.Subscriber;
import com.softonic.instamaterial.domain.common.UseCase;
import com.softonic.instamaterial.domain.executor.UseCaseExecutor;

/**
 * Created by javie on 22/10/2017.
 */

public class SingIn extends UseCase<Integer, Boolean> implements GoogleApiClient.OnConnectionFailedListener {

    private final FragmentActivity activity;
    private final GoogleApiClient googleApiClient;

    private Subscriber<Boolean> subscriber;

    public SingIn(UseCaseExecutor useCaseExecutor, FragmentActivity activity) {
        super(useCaseExecutor);
        this.activity = activity;

        GoogleSignInOptions signInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(activity.getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        googleApiClient = new GoogleApiClient.Builder(activity)
                .enableAutoManage(activity, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, signInOptions)
                .build();
    }

    public void handleSignInResult(GoogleSignInResult result) {
        if (subscriber != null) {
            if (result.isSuccess()) {
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
            } else {
                subscriber.onSuccess(false);
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        FirebaseAuth auth=FirebaseAuth.getInstance();
        AuthCredential credential= GoogleAuthProvider.getCredential(account.getIdToken(),null);
        auth.signInWithCredential(credential).addOnCompleteListener()

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public ObservableTask<Boolean> createObservableTask(final Integer requestCode) {
        return new ObservableTask<Boolean>() {
            @Override
            public void run(Subscriber<Boolean> result) {
                Intent intent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
                subscriber = result;
                activity.startActivityForResult(intent, requestCode);
            }
        };
    }
}

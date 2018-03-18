package com.softonic.instamaterial.data.repository.user;

import android.support.annotation.NonNull;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.softonic.instamaterial.domain.common.ObservableTask;
import com.softonic.instamaterial.domain.common.Subscriber;
import com.softonic.instamaterial.domain.model.User;

/**
 * Created by javie on 17-Mar-18.
 */

public class FirebaseUserDataSource implements UserDataSource {
  @Override public ObservableTask<User> get(String userId) {
    return null;
  }

  @Override public ObservableTask<Boolean> put(final User user) {
    return new ObservableTask<Boolean>() {
      @Override public void run(final Subscriber<Boolean> result) {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("Users");
        DatabaseReference userRef = usersRef.child(user.getId());
        UserData userData = createUserData(user);
        userRef.setValue(userData).addOnFailureListener(new OnFailureListener() {
          @Override public void onFailure(@NonNull Exception e) {
            result.onError(e);
          }
        }).addOnCompleteListener(new OnCompleteListener<Void>() {
          @Override public void onComplete(@NonNull Task<Void> task) {
            result.onSuccess(true);
          }
        });
      }
    };
  }

  private UserData createUserData(User user) {
    return new UserData(user.getUsername(), user.getPhotoUrl());
  }
}

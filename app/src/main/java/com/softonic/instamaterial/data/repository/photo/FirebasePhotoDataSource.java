package com.softonic.instamaterial.data.repository.photo;

import android.net.Uri;
import android.support.annotation.NonNull;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.softonic.instamaterial.domain.common.ObservableTask;
import com.softonic.instamaterial.domain.common.Subscriber;
import com.softonic.instamaterial.domain.model.Photo;
import com.softonic.instamaterial.domain.model.UnpublishedPhoto;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Created by javie on 18-Mar-18.
 */

public class FirebasePhotoDataSource implements PhotoDataSource {
  @Override public ObservableTask<Photo> getPhoto(final String photoId) {
    return new ObservableTask<Photo>() {
      @Override public void run(final Subscriber<Photo> result) {
        DatabaseReference photoRef =
            FirebaseDatabase.getInstance().getReference("Photos").child(photoId);
        photoRef.addListenerForSingleValueEvent(new ValueEventListener() {
          @Override public void onDataChange(DataSnapshot dataSnapshot) {
            PhotoData photoData = dataSnapshot.getValue(PhotoData.class);
            if (photoData != null) {
              result.onSuccess(createPhoto(photoId, photoData));
            } else {
              result.onError(new NoSuchElementException());
            }
          }

          @Override public void onCancelled(DatabaseError databaseError) {

            result.onError(databaseError.toException());
          }
        });
      }
    };
  }

  @Override public ObservableTask<List<Photo>> getPhotos() {
    return new ObservableTask<List<Photo>>() {
      @Override public void run(final Subscriber<List<Photo>> result) {
        final DatabaseReference photosRef = FirebaseDatabase.getInstance().getReference("Photos");
        photosRef.addValueEventListener(new ValueEventListener() {
          @Override public void onDataChange(DataSnapshot dataSnapshot) {
            List<Photo> photos = new LinkedList<>();
            for (DataSnapshot data : dataSnapshot.getChildren()) {
              try {
                PhotoData photoData = data.getValue(PhotoData.class);
                if (photoData.isValid()) {
                  photos.add(0, createPhoto(data.getKey(), photoData));
                }
              } catch (DatabaseException ex) {

              }
            }

            result.onSuccess(photos);
            photosRef.removeEventListener(this);
          }

          @Override public void onCancelled(DatabaseError databaseError) {
            result.onError(databaseError.toException());
          }
        });
      }
    };
  }

  @Override public ObservableTask<Photo> publishPhoto(final UnpublishedPhoto unpublishedPhoto) {
    return new ObservableTask<Photo>() {
      @Override public void run(final Subscriber<Photo> result) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference photosRef = database.getReference("Photos");
        final DatabaseReference photoRef = photosRef.push();
        PhotoData photoData = createPhotoData(unpublishedPhoto);
        photoRef.setValue(photoData).addOnFailureListener(new OnFailureListener() {
          @Override public void onFailure(@NonNull Exception e) {
            result.onError(e);
          }
        }).addOnCompleteListener(new OnCompleteListener<Void>() {
          @Override public void onComplete(@NonNull Task<Void> task) {
            result.onSuccess(Photo.Builder()
                .id(photoRef.getKey())
                .sourceUrl(unpublishedPhoto.getPhotoUri())
                .userId(unpublishedPhoto.getUserId())
                .description(unpublishedPhoto.getDescription())
                .build());
          }
        });
      }
    };
  }

  @Override public ObservableTask<String> uploadPhoto(final String photoUri) {
    return new ObservableTask<String>() {
      @Override public void run(final Subscriber<String> result) {
        Uri localUri = Uri.parse(photoUri);
        FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
        StorageReference storage = firebaseStorage.getReference();
        StorageReference photoRef = storage.child("images/" + localUri.getLastPathSegment());
        photoRef.putFile(localUri).addOnFailureListener(new OnFailureListener() {
          @Override public void onFailure(@NonNull Exception e) {
            result.onError(e);
          }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
          @Override public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
            Uri downloadUri = taskSnapshot.getDownloadUrl();
            if (downloadUri != null) {
              result.onSuccess(downloadUri.toString());
            } else {
              result.onError(new NoSuchElementException());
            }
          }
        });
      }
    };
  }

  private Photo createPhoto(String photoId, PhotoData photoData) {
    return new Photo.Builder().id(photoId)
        .userId(photoData.getUserId())
        .sourceUrl(photoData.getSourceUrl())
        .description(photoData.getDescription())
        .build();
  }

  private PhotoData createPhotoData(UnpublishedPhoto unpublishedPhoto) {
    return new PhotoData(unpublishedPhoto.getUserId(), unpublishedPhoto.getPhotoUri(),
        unpublishedPhoto.getDescription());
  }
}

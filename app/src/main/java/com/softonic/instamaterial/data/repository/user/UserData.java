package com.softonic.instamaterial.data.repository.user;

/**
 * Created by javie on 17-Mar-18.
 */

public class UserData {

  private String displayName;
  private String photoUrl;

  @SuppressWarnings("unused")
  public UserData() {
  }

  public UserData(String displayName, String photoUrl) {
    this.displayName = displayName;
    this.photoUrl = photoUrl;
  }

  public String getDisplayName() {
    return displayName;
  }

  public String getPhotoUrl() {
    return photoUrl;
  }
}

package com.sprint.team2.monew.domain.userActivity.exception;
import java.util.UUID;

public class UserActivityNotFoundException extends UserActivityException {
  public UserActivityNotFoundException() {
    super(UserActivityErrorCode.USER_ACTIVITY_NOT_FOUND);
  }

  public static UserActivityNotFoundException withId(UUID userId) {
    UserActivityNotFoundException exception = new UserActivityNotFoundException();
    exception.addDetail("userId", userId);
    return exception;
  }
}

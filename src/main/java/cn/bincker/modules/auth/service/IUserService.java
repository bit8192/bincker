package cn.bincker.modules.auth.service;

import cn.bincker.modules.auth.dto.UserSignUpDto;
import cn.bincker.modules.auth.entity.User;

public interface IUserService {
    Long countAll();
    User signUp(UserSignUpDto dto);
    String getUser2FAQRCodeUrl(User user);
    boolean verify2FACode(String username, int code);
}

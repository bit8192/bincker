package cn.bincker.modules.auth.service.impl;

import cn.bincker.common.mapper.UserMapper;
import cn.bincker.modules.auth.dto.UserSignUpDto;
import cn.bincker.modules.auth.entity.User;
import cn.bincker.modules.auth.service.IUserService;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import com.warrenstrange.googleauth.GoogleAuthenticatorQRGenerator;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Service
public class UserService implements UserDetailsService, IUserService {
    private final UserMapper userMapper;
    private final GoogleAuthenticator googleAuthenticator;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserMapper userMapper, PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        googleAuthenticator = new GoogleAuthenticator();
    }

    @Override
    public User loadUserByUsername(String username) throws UsernameNotFoundException {
        var user = userMapper.selectOne(Wrappers.<User>lambdaQuery().eq(User::getUsername, username));
        if (user == null) throw new UsernameNotFoundException(username + " not found");
        return user;
    }

    @Override
    public Long countAll() {
        return userMapper.selectCount(Wrappers.emptyWrapper());
    }

    @Override
    public User signUp(@Validated UserSignUpDto dto) {
        var user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setEmail(dto.getEmail());
        user.setTfaSecret(googleAuthenticator.createCredentials().getKey());
        if (userMapper.insert(user) < 1){
            return null;
        }
        return user;
    }

    @Override
    public String getUser2FAQRCodeUrl(User user) {
        return GoogleAuthenticatorQRGenerator.getOtpAuthTotpURL("bincker", user.getUsername(), new GoogleAuthenticatorKey.Builder(user.getTfaSecret()).build());
    }

    @Override
    public boolean verify2FACode(String username, int code) {
        var user = loadUserByUsername(username);
        if (user == null) return false;
        return googleAuthenticator.authorize(user.getTfaSecret(), code);
    }
}

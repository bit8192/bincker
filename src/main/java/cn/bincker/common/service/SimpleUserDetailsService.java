package cn.bincker.common.service;

import cn.bincker.common.entity.User;
import cn.bincker.common.mapper.UserMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class SimpleUserDetailsService implements UserDetailsService {
    private final UserMapper userMapper;

    public SimpleUserDetailsService(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        var user = userMapper.selectOne(Wrappers.<User>lambdaQuery().eq(User::getUsername, username));
        if (user == null) throw new UsernameNotFoundException(username + " not found");
        return user;
    }
}

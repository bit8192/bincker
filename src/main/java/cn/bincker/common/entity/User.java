package cn.bincker.common.entity;

import cn.bincker.common.DDL;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@TableName(value = "user", autoResultMap = true)
@DDL("""
drop table if exists user;
create table if not exists user(
    id unsigned bigint(20) primary key,
    created_time timestamp default current_timestamp,
    updated_time timestamp default current_timestamp,
    deleted boolean default 0,
    username varchar(32) unique not null,
    password varchar(64)
);
""")
public class User extends BaseEntity implements UserDetails {
    private String username;
    private String password;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @Override
    public boolean isAccountNonExpired() {
        return false;
    }

    @Override
    public boolean isAccountNonLocked() {
        return false;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return false;
    }
}

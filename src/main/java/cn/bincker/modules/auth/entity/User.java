package cn.bincker.modules.auth.entity;

import cn.bincker.common.annotation.DDL;
import cn.bincker.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonProperty;
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
create table if not exists user(
    id integer primary key autoincrement,
    created_time timestamp default current_timestamp,
    updated_time timestamp default current_timestamp,
    deleted boolean default 0,
    username varchar(32) unique not null,
    email varchar(32) unique,
    password varchar(64),
    tfa_secret varchar(64)
);
""")
public class User extends BaseEntity implements UserDetails {
    private String username;
    private String password;
    private String email;
    private String tfaSecret;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    public String getTfaSecret() {
        return tfaSecret;
    }
}

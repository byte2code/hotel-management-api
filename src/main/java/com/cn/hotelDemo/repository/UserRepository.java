package com.cn.hotelDemo.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import com.cn.hotelDemo.model.User;

public interface UserRepository  extends JpaRepository<User, Long> {
    User findByEmail(String email);
}

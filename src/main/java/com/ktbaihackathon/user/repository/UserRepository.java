package com.ktbaihackathon.user.repository;

import com.ktbaihackathon.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

}

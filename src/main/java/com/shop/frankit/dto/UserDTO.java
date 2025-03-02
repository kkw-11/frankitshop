package com.shop.frankit.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * UserDTO는 User 엔티티의 정보를 클라이언트에 전달할 때 사용하는 객체입니다. 보안상 비밀번호는 포함하지 않습니다.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {

    private Long id;
    private String email;
    private String role;

}

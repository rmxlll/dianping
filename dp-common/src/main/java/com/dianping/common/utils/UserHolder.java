package com.dianping.common.utils;


import com.dianping.common.UserDTO;

public class UserHolder {
    private static final ThreadLocal<UserDTO> tl = new ThreadLocal<>();

    public static void saveUser(UserDTO user){
        tl.set(user);
    }

    public static UserDTO getUser(){
        return tl.get();
    }

    public static void removeUser(){
        tl.remove();
    }


    public static void updateNickname(String newNickname) {
        if (getUser() != null) {
            UserDTO user = getUser();
            user.setNickName(newNickname);
        }
    }
}

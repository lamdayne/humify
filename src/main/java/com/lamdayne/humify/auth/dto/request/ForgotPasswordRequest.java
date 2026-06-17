package com.lamdayne.humify.auth.dto.request;

import lombok.Getter;

import java.io.Serializable;

@Getter
public class ForgotPasswordRequest implements Serializable {

    String email;

}

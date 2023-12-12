package com.example.UsersService.Responses;

import lombok.AllArgsConstructor;
import lombok.Data;


    @Data @AllArgsConstructor
    public class CustomResponse<T> {
        private int status;
        private String message;
        private T data;

        public CustomResponse(int status, String message) {
            this.status = status;
            this.message = message;
        }
        public CustomResponse(int status, T data) {
            this.status = status;
            this.data = data;
        }

    }


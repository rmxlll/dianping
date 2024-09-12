package com.dianping.blog.exception;

public class BlogNotExistException extends RuntimeException{
    public BlogNotExistException(String msg){
        super(msg);
    }
}

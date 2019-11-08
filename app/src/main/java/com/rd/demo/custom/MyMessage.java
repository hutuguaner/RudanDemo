package com.rd.demo.custom;

import com.rd.demo.model.Message;
import com.rd.demo.model.User;

import java.util.Date;

public class MyMessage extends Message {

    public MyMessage(String id, User user, String text) {
        super(id, user, text);
    }

    public MyMessage(String id, User user, String text, Date createdAt) {
        super(id, user, text, createdAt);
    }


}

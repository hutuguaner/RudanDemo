package com.rd.demo.custom;

import com.stfalcon.chatkit.commons.ImageLoader;
import com.stfalcon.chatkit.messages.MessageHolders;
import com.stfalcon.chatkit.messages.MessagesListAdapter;

public class MyMessagesListAdapter extends MessagesListAdapter {
    public MyMessagesListAdapter(String senderId, ImageLoader imageLoader) {
        super(senderId, imageLoader);
    }

    public MyMessagesListAdapter(String senderId, MessageHolders holders, ImageLoader imageLoader) {
        super(senderId, holders, imageLoader);
    }

}

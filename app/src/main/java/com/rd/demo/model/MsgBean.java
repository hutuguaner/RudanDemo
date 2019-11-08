package com.rd.demo.model;


import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Index;
import org.greenrobot.greendao.annotation.NotNull;

import java.util.Date;
import org.greenrobot.greendao.annotation.Generated;

@Entity(indexes = {@Index(value = "id,timeStamp DESC", unique = true)})
public class MsgBean {

    @Id(autoincrement = true)
    private Long id;
    @NotNull
    private String content;
    @NotNull
    private String username;
    @NotNull
    private long timeStamp;
    @Generated(hash = 23864259)
    public MsgBean(Long id, @NotNull String content, @NotNull String username,
            long timeStamp) {
        this.id = id;
        this.content = content;
        this.username = username;
        this.timeStamp = timeStamp;
    }
    @Generated(hash = 237905234)
    public MsgBean() {
    }
    public Long getId() {
        return this.id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getContent() {
        return this.content;
    }
    public void setContent(String content) {
        this.content = content;
    }
    public String getUsername() {
        return this.username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public long getTimeStamp() {
        return this.timeStamp;
    }
    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

}

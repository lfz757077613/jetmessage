package com.jetmessage.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Message {
    private String id;
    private String topic;
    private long offset;
    private byte[] content;
}

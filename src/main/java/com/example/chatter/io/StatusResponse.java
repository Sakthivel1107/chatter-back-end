package com.example.chatter.io;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class StatusResponse {

    private String id;
    private boolean online;
}

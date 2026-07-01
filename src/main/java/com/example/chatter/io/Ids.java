package com.example.chatter.io;

import com.example.chatter.entity.MessageEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Ids {
    private String senderId;
    private String receiverId;
}

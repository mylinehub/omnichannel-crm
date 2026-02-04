package com.mylinehub.crm.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CommentDTO {

     Long postId;
     String createdAt;
     String commentAuthorFirstName;
     String commentAuthorLastName;
     String content;
     String organization;
}

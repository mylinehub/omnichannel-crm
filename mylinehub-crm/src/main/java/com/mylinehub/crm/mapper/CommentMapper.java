package com.mylinehub.crm.mapper;

import com.mylinehub.crm.entity.Comment;
import com.mylinehub.crm.entity.dto.CommentDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CommentMapper {

    @Mapping(target = "postId", source = "post.postId")
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "commentAuthorFirstName", source = "author.firstName")
    @Mapping(target = "commentAuthorLastName", source = "author.lastName")
    @Mapping(target = "content", source = "content")
    @Mapping(target = "organization", source = "organization")
    CommentDTO mapCommentToDTO(Comment comment);
}

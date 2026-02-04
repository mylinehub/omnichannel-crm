package com.mylinehub.crm.repository;

import com.mylinehub.crm.entity.Post;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    @EntityGraph(attributePaths = {"author","comments"})
    List<Post> findPostByAuthorFirstNameAndAuthorLastName(String firstname, String lastName, Pageable pageable);

    @EntityGraph(attributePaths = {"author","comments"})
    List<Post> findAllBy(Pageable pageable);

    List<Post> findAllByOrganization(String organization);
    
    /*@Override
    @EntityGraph(attributePaths = {"author","comments"})
    Optional<Post> findById(Long aLong);*/
}

package com.example.project.Model;

import com.example.project.Entity.Questions;
import com.example.project.Entity.SubCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.function.Function;

@Component
public class QuestionPostFactory {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuestionPost implements Serializable {

        @NotNull(message = "category can't be blank")
        private SubCategory subCategory;

        @NotNull(message = "title can't be blank")
        private String title;

        @NotNull(message = "body can't be blank")
        private String body;
    }

    public Function<QuestionPost, Questions> rpoToPojo = questionPost -> {
        Questions questions = new Questions();
        questions.setTitle(questionPost.getTitle());
        questions.setBody(questionPost.getBody());
        questions.setPostTime(new Timestamp(System.currentTimeMillis()));
        questions.setSubCategory(questionPost.getSubCategory());
        questions.setBestAnswerId(null);
        return questions;

//        Date dt= new Date();
//        SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//        String currentTime =
    };


}

package com.example.project.Model;

import com.example.project.Entity.Questions;
import com.example.project.Entity.SubCategory;
import com.example.project.Entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.function.Function;

@Component
public class QuestionDisplayFactory {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuestionDisplay implements Serializable{

        @NotBlank(message = "id can't be blank")
        private Integer id;

        @NotBlank(message = "id can't be blank")
        private Long questionId;

        @NotNull(message = "user can't be blank")
        private User user;

        @NotNull(message = "category can't be blank")
        private SubCategory subCategory;

        @NotNull(message = "title can't be blank")
        private String title;

        @NotNull(message = "body can't be blank")
        private String body;

        @NotNull(message = "postTime can't be blank")
        private java.sql.Timestamp postTime;

        private Long bestAnswerId;

    }

    public Function<Questions, QuestionDisplay> PojoToDTO = question -> {
        QuestionDisplay questionDisplay = new QuestionDisplay();
        questionDisplay.setQuestionId(question.getQuestionId());
        questionDisplay.setUser(question.getUser());
        questionDisplay.setSubCategory(question.getSubCategory());
        questionDisplay.setTitle(question.getTitle());
        questionDisplay.setBody(question.getBody());
        questionDisplay.setPostTime(question.getPostTime());
        questionDisplay.setBestAnswerId(question.getBestAnswerId());
        return questionDisplay;
    };

}

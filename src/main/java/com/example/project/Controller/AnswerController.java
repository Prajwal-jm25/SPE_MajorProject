package com.example.project.Controller;

import com.example.project.Entity.Answers;
import com.example.project.Entity.Thumbs;
import com.example.project.Entity.User;
import com.example.project.Entity.Exception.SystemGlobalException;
import com.example.project.Entity.Response.ResponseEntity;
import com.example.project.Model.AnswerDisplayFactory;
import com.example.project.Model.AnswerPostFactory;
import com.example.project.Service.AnswerService;
import com.example.project.Service.ThumbService;
import com.example.project.Service.UserService;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/answer")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AnswerController {

    private static final Logger logger = LogManager.getLogger(AnswerController.class);
    @Autowired
    private AnswerService answerService;

    @Autowired
    private AnswerPostFactory answerPostFactory;

    @Autowired
    private UserService userService;

    @Autowired
    private ThumbService thumbService;

    @Autowired AnswerDisplayFactory answerDisplayFactory;

    @GetMapping("/list/{questionId}")
    ResponseEntity<List<AnswerDisplayFactory.AnswerDisplay>> listAnswer(@PathVariable Long questionId) throws Exception{
        StringBuilder reqMessage = new StringBuilder();
        reqMessage.append("Message = \"List Answer based on Question Id\",");
        reqMessage.append("method = [\"GET\"],");
        reqMessage.append("path = [\"/api/answer/list/{questionId}\"],");
        reqMessage.append("status = "+HttpStatus.OK.value());
        try {
            List<Answers> answerslist = answerService.listAnswers(questionId);
            logger.debug("Obtained List of Answers from Answer Service");
            List<AnswerDisplayFactory.AnswerDisplay> res = answerslist.stream().map(answerDisplayFactory.pojoToDTO).collect(Collectors.toList());
            logger.debug("Converted the List<Answers> to List<AnswerDisplay> format");
            logger.info(reqMessage);
            return new ResponseEntity<>(HttpStatus.OK.value(), "find related answers successfully", res);
        }
        catch(Exception e){
            StringBuilder errMessage = new StringBuilder();
            errMessage.append("Message = \"Error Executing ListAnswers for QuestionID\",");
            errMessage.append("method = [\"GET\"],");
            errMessage.append("path = [\"/api/answer/list/{questionId}\"],");
            errMessage.append("status = "+500);
            errMessage.append(",ExceptionMessage = "+e.getMessage());
            logger.error(errMessage);
            throw new Exception();
        }
    }

    @PostMapping("/post/{id}")
    @ResponseBody
    public ResponseEntity<Answers> postAnswer(@RequestBody @Valid AnswerPostFactory.AnswerPost answerPost, @PathVariable Integer id) throws Exception{
        StringBuilder reqMessage = new StringBuilder();
        reqMessage.append("Message = \"Executing Post Answer Endpoint\",");
        reqMessage.append("method = [POST],");
        reqMessage.append("path = [/api/answer/post/{id}],");
        reqMessage.append("status = "+HttpStatus.OK.value());
        try {
            Answers answers = answerPostFactory.rpoToPojo.apply(answerPost);
            if (answers.getAnswerBody().length() == 0) {
                logger.error("Answer is Empty");
                throw new SystemGlobalException("Answer can't be empty");
            }
            Integer userId = id;
            answers.setUser(userService.findById(userId));
            answers = answerService.saveNewAnswer(answers);
            logger.debug("Answer is saved to the database");
            logger.info(reqMessage);
            return new ResponseEntity<>(HttpStatus.OK.value(), answers);
        }
        catch(Exception e){
            StringBuilder errMessage = new StringBuilder();
            errMessage.append("Message = \"Error while executing postAnswers\",");
            errMessage.append("method = [POST],");
            errMessage.append("path = [/api/answer/post/{id}],");
            errMessage.append("status = "+500);
            errMessage.append(",ExceptionMessage = "+e.getMessage());
            logger.error(errMessage);
            throw new Exception();
        }
    }

    @GetMapping("/listMyAnswer/{id}")
    ResponseEntity<List<AnswerDisplayFactory.AnswerDisplay>> listMyAnswers(@PathVariable Integer id) throws Exception{
        StringBuilder reqMessage = new StringBuilder();
        reqMessage.append("Message = \"Executing listMyAnswers Endpoint\",");
        reqMessage.append("method = [GET],");
        reqMessage.append("path = [/api/answer/listMyAnswer/{id}],");
        reqMessage.append("status = "+HttpStatus.OK.value());
        try {
            User user = userService.findById(id);
            List<Answers> answerList = answerService.listMyAnswer(user);
            List<AnswerDisplayFactory.AnswerDisplay> res = answerList.stream().map(answerDisplayFactory.pojoToDTO).collect(Collectors.toList());
            logger.debug("Obtained list of answers in AnswerDisplay format");
            logger.info(reqMessage);
            return new ResponseEntity<>(HttpStatus.OK.value(), res);
        }
        catch(Exception e){
            StringBuilder errMessage = new StringBuilder();
            errMessage.append("Message = \"Error while executing listMyAnswers function\",");
            errMessage.append("method = [GET],");
            errMessage.append("path = [/api/answer/listMyAnswer/{id}],");
            errMessage.append("status = "+500);
            errMessage.append(",ExceptionMessage = "+e.getMessage());
            logger.error(errMessage);
            throw new Exception();
        }
    }

    @PostMapping("/like/{id}")
    ResponseEntity<Thumbs> giveLike(@RequestBody @Valid Answers answers, @PathVariable Integer id) throws Exception{
        StringBuilder reqMessage = new StringBuilder();
        reqMessage.append("Message = \"Executing givelike Endpoint\",");
        reqMessage.append("method = [POST],");
        reqMessage.append("path = [/api/answer/like/{id}],");
        reqMessage.append("status = "+HttpStatus.OK.value());
        try {
            User user = userService.findById(id);
            Thumbs thumbs = new Thumbs();
            thumbs.setAnswers(answers);
            thumbs.setUser(user);
            Thumbs tb = thumbService.saveLike(thumbs);
            logger.debug("The given like is persisted to the database");
            logger.info(reqMessage);
            return new ResponseEntity<>(HttpStatus.OK.value(), tb);
        }
        catch (Exception e){
            StringBuilder errMessage = new StringBuilder();
            errMessage.append("Message = \"Error while executing giveLike function\",");
            errMessage.append("method = [POST],");
            errMessage.append("path = [/api/answer/like/{id}],");
            errMessage.append("status = "+500);
            errMessage.append(",ExceptionMessage = "+e.getMessage());
            logger.error(errMessage);
            throw new Exception();
        }
    }

}

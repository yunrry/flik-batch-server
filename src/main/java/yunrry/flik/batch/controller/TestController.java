package yunrry.flik.batch.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class TestController {

    @Autowired
    private ApplicationContext context;

    @GetMapping("/beans")
    public String checkBeans() {
        return "tourismDataJob: " + context.containsBean("tourismDataJob") +
                ", batchController: " + context.containsBean("batchController");
    }
}
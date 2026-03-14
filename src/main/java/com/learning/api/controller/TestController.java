<<<<<<< HEAD
package com.learning.api.controller;

import com.learning.api.annotation.ApiController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@ApiController
@RequestMapping("/api")
public class TestController {

    @GetMapping("/TestController")
    public String test() {
        return "OK";
    }

    // test ok
    //http://localhost:8080/api/TestController
}
=======
//package com.learning.api.controller;
//
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//@RestController
//@RequestMapping("/api")
//public class TestController {
//    @GetMapping("/TestController")
//    public String test(){
//        return "OK";
//    }
//
//    // test ok
//    // http://localhost:8080/api/TestController
//}
>>>>>>> upstream/feature/Review

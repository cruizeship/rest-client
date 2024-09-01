package com.example.restservice;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.web.bind.annotation.RequestMapping;  
import org.springframework.beans.factory.annotation.Autowired;  
import org.springframework.jdbc.core.JdbcTemplate;  
import org.springframework.web.bind.annotation.RestController;  

@RestController
public class UserController {

  UserList a = new UserList();
  @Autowired 
  JdbcTemplate jdbc; 

	@GetMapping("/getuser")
	public User getuser(@RequestParam(value = "username") String username) {
		return a.getUser(username);
	}
  @GetMapping("/getuserlist")
	public String getuserlist() {
    System.out.println("getting list");
		return "User List: " + a.toString();
	}
  @PostMapping("/adduser")
	public void adduser(@RequestParam(value = "id") long id, @RequestParam(value = "username") String username, @RequestParam(value = "value") long value) {
		System.out.println("tyring to add");
    a.addUser(new User(id, username, value));
	}

     
  @GetMapping("/insert")  
  public String insert(){  
      jdbc.execute("INSERT INTO Persons (PersonID, LastName, FirstName, Address, City) VALUES(4, 'Random', 'Person', 'L home', 'Santa Cruz');");  
      return"data inserted Successfully";  
  }  
}

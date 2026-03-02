package com.example.demo;

import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/todos")
public class TodoController {
    private List<String> todos = new ArrayList<>();

    @GetMapping
    public List<String> getTodos() {
        return todos;
    }

    @PostMapping
    public void addTodo(@RequestBody String todo) {
        todos.add(todo);
    }

    @DeleteMapping("/{index}")
    public void deleteTodo(@PathVariable int index) {
        if (index >= 0 && index < todos.size()) {
            todos.remove(index);
        }
    }
}
